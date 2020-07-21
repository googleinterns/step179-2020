package com.google.sps.servlets;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns students' profile content */
@WebServlet("/student-data")
public class StudentServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ImmutableMap<String, Student> students = PrototypeStudents.PROTOTYPE_STUDENTS;
    String studentsJson = convertToJsonUsingGson(students.get(PrototypeStudents.KEVIN_EMAIL));
    response.getWriter().println(studentsJson);
  }

  private static String convertToJsonUsingGson(Student student) {
    Gson gson = new Gson();
    String json = gson.toJson(student);
    return json;
  }
}
