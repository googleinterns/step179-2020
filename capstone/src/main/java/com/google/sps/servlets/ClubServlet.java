package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content. */
@WebServlet("/clubs")
public class ClubServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HardcodedClubs clubData = new HardcodedClubs();
    Map<String, Club> clubs = clubData.clubs;
    Gson gson = new Gson();
    // Only accesses Club club 1 for now
    String json = gson.toJson(clubs.get("Club 1"));
    response.setContentType("text/html;");
    response.getWriter().println(json);
  }
}
