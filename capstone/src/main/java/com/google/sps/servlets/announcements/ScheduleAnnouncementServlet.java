package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/schedule-announcement")
public class ScheduleAnnouncementServlet extends HttpServlet {

  private ZoneId timeZone = ZoneId.of(Constants.TIME_ZONE);
  private Clock clock = Clock.system(timeZone);

  protected void setClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String userEmail = request.getUserPrincipal().getName();
    final String clubName = request.getParameter(Constants.PROPERTY_NAME);
    final String announcementContent = request.getParameter(Constants.CONTENT_PROP);
    String scheduledDatetime = request.getParameter(Constants.SCHEDULED_DATE_PROP);
    if (!scheduledDatetime.endsWith("Z")) {
      scheduledDatetime += ":00.00Z";
    }
    Instant instant = Instant.parse(scheduledDatetime);
    int timeZoneOffset =
        timeZone.getRules().getOffset(instant).getTotalSeconds()
            * 1000; // Offset returned in seconds, need milliseconds.
    final long scheduledTimestamp = instant.toEpochMilli() - timeZoneOffset;
    final long waitTime = scheduledTimestamp - clock.millis();
    if (waitTime <= 0) {
      return;
    }

    final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Club club = getClub(datastore, clubName);
    if (club == null || !club.hasOfficer(userEmail)) {
      return; // Not authenticated to post.
    }

    Entity announcementEntity = new Entity(Constants.FUTURE_ANNOUNCEMENT_PROP);
    announcementEntity.setProperty(Constants.AUTHOR_PROP, userEmail);
    announcementEntity.setProperty(Constants.TIME_PROP, scheduledTimestamp);
    announcementEntity.setProperty(Constants.CONTENT_PROP, announcementContent);
    announcementEntity.setProperty(Constants.CLUB_PROP, clubName);
    announcementEntity.setProperty(Constants.EDITED_PROP, false);

    datastore.put(announcementEntity);
    response.sendRedirect("/about-us.html?name=" + clubName + "&tab=announcements");
  }

  private Club getClub(DatastoreService datastore, String clubName) {
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
    Entity entity = datastore.prepare(query).asSingleEntity();
    if (entity == null) {
      return null;
    }
    String key = "";
    if (entity.getProperty(Constants.LOGO_PROP) != null) {
      key = entity.getProperty(Constants.LOGO_PROP).toString();
    }

    return new Club(
        entity.getProperty(Constants.PROPERTY_NAME).toString(),
        ServletUtil.getPropertyList(entity, Constants.MEMBER_PROP),
        ServletUtil.getPropertyList(entity, Constants.OFFICER_PROP),
        entity.getProperty(Constants.DESCRIP_PROP).toString(),
        entity.getProperty(Constants.WEBSITE_PROP).toString(),
        key,
        ServletUtil.getPropertyList(entity, Constants.LABELS_PROP),
        Long.parseLong(entity.getProperty(Constants.TIME_PROP).toString()));
  }
}
