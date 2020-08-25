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

/** Servlet for adding events to a club's calendar */
@WebServlet("/add-event")
public class AddEventsServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String startTime = request.getParameter(Constants.START_TIME_PROP) + Constants.TIMEZONE_OFFSET;
    String endTime = request.getParameter(Constants.END_TIME_PROP) + Constants.TIMEZONE_OFFSET;
    String title = request.getParameter(Constants.EVENT_TITLE_PROP);
    String description = request.getParameter(Constants.EVENT_DESCRIPTION_PROP);
    String clubName = request.getParameter(Constants.CLUB_PROP);
    String calendarId = getCalendarId(clubName);

    // Add event to calendar and return event information
    try {
      Event event = addEventToCalendar(calendarId, title, description, startTime, endTime);
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

  public Event addEventToCalendar(
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
    EventDateTime startTime = getEventDateTime(start);
    EventDateTime endTime = getEventDateTime(end);

    return new Event()
        .setSummary(title)
        .setDescription(description)
        .setStart(startTime)
        .setEnd(endTime);
  }

  private EventDateTime getEventDateTime(String time) {
    return new EventDateTime().setDateTime(new DateTime(time)).setTimeZone(Constants.TIME_ZONE);
  }
}
