package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content. */
@WebServlet("/clubs")
public class ClubServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String json =
        gson.toJson(
            PrototypeClubs.PROTOTYPE_CLUBS_MAP.get(request.getParameter(Constants.CLUB_NAME_PROP)));
    response.setContentType("text/html;");
    response.getWriter().println(json);
  }
}
