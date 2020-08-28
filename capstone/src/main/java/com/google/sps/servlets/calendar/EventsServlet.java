package com.google.sps.servlets;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for getting events from a club's calendar */
@WebServlet("/events")
public class EventsServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String clubName = request.getParameter(Constants.PROPERTY_NAME);
    String calendarId = ServletUtil.getPropertyFromClub(clubName, Constants.CALENDAR_PROP);
    List<Event> allEvents = getEvents(calendarId);

    Gson gson = new Gson();
    String json = gson.toJson(allEvents);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  List<Event> getEvents(String calendarId) {
    List<Event> allEvents = new ArrayList<Event>();
    try {
      Calendar service = ClubServlet.getCalendarService();
      String pageToken = null;
      do {
        Events events = service.events().list(calendarId).setPageToken(pageToken).execute();
        List<Event> items = events.getItems();
        allEvents.addAll(items);
        pageToken = events.getNextPageToken();
      } while (pageToken != null);
    } catch (Exception e) {
      System.out.println("Error: " + e);
    }
    return allEvents;
  }
}
