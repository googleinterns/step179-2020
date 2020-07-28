package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
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

    // Store the student and each club's announcements in one StudentInfo object
    Student student = PrototypeStudents.PROTOTYPE_STUDENTS.get(userEmail);
    ImmutableList<String> announcements = getAllAnnouncements(student.getClubList());
    StudentInfo allInfo = new StudentInfo(student, announcements);

    String studentJson = convertToJsonUsingGson(allInfo);

    response.setContentType("application/json;");
    response.getWriter().println(studentJson);
  }

  private static String convertToJsonUsingGson(StudentInfo info) {
    Gson gson = new Gson();
    String json = gson.toJson(info);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // TODO: All commented steps below
    // 1. Get student object based on the logged in email
    // 2. Add club to logged in student's club list if requested
    // 3. Remove club from logged in student's club list if requested
    // 4. Update student information with edited content

    response.sendRedirect("/profile.html");
  }

  public ImmutableList<String> getAllAnnouncements(ImmutableList<String> clubNames) {
    ImmutableList<String> announcements =
        Streams.stream(clubNames)
            .flatMap(
                clubName -> PrototypeClubs.PROTOTYPE_CLUBS_MAP.get(clubName).getAnnouncements().stream())
            .collect(toImmutableList());
    return announcements;
  }
}

class StudentInfo {
  private Student student;
  private ImmutableList<String> announcements;

  public StudentInfo(Student student, ImmutableList<String> announcements) {
    this.student = student;
    this.announcements = announcements;
  }
}
