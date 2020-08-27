package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.services.gmail.Gmail;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.sps.gmail.EmailFactory;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a student's profile content */
@WebServlet("/student-data")
public class StudentServlet extends AbstractAppEngineAuthorizationCodeServlet {
  private static String TIMEZONE_PST = "PST";

  // Add Gmail service for testing purposes only
  private static Gmail service;

  public StudentServlet(Gmail service) {
    this.service = service;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get student object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    AnnouncementsSweeper.sweepAnnouncements();
    Entity currentStudent = getStudent(userEmail, datastore);
    if (currentStudent == null) {
      return;
    }

    String profilePictureKey = "";
    if (currentStudent.getProperty(Constants.PROFILE_PIC_PROP) != null) {
      profilePictureKey = currentStudent.getProperty(Constants.PROFILE_PIC_PROP).toString();
    }

    // Get club list from entity and convert to an ImmutableList
    // Initally empty in case there is no club list
    ImmutableList<String> clubs = ImmutableList.of();
    if (currentStudent.getProperty(Constants.PROPERTY_CLUBS) != null) {
      clubs =
          ImmutableList.copyOf(
              (ArrayList<String>) currentStudent.getProperty(Constants.PROPERTY_CLUBS));
    }

    // Create Student object based on stored information
    Student student =
        new Student(
            currentStudent.getProperty(Constants.PROPERTY_NAME).toString(),
            Integer.parseInt(currentStudent.getProperty(Constants.PROPERTY_GRADYEAR).toString()),
            currentStudent.getProperty(Constants.PROPERTY_MAJOR).toString(),
            currentStudent.getProperty(Constants.PROPERTY_EMAIL).toString(),
            clubs,
            profilePictureKey);

    ImmutableList<String> announcements = getAllAnnouncements(clubs, datastore);
    StudentInfo allInfo = new StudentInfo(student, announcements);
    String studentJson = convertToJsonUsingGson(allInfo);

    response.setContentType("application/json; charset=utf-8");
    response.getWriter().println(studentJson);
  }

  private static String convertToJsonUsingGson(StudentInfo info) {
    Gson gson = new Gson();
    String json = gson.toJson(info);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get student object based on the logged in email
    String userEmail = request.getUserPrincipal().getName();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity student = getStudent(userEmail, datastore);

    String clubToJoin = request.getParameter(Constants.JOIN_CLUB_PROP);
    String clubToRemove = request.getParameter(Constants.LEAVE_CLUB_PROP);

    if (clubToJoin != null && !clubToJoin.isEmpty()) {
      // Add member to club's member list and update Datastore
      Entity club = retrieveClub(clubToJoin, datastore, response);
      if (club == null) {
        return;
      }
      club = ServletUtil.addItemToEntity(club, userEmail, Constants.MEMBER_PROP);
      datastore.put(club);

      // Add new club to student's club list and update Datastore
      student = ServletUtil.addItemToEntity(student, clubToJoin, Constants.PROPERTY_CLUBS);
      datastore.put(student);
      response.sendRedirect("/about-us.html?name=" + club.getProperty(Constants.PROPERTY_NAME));
    } else if (clubToRemove != null && !clubToRemove.isEmpty()) {
      // Remove member from club's member list and update Datastore
      Entity club = retrieveClub(clubToRemove, datastore, response);
      if (club == null) {
        return;
      }
      club = ServletUtil.removeItemFromEntity(club, userEmail, Constants.MEMBER_PROP);
      club = ServletUtil.removeItemFromEntity(club, userEmail, Constants.OFFICER_PROP);
      datastore.put(club);

      // Remove club from student's club list and update Datastore
      student = ServletUtil.removeItemFromEntity(student, clubToRemove, Constants.PROPERTY_CLUBS);
      datastore.put(student);
      response.sendRedirect("/profile.html");
    } else {
      response.sendRedirect("/profile.html");
    }

    // Update student information with edited content
    String newGradYear = request.getParameter(Constants.NEW_YEAR_PROP);
    String newMajor = request.getParameter(Constants.NEW_MAJOR_PROP);
    String newName = request.getParameter(Constants.NEW_NAME_PROP);
    updateStudentInDatastore(student, newGradYear, Constants.PROPERTY_GRADYEAR, datastore);
    updateStudentInDatastore(student, newMajor, Constants.PROPERTY_MAJOR, datastore);
    updateStudentInDatastore(student, newName, Constants.PROPERTY_NAME, datastore);
  }

  private static void updateStudentInDatastore(
      Entity student, String newItem, String property, DatastoreService datastore) {
    if (newItem != null && !newItem.isEmpty()) {
      student.setProperty(property, newItem);
      datastore.put(student);
    }
  }

  public static Entity getStudent(String userEmail, DatastoreService datastore) throws IOException {
    // Get the user's information from Datastore
    Query query = new Query(userEmail);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());

    // Add user to Datastore if this is the first time they login
    if (students.isEmpty()) {
      Entity studentEntity = createStudentEntity(userEmail);
      datastore.put(studentEntity);
      if (service != null) {
        EmailFactory testEmailFactory = new EmailFactory(service);
        testEmailFactory.sendWelcomeEmail(userEmail);
      } else {
        EmailFactory.sendWelcomeEmail(userEmail);
      }
      results = datastore.prepare(query);
      students = ImmutableList.copyOf(results.asIterable());
    }
    // A user can only be logged in with one email address at a time
    return students.get(0);
  }

  private static Entity createStudentEntity(String userEmail) {
    Entity studentEntity = new Entity(userEmail);
    studentEntity.setProperty(Constants.PROPERTY_NAME, "First Last");
    studentEntity.setProperty(Constants.PROPERTY_EMAIL, userEmail);
    studentEntity.setProperty(Constants.PROPERTY_GRADYEAR, 0);
    studentEntity.setProperty(Constants.PROPERTY_MAJOR, "Enter your major here");
    studentEntity.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());
    studentEntity.setProperty(Constants.PROFILE_PIC_PROP, "");
    studentEntity.setProperty(Constants.INTERESTED_CLUB_PROP, ImmutableList.of());
    return studentEntity;
  }

  private ImmutableList<String> getAllAnnouncements(
      ImmutableList<String> clubNames, DatastoreService datastore) {
    ImmutableList<String> announcements =
        Streams.stream(clubNames)
            .flatMap(clubName -> getClubAnnouncements(clubName, datastore).stream())
            .collect(toImmutableList());
    return announcements;
  }

  private ImmutableList<String> getClubAnnouncements(String clubName, DatastoreService datastore) {
    // Get all announcements from given club name in reverse chronological order
    Query query =
        new Query(Constants.ANNOUNCEMENT_PROP)
            .setFilter(new FilterPredicate(Constants.CLUB_PROP, FilterOperator.EQUAL, clubName));
    PreparedQuery results = datastore.prepare(query);

    // Stream through results and get formatted announcements
    ImmutableList<String> announcements =
        Streams.stream(results.asIterable())
            .map(StudentServlet::getAnnouncementAsString)
            .collect(toImmutableList())
            .reverse();
    return announcements;
  }

  public static String getAnnouncementAsString(Entity announcement) {
    // Set calendar timezone and time
    TimeZone timePST = TimeZone.getTimeZone(TIMEZONE_PST);
    Calendar calendar = Calendar.getInstance(timePST);
    calendar.setTimeInMillis(
        Long.parseLong(announcement.getProperty(Constants.TIME_PROP).toString()));

    // Get formatted date and time
    DateFormat formatDate = new SimpleDateFormat("HH:mm MM-dd-yyyy");
    formatDate.setTimeZone(timePST);
    String time = formatDate.format(calendar.getTime());

    String fullAnnouncement =
        String.format(
            "%1$s from %2$s in %3$s sent at %4$s",
            announcement.getProperty(Constants.CONTENT_PROP),
            ServletUtil.getNameByEmail(announcement.getProperty(Constants.AUTHOR_PROP).toString()),
            announcement.getProperty(Constants.CLUB_PROP),
            time);
    return fullAnnouncement;
  }

  private Entity retrieveClub(
      String clubName, DatastoreService datastore, HttpServletResponse response) {
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> clubs = ImmutableList.copyOf(results.asIterable());
    if (clubs.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }
    return clubs.get(0);
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return ServletUtil.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return ServletUtil.newFlow();
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
