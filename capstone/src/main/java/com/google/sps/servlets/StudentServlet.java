package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    System.out.println("here get: " + userEmail);
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
    Student student = PrototypeStudents.PROTOTYPE_STUDENTS.get(PrototypeStudents.KEVIN_EMAIL);
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    System.out.println("here: " + userEmail);
    String clubToRemove = request.getParameter("club");
    if (clubToRemove != null && !clubToRemove.isEmpty()) {
      student.removeClub(clubToRemove);
    }
    response.sendRedirect("/profile.html");
  }
}
