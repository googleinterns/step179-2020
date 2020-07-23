package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a student's profile content */
@WebServlet("/student-data")
public class StudentServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String studentJson =
        convertToJsonUsingGson(
            PrototypeStudents.PROTOTYPE_STUDENTS.get(PrototypeStudents.KEVIN_EMAIL));

    response.setContentType("application/json;");
    response.getWriter().println(studentJson);
  }

  private static String convertToJsonUsingGson(Student student) {
    Gson gson = new Gson();
    String json = gson.toJson(student);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get student object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    Student student = PrototypeStudents.PROTOTYPE_STUDENTS.get(userEmail);

    if (request.getParameter("action").equals("join")) {
      String clubToJoin = request.getParameter("club");
      if (clubToJoin != null && !clubToJoin.isEmpty()) {
        student.joinClub(clubToJoin);
      }    
    }

    if (request.getParameter("action").equals("leave")) {
      String clubToRemove = request.getParameter("club");
      if (clubToRemove != null && !clubToRemove.isEmpty()) {
        student.removeClub(clubToRemove);
      }
    }

    response.sendRedirect(request.getHeader("referer"));
  }

}
