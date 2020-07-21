package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/clubs")
public class ClubServlet extends HttpServlet {
  private Map<String, Club> clubs = new HashMap<String, Club>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    setUp();
    response.setContentType("text/html;");
    response.getWriter().println("<h1>Hello world!</h1>");
  }
}
