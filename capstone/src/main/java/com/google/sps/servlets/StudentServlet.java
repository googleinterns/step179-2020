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
}
