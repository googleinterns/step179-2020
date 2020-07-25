package com.google.sps.servlets;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/announcements")
public class AnnouncementsServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    ImmutableList<String> announcements =
        PrototypeClubs.PROTOTYPE_CLUBS_MAP
            .get(request.getParameter(Constants.CLUB_NAME_PROP))
            .getAnnouncements();
    String json = gson.toJson(announcements);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {}
}
