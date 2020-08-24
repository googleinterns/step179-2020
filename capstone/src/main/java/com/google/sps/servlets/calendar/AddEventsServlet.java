package com.google.sps.servlets;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for obtaining the Calendar ID of a group and adding or obtaining events */
@WebServlet("/add-event")
public class AddEventsServlet extends HttpServlet {
  public static final String TIMEZONE_OFFSET = ":00.000-07:00";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String startTime = request.getParameter("start-time");
    String endTime = request.getParameter("end-time");
    String title = request.getParameter("event-title");
    String description = request.getParameter("event-description");
    String clubName = request.getParameter("club-name");
    String calendarId = getCalendarId(clubName);

    try {
      Event event =
          addEvent(
              calendarId,
              title,
              description,
              startTime + TIMEZONE_OFFSET,
              endTime + TIMEZONE_OFFSET);
      response.setContentType("application/json");
      response.getWriter().println(event.toString());
    } catch (Exception error) {
      System.out.println("Error: " + error);
    }
    response.sendRedirect(request.getHeader("referer"));
  }

  private String getCalendarId(String clubName) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
    PreparedQuery results = datastore.prepare(query);
    Entity club = results.asSingleEntity();
    if (club != null) {
      return club.getProperty(Constants.CALENDAR_PROP).toString();
    }
    return null;
  }

  public Event addEvent(
      String calendarId,
      String eventTitle,
      String eventDescription,
      String eventStart,
      String eventEnd)
      throws IOException, GeneralSecurityException {
    Event event = createEvent(eventTitle, eventDescription, eventStart, eventEnd);

    Calendar service = ClubServlet.getCalendarService();
    service.events().insert(calendarId, event).execute();
    return event;
  }

  private Event createEvent(String title, String description, String start, String end) {
    EventDateTime startTime = createEventDateTime(start);
    EventDateTime endTime = createEventDateTime(end);

    return new Event()
        .setSummary(title)
        .setDescription(description)
        .setStart(startTime)
        .setEnd(endTime);
  }

  private EventDateTime createEventDateTime(String time) {
    return new EventDateTime().setDateTime(new DateTime(time)).setTimeZone("America/Los_Angeles");
  }
}
