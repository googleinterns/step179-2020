package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public final class StudentServletTest {
  private static final String STUDENT = "student";
  private static final String ANNOUNCEMENTS = "announcements";
  public static final String MEGHA_EMAIL = "kakm@google.com";
  public static final String MEGAN_EMAIL = "meganshi@google.com";
  public static final String TEST_EMAIL = "test@example.com";
  public static final String MEGAN_NAME = "Megan Shi";
  public static final String MEGHA_NAME = "Megha Kak";
  public static final int YEAR_2022 = 2022;
  public static final String MAJOR = "Computer Science";
  public static final String CLUB_1 = "Club 1";
  public static final String CLUB_2 = "Club 2";
  public static final String CLUB_3 = "Club 3";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  private StudentServlet studentServlet = new StudentServlet();
  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Entity studentMegan;
  private Entity studentMegha;
  private Entity club1;
  private Entity club2;
  private Entity club3;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    localHelper.setUp();

    studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_NAME, MEGAN_NAME);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_GRADYEAR, YEAR_2022);
    studentMegan.setProperty(Constants.PROPERTY_MAJOR, MAJOR);
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1));

    studentMegha = new Entity(MEGHA_EMAIL);
    studentMegha.setProperty(Constants.PROPERTY_NAME, MEGHA_NAME);
    studentMegha.setProperty(Constants.PROPERTY_EMAIL, MEGHA_EMAIL);
    studentMegha.setProperty(Constants.PROPERTY_GRADYEAR, YEAR_2022);
    studentMegha.setProperty(Constants.PROPERTY_MAJOR, MAJOR);
    studentMegha.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());

    club1 = new Entity("Club");
    club1.setProperty(Constants.PROPERTY_NAME, CLUB_1);
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGHA_EMAIL));
    club1.setProperty(Constants.EXCLUSIVE_PROP, false);

    club2 = new Entity("Club");
    club2.setProperty(Constants.PROPERTY_NAME, CLUB_2);
    club2.setProperty(Constants.MEMBER_PROP, ImmutableList.of(TEST_EMAIL));
    club2.setProperty(Constants.EXCLUSIVE_PROP, false);

    club3 = new Entity("Club");
    club3.setProperty(Constants.PROPERTY_NAME, CLUB_3);
    club3.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGAN_EMAIL));
    club3.setProperty(Constants.EXCLUSIVE_PROP, true);
    club3.setProperty(Constants.REQUEST_PROP, ImmutableList.of(TEST_EMAIL));
  }

  @After
  public void tearDown() throws Exception {
    localHelper.tearDown();
  }

  private JsonObject doGet_studentServletResponse() throws ServletException, IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    studentServlet.doGet(request, response);

    String responseStr = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);
    JsonObject responseJson = responseJsonElement.getAsJsonObject();

    return responseJson;
  }

  private String doPost_studentServletResponse() throws ServletException, IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    studentServlet.doPost(request, response);
    return stringWriter.toString();
  }

  @Test
  public void doGet_studentLogsInForFirstTime() throws ServletException, IOException {
    localHelper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("example.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    // Tests for sending emails is done in EmailFactoryTest.java so we can ignore the welcome email
    // here
    when(request.getParameter(Constants.SERVICE_PROP)).thenReturn("do not test gmail service here");

    JsonObject responseJson = doGet_studentServletResponse();
    JsonObject responseStudent = responseJson.get(STUDENT).getAsJsonObject();

    String responseName = responseStudent.get(Constants.PROPERTY_NAME).getAsString();
    String responseEmail = responseStudent.get(Constants.PROPERTY_EMAIL).getAsString();
    String responseMajor = responseStudent.get(Constants.PROPERTY_MAJOR).getAsString();
    int responseYear = responseStudent.get(Constants.PROPERTY_GRADYEAR).getAsInt();
    ImmutableList responseClubs =
        ImmutableList.copyOf(responseStudent.get(Constants.PROPERTY_CLUBS).getAsJsonArray());

    Assert.assertEquals("First Last", responseName);
    Assert.assertEquals(TEST_EMAIL, responseEmail);
    Assert.assertEquals(0, responseYear);
    Assert.assertEquals("Enter your major here", responseMajor);
    Assert.assertEquals(ImmutableList.of(), responseClubs);
  }

  @Test
  public void doGet_studentIsInNoClubs() throws ServletException, IOException {
    datastore.put(studentMegha);

    localHelper.setEnvEmail(MEGHA_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGHA_EMAIL);

    JsonObject responseJson = doGet_studentServletResponse();
    JsonObject responseStudent = responseJson.get(STUDENT).getAsJsonObject();

    String responseName = responseStudent.get(Constants.PROPERTY_NAME).getAsString();
    String responseEmail = responseStudent.get(Constants.PROPERTY_EMAIL).getAsString();
    String responseMajor = responseStudent.get(Constants.PROPERTY_MAJOR).getAsString();
    int responseYear = responseStudent.get(Constants.PROPERTY_GRADYEAR).getAsInt();
    ImmutableList responseClubs =
        ImmutableList.copyOf(responseStudent.get(Constants.PROPERTY_CLUBS).getAsJsonArray());

    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_NAME), responseName);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_EMAIL), responseEmail);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_GRADYEAR), responseYear);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_MAJOR), responseMajor);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_CLUBS), responseClubs);
  }

  @Test
  public void doGet_studentIsInOneClub() throws ServletException, IOException {
    datastore.put(studentMegan);

    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    JsonObject responseJson = doGet_studentServletResponse();
    JsonObject responseStudent = responseJson.get(STUDENT).getAsJsonObject();

    String responseName = responseStudent.get(Constants.PROPERTY_NAME).getAsString();
    String responseEmail = responseStudent.get(Constants.PROPERTY_EMAIL).getAsString();
    String responseMajor = responseStudent.get(Constants.PROPERTY_MAJOR).getAsString();
    int responseYear = responseStudent.get(Constants.PROPERTY_GRADYEAR).getAsInt();
    // Remove additional quotation marks from JSON Array and convert to ImmutableList
    ImmutableList responseClubs =
        Streams.stream(responseStudent.get(Constants.PROPERTY_CLUBS).getAsJsonArray())
            .map(club -> club.toString().replaceAll("\"", ""))
            .collect(toImmutableList());

    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_NAME), responseName);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_EMAIL), responseEmail);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_GRADYEAR), responseYear);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_MAJOR), responseMajor);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_CLUBS), responseClubs);
  }

  @Test
  public void doGet_studentIsInOneClubWithAnnouncement() throws ServletException, IOException {
    String announcementContent = "This is a test announcement";
    Entity announcementEntity = new Entity(Constants.ANNOUNCEMENT_PROP);
    announcementEntity.setProperty(Constants.AUTHOR_PROP, MEGHA_EMAIL);
    announcementEntity.setProperty(Constants.TIME_PROP, System.currentTimeMillis());
    announcementEntity.setProperty(Constants.CONTENT_PROP, announcementContent);
    announcementEntity.setProperty(Constants.CLUB_PROP, CLUB_1);
    studentMegha.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1));
    datastore.put(announcementEntity);
    datastore.put(studentMegha);

    localHelper.setEnvEmail(MEGHA_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGHA_EMAIL);

    JsonObject responseJson = doGet_studentServletResponse();
    JsonObject responseStudent = responseJson.get(STUDENT).getAsJsonObject();

    // Get announcement and remove list brackets and quotes
    String responseAnnouncement = responseJson.get(ANNOUNCEMENTS).toString();
    responseAnnouncement =
        responseAnnouncement.substring(1, responseAnnouncement.length() - 1).replaceAll("\"", "");

    String responseName = responseStudent.get(Constants.PROPERTY_NAME).getAsString();
    String responseEmail = responseStudent.get(Constants.PROPERTY_EMAIL).getAsString();
    String responseMajor = responseStudent.get(Constants.PROPERTY_MAJOR).getAsString();
    int responseYear = responseStudent.get(Constants.PROPERTY_GRADYEAR).getAsInt();
    // Remove additional quotation marks from JSON Array and convert to ImmutableList
    ImmutableList responseClubs =
        Streams.stream(responseStudent.get(Constants.PROPERTY_CLUBS).getAsJsonArray())
            .map(club -> club.toString().replaceAll("\"", ""))
            .collect(toImmutableList());

    String expectedAnnouncement = getAnnouncement(announcementContent);

    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_NAME), responseName);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_EMAIL), responseEmail);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_GRADYEAR), responseYear);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_MAJOR), responseMajor);
    Assert.assertEquals(studentMegha.getProperty(Constants.PROPERTY_CLUBS), responseClubs);
    Assert.assertEquals(expectedAnnouncement, responseAnnouncement);
  }

  @Test
  public void doPost_studentIsLoggedIn() throws ServletException, IOException {
    datastore.put(studentMegan);

    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);
    String responseStr = doPost_studentServletResponse();

    Assert.assertTrue(responseStr.isEmpty());
    Mockito.verify(response).sendRedirect("/profile.html");
  }

  @Test
  public void doPost_studentClicksJoinClub() throws ServletException, IOException {
    datastore.put(club2);
    datastore.put(studentMegha);
    localHelper.setEnvEmail(MEGHA_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.JOIN_CLUB_PROP)).thenReturn(CLUB_2);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGHA_EMAIL);

    String responseStr = doPost_studentServletResponse();

    // Access local Datastore to get student's new club list
    Query query = new Query(MEGHA_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(students.isEmpty());
    Entity student = students.get(0);
    ImmutableList<String> clubList =
        ImmutableList.copyOf((ArrayList<String>) student.getProperty(Constants.PROPERTY_CLUBS));
    Assert.assertFalse(clubList.isEmpty());
    String club = clubList.get(0);

    Assert.assertEquals(1, clubList.size());
    Assert.assertEquals(CLUB_2, club);
    Mockito.verify(response).sendRedirect("/about-us.html?name=" + club);
  }

  @Test
  public void doPost_studentJoinsClubTheyAreAlreadyIn() throws ServletException, IOException {
    datastore.put(club1);
    datastore.put(studentMegan);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.JOIN_CLUB_PROP)).thenReturn(CLUB_1);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    String responseStr = doPost_studentServletResponse();

    // Access local Datastore to get student's new club list
    Query query = new Query(MEGAN_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(students.isEmpty());
    Assert.assertTrue(students.size() == 1);
    Entity student = students.get(0);
    ImmutableList<String> clubList =
        ImmutableList.copyOf((ArrayList<String>) student.getProperty(Constants.PROPERTY_CLUBS));

    Assert.assertEquals(1, clubList.size());
    Assert.assertEquals(CLUB_1, clubList.get(0));
  }

  private String getAnnouncement(String announcementContent) {
    // Get announcement from test Datastore based on content
    Query query =
        new Query(Constants.ANNOUNCEMENT_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.CONTENT_PROP, FilterOperator.EQUAL, announcementContent));
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> announcements = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(announcements.isEmpty());
    Entity announcement = announcements.get(0);

    // Format current time to match pattern in Datastore
    TimeZone timePST = TimeZone.getTimeZone("PST");
    DateFormat formatDate = new SimpleDateFormat("HH:mm MM-dd-yyyy");
    formatDate.setTimeZone(timePST);
    String time =
        formatDate.format(
            new Date(Long.parseLong(announcement.getProperty(Constants.TIME_PROP).toString())));

    String fullAnnouncement =
        String.format(
            "%1$s from %2$s in %3$s sent at %4$s",
            announcement.getProperty(Constants.CONTENT_PROP),
            ServletUtil.getNameByEmail(announcement.getProperty(Constants.AUTHOR_PROP).toString()),
            announcement.getProperty(Constants.CLUB_PROP),
            time);
    return fullAnnouncement;
  }

  @Test
  public void doPost_studentClicksLeaveButton() throws ServletException, IOException {
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1, CLUB_2));
    datastore.put(studentMegan);
    datastore.put(club1);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.LEAVE_CLUB_PROP)).thenReturn(CLUB_1);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    String responseStr = doPost_studentServletResponse();

    // Access local Datastore to get student's new club list
    Query query = new Query(MEGAN_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(students.isEmpty());
    Assert.assertTrue(students.size() == 1);
    Entity student = students.get(0);
    ImmutableList<String> clubList =
        ImmutableList.copyOf((ArrayList<String>) student.getProperty(Constants.PROPERTY_CLUBS));

    Assert.assertEquals(1, clubList.size());
    Assert.assertEquals(CLUB_2, clubList.get(0));
    Mockito.verify(response).sendRedirect("/profile.html");
  }

  @Test
  public void doPost_studentClicksLeaveButtonWithOnlyOneClub()
      throws ServletException, IOException {
    datastore.put(studentMegan);
    datastore.put(club1);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.LEAVE_CLUB_PROP)).thenReturn(CLUB_1);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    String responseStr = doPost_studentServletResponse();

    // Access local Datastore to get student's new club list
    Query query = new Query(MEGAN_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(students.isEmpty());
    Assert.assertTrue(students.size() == 1);
    Entity student = students.get(0);

    Assert.assertEquals(null, student.getProperty(Constants.PROPERTY_CLUBS));
    Mockito.verify(response).sendRedirect("/profile.html");
  }

  @Test
  public void doPost_studentRequestsToExclusiveJoinClub() throws ServletException, IOException {
    datastore.put(studentMegan);
    datastore.put(club3);
    localHelper.setEnvEmail(MEGHA_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGHA_EMAIL);
    when(request.getParameter(Constants.JOIN_CLUB_PROP)).thenReturn(CLUB_3);
    when(request.getParameter(Constants.SERVICE_PROP)).thenReturn("do not test gmail service here");

    doPost_studentServletResponse();

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, CLUB_3));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();

    Assert.assertNotNull(clubEntity);
    ImmutableList<String> newRequests =
        ServletUtil.getPropertyList(clubEntity, Constants.REQUEST_PROP);
    Assert.assertEquals(2, newRequests.size());
    Assert.assertEquals(TEST_EMAIL, newRequests.get(0));
    Assert.assertEquals(MEGHA_EMAIL, newRequests.get(1));
    Mockito.verify(response).sendRedirect("/explore.html");
  }

  @Test
  public void doPost_studentRequestsToExclusiveJoinClubAlreadyMember()
      throws ServletException, IOException {
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);
    datastore.put(studentMegan);
    datastore.put(club3);
    when(request.getParameter(Constants.JOIN_CLUB_PROP)).thenReturn(CLUB_3);

    doPost_studentServletResponse();

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, CLUB_3));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();

    Assert.assertNotNull(clubEntity);
    ImmutableList<String> newRequests =
        ServletUtil.getPropertyList(clubEntity, Constants.REQUEST_PROP);
    Assert.assertEquals(1, newRequests.size());
    Assert.assertEquals(TEST_EMAIL, newRequests.get(0));
  }

  @Test
  public void doPost_studentChangesProfileContent() throws ServletException, IOException {
    String newName = "Megan";
    String newYear = "2023";
    String newMajor = "Testing";
    datastore.put(studentMegan);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter("new-name")).thenReturn(newName);
    when(request.getParameter("new-year")).thenReturn(newYear);
    when(request.getParameter("new-major")).thenReturn(newMajor);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    String responseStr = doPost_studentServletResponse();

    // Access local Datastore to get student's edited name
    Query query = new Query(MEGAN_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(students.isEmpty());
    Assert.assertTrue(students.size() == 1);
    Entity student = students.get(0);

    Assert.assertEquals(newName, student.getProperty(Constants.PROPERTY_NAME));
    Assert.assertEquals(newYear, student.getProperty(Constants.PROPERTY_GRADYEAR));
    Assert.assertEquals(newMajor, student.getProperty(Constants.PROPERTY_MAJOR));
    Mockito.verify(response).sendRedirect("/profile.html");
  }
}
