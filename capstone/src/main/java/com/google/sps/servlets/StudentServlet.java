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
    // Get student object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String studentJson =
        convertToJsonUsingGson(PrototypeStudents.PROTOTYPE_STUDENTS.get(userEmail));

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
    // TODO: All commented steps below

    // Get student object based on the logged in email

    // Add club to logged in student's club list if requested

    // Remove club from logged in student's club list if requested

    // Update student information with edited content

    response.sendRedirect("/profile.html");
  }
}
