package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.collect.ImmutableList;
import com.google.sps.gmail.EmailFactory;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/schedule-announcement")
public class ScheduleAnnouncementServlet extends HttpServlet {

  private ScheduledExecutorService mockExecutor = null; // Set a mock executor during testing.
  private ZoneId timeZone = ZoneId.of("America/Los_Angeles");
  private Clock clock = Clock.system(timeZone);

  protected void setClock(Clock clock) {
    this.clock = clock;
  }

  protected void setExecutor(ScheduledExecutorService service) {
    mockExecutor = service;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String userEmail = request.getUserPrincipal().getName();
    final String clubName = request.getParameter(Constants.PROPERTY_NAME);
    final String announcementContent = request.getParameter(Constants.CONTENT_PROP);
    String scheduledDate = request.getParameter(Constants.SCHEDULED_DATE_PROP);
    if (scheduledDate.charAt(scheduledDate.length() - 1) != 'Z') {
      scheduledDate += ":00.00Z";
    }
    Instant instant = Instant.parse(scheduledDate);
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

    Entity announcementEntity = new Entity(Constants.ANNOUNCEMENT_PROP);
    announcementEntity.setProperty(Constants.AUTHOR_PROP, userEmail);
    announcementEntity.setProperty(Constants.TIME_PROP, scheduledTimestamp);
    announcementEntity.setProperty(Constants.CONTENT_PROP, announcementContent);
    announcementEntity.setProperty(Constants.CLUB_PROP, clubName);
    announcementEntity.setProperty(Constants.EDITED_PROP, false);

    this.scheduleAnnouncement(announcementEntity, clubName, waitTime, datastore);
    if (mockExecutor == null
        && this.getServletConfig().getServletContext().getAttribute("executorService") == null) {
      return;
    }
    response.sendRedirect(
        "/about-us.html?name=" + clubName + "&tab=announcements&waittime=" + waitTime);
  }

  private void scheduleAnnouncement(
      final Entity entity,
      final String clubName,
      final long waitTime,
      final DatastoreService datastore) {

    ServletContext context = this.getServletConfig().getServletContext();
    ScheduledExecutorService service =
        (mockExecutor == null)
            ? (ScheduledExecutorService) context.getAttribute("executorService")
            : mockExecutor;
    // if (context.getAttribute("scheduler") == null) {
    //   context.setAttribute("scheduler", service);
    // } else {
    //   service = (ScheduledExecutorService) context.getAttribute("scheduler");
    // }
    service.schedule(
        new Runnable() {
          public void run() {
            datastore.put(entity);
            try {
              EmailFactory.sendEmailToAllMembers(clubName, entity);
            } catch (IOException e) {
              System.err.println(e.getMessage());
            }
          }
        },
        waitTime,
        TimeUnit.MILLISECONDS);
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
        ImmutableList.copyOf((ArrayList<String>) entity.getProperty(Constants.MEMBER_PROP)),
        ImmutableList.copyOf((ArrayList<String>) entity.getProperty(Constants.OFFICER_PROP)),
        entity.getProperty(Constants.DESCRIP_PROP).toString(),
        entity.getProperty(Constants.WEBSITE_PROP).toString(),
        key,
        ServletUtil.getPropertyList(entity, Constants.LABELS_PROP),
        Long.parseLong(entity.getProperty(Constants.TIME_PROP).toString()));
  }
}
