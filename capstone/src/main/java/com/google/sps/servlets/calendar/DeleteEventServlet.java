package com.google.sps.servlets;

import com.google.api.services.calendar.Calendar;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for deleting events from a club's calendar */
@WebServlet("/delete-event")
public class DeleteEventServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userEmail = request.getUserPrincipal().getName();
    String clubName = request.getParameter(Constants.PROPERTY_NAME);
    String calendarId = ServletUtil.getPropertyFromClub(clubName, Constants.CALENDAR_PROP);
    String eventId = request.getParameter(Constants.ID_PROP);

    Entity club = ServletUtil.getClubAsEntity(clubName);
    if (!ServletUtil.getPropertyList(club, Constants.OFFICER_PROP).contains(userEmail)) {
      // Not officer, return.
      return;
    }

    executeDelete(calendarId, eventId, response);
    response.getWriter().println(eventId);
  }

  void executeDelete(String calendarId, String eventId, HttpServletResponse response)
      throws IOException {
    try {
      Calendar service = ClubServlet.getCalendarService();
      service.events().delete(calendarId, eventId).execute();
    } catch (Exception e) {
      response.getWriter().println("Error: " + e);
    }
  }
}
