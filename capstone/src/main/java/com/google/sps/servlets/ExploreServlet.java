package com.google.sps.servlets;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content. */
@WebServlet("/explore")
public class ExploreServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    // Only accesses Clubs 1, 2, 3 for now
    ImmutableList<Club> clubs =
        ImmutableList.of(
            PrototypeClubs.PROTOTYPE_CLUBS_MAP.get(PrototypeClubs.CLUB_1),
            PrototypeClubs.PROTOTYPE_CLUBS_MAP.get(PrototypeClubs.CLUB_2),
            PrototypeClubs.PROTOTYPE_CLUBS_MAP.get(PrototypeClubs.CLUB_3));
    String json = gson.toJson(clubs);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
