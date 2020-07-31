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

    // Add user to Datastore if this is the first time they login
    if (students.isEmpty()) {
      Entity studentEntity = createStudentEntity(userEmail);
      datastore.put(studentEntity);
      results = datastore.prepare(query);
      students = ImmutableList.copyOf(results.asIterable());
    }
    // A user can only be logged in with one email address at a time
    Entity currentStudent = students.get(0);

    // Get club list from entity and convert to an ImmutableList
    // Initally empty in case there is no club list
    ImmutableList<String> clubs = ImmutableList.of();
    if (currentStudent.getProperty(Constants.PROPERTY_CLUBS) != null) {
      String clubsAsString = currentStudent.getProperty(Constants.PROPERTY_CLUBS).toString();
      // Convert string representation of a list to an ImmutableList
      // JsonElement clubsJson = new JsonParser().parse(clubsAsString);
      System.out.println("here: " + clubsAsString);
      //   clubs =
      //       Streams.stream(clubsJson.getAsJsonArray())
      //           .map(club -> club.toString().replaceAll("\"", ""))
      //           .collect(toImmutableList());
      //   System.out.println(clubs);
      clubs =
          ImmutableList.copyOf(clubsAsString.substring(1, clubsAsString.length() - 1).split(","));
    }

    // Create Student object based on stored information
    Student student =
        new Student(
            currentStudent.getProperty(Constants.PROPERTY_NAME).toString(),
            Integer.parseInt(currentStudent.getProperty(Constants.PROPERTY_GRADYEAR).toString()),
            currentStudent.getProperty(Constants.PROPERTY_MAJOR).toString(),
            currentStudent.getProperty(Constants.PROPERTY_EMAIL).toString(),
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

    // No need to check if user's information is not in Datastore - this is done in doGet method
    // Get student object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();

    response.sendRedirect("/profile.html");
  }

  private Entity createStudentEntity(String userEmail) {
    Entity studentEntity = new Entity(userEmail);
    studentEntity.setProperty(Constants.PROPERTY_NAME, "First Last");
    studentEntity.setProperty(Constants.PROPERTY_EMAIL, userEmail);
    studentEntity.setProperty(Constants.PROPERTY_GRADYEAR, 0);
    studentEntity.setProperty(Constants.PROPERTY_MAJOR, "");
    studentEntity.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());
    return studentEntity;
  }

  private ImmutableList<String> getAllAnnouncements(ImmutableList<String> clubNames) {
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
