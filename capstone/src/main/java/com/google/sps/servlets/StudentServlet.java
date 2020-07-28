package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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
  private static final String PROPERTY_NAME = "name";
  private static final String PROPERTY_EMAIL = "email";
  private static final String PROPERTY_GRADYEAR = "gradYear";
  private static final String PROPERTY_MAJOR = "major";
  private static final String PROPERTY_CLUBS = "clubs";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get student object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();

    // Get the user's information from Datastore
    Query query = new Query(userEmail);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    if (students.isEmpty()) {
      return;
    }
    // A user can only be logged in with one email address at a time
    Entity currentStudent = students.get(0);

    // Get club list from entities and convert to an ImmutableList - initally empty in case there is
    // no club list
    ImmutableList<String> clubs = ImmutableList.of();
    if (currentStudent.getProperty(PROPERTY_CLUBS) != null) {
      String clubsAsString = currentStudent.getProperty(PROPERTY_CLUBS).toString();
      // Convert string representation of a list to an ImmutableList
      clubs =
          ImmutableList.copyOf(clubsAsString.substring(1, clubsAsString.length() - 1).split(","));
    }

    // Create Student object based on stored information
    Student student =
        new Student(
            currentStudent.getProperty(PROPERTY_NAME).toString(),
            Integer.parseInt(currentStudent.getProperty(PROPERTY_GRADYEAR).toString()),
            currentStudent.getProperty(PROPERTY_MAJOR).toString(),
            currentStudent.getProperty(PROPERTY_EMAIL).toString(),
            clubs);

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
    // 1. Add club to logged in student's club list if requested
    // 2. Remove club from logged in student's club list if requested
    // 3. Update student information with edited content

    // Get student object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();

    // Add user to Datastore if not already stored
    Query query = new Query(userEmail);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    if (Streams.stream(results.asIterable()).count() == 0) {
      Entity studentEntity = new Entity(userEmail);
      studentEntity.setProperty(PROPERTY_NAME, "First Last");
      studentEntity.setProperty(PROPERTY_EMAIL, userEmail);
      studentEntity.setProperty(PROPERTY_GRADYEAR, "0");
      studentEntity.setProperty(PROPERTY_MAJOR, "");
      studentEntity.setProperty(PROPERTY_CLUBS, ImmutableList.of());
      datastore.put(studentEntity);
    }

    response.sendRedirect("/profile.html");
  }

  public ImmutableList<String> getAllAnnouncements(ImmutableList<String> clubNames) {
    // TODO: Get announcements from Datastore once announcements have been loaded into Datastore
    ImmutableList<String> announcements =
        Streams.stream(clubNames)
            .flatMap(
                clubName ->
                    PrototypeClubs.PROTOTYPE_CLUBS_MAP.get(clubName).getAnnouncements().stream())
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
