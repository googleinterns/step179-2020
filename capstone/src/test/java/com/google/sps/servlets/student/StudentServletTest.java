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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public final class StudentServletTest {
  private static final String STUDENT = "student";
  private static final String ANNOUNCEMENTS = "announcements";
  public static final String MEGHA_EMAIL = "kakm@google.com";
  public static final String KEVIN_EMAIL = "kshao@google.com";
  public static final String MEGAN_EMAIL = "meganshi@google.com";
  public static final String MEGAN_NAME = "Megan Shi";
  public static final String MEGHA_NAME = "Megha Kak";
  public static final int YEAR_2022 = 2022;
  public static final String MAJOR = "Computer Science";
  public static final String CLUB_1 = "Club 1";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private StudentServlet studentServlet = new StudentServlet();
  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    localHelper.setUp();
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
    localHelper.setEnvEmail(KEVIN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_EMAIL)).thenReturn(KEVIN_EMAIL);

    JsonObject responseJson = doGet_studentServletResponse();
    JsonObject responseStudent = responseJson.get(STUDENT).getAsJsonObject();

    String responseName = responseStudent.get(Constants.PROPERTY_NAME).getAsString();
    String responseEmail = responseStudent.get(Constants.PROPERTY_EMAIL).getAsString();
    String responseMajor = responseStudent.get(Constants.PROPERTY_MAJOR).getAsString();
    int responseYear = responseStudent.get(Constants.PROPERTY_GRADYEAR).getAsInt();
    ImmutableList responseClubs =
        ImmutableList.copyOf(responseStudent.get(Constants.PROPERTY_CLUBS).getAsJsonArray());

    Assert.assertEquals("First Last", responseName);
    Assert.assertEquals(KEVIN_EMAIL, responseEmail);
    Assert.assertEquals(0, responseYear);
    Assert.assertEquals("", responseMajor);
    Assert.assertEquals(ImmutableList.of(), responseClubs);
  }

  @Test
  public void doGet_studentIsInNoClubs() throws ServletException, IOException {
    Entity studentMegha = new Entity(MEGHA_EMAIL);
    studentMegha.setProperty(Constants.PROPERTY_NAME, MEGHA_NAME);
    studentMegha.setProperty(Constants.PROPERTY_EMAIL, MEGHA_EMAIL);
    studentMegha.setProperty(Constants.PROPERTY_GRADYEAR, YEAR_2022);
    studentMegha.setProperty(Constants.PROPERTY_MAJOR, MAJOR);
    studentMegha.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());
    datastore.put(studentMegha);

    localHelper.setEnvEmail(MEGHA_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_EMAIL)).thenReturn(MEGHA_EMAIL);

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
    Entity studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_NAME, MEGAN_NAME);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_GRADYEAR, YEAR_2022);
    studentMegan.setProperty(Constants.PROPERTY_MAJOR, MAJOR);
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1));
    datastore.put(studentMegan);

    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_EMAIL)).thenReturn(MEGAN_EMAIL);

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
    datastore.put(announcementEntity);

    Entity studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_NAME, MEGAN_NAME);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_GRADYEAR, YEAR_2022);
    studentMegan.setProperty(Constants.PROPERTY_MAJOR, MAJOR);
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1));
    datastore.put(studentMegan);

    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_EMAIL)).thenReturn(MEGAN_EMAIL);

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

    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_NAME), responseName);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_EMAIL), responseEmail);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_GRADYEAR), responseYear);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_MAJOR), responseMajor);
    Assert.assertEquals(studentMegan.getProperty(Constants.PROPERTY_CLUBS), responseClubs);
    Assert.assertEquals(expectedAnnouncement, responseAnnouncement);
  }

  @Test
  public void doPost_studentIsLoggedIn() throws ServletException, IOException {
    Entity studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_NAME, MEGAN_NAME);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_GRADYEAR, YEAR_2022);
    studentMegan.setProperty(Constants.PROPERTY_MAJOR, MAJOR);
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1));
    datastore.put(studentMegan);

    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    String response = doPost_studentServletResponse();

    Assert.assertTrue(response.isEmpty());
  }

  private String getAnnouncement(String announcementContent) {
    // Get announcement from test Datastore based on content
    Query query =
        new Query(Constants.ANNOUNCEMENT_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.CONTENT_PROP, FilterOperator.EQUAL, announcementContent));
    PreparedQuery results = datastore.prepare(query);
    Entity announcement = ImmutableList.copyOf(results.asIterable()).get(0);

    // Format current time to match pattern in Datastore
    TimeZone timePST = TimeZone.getTimeZone("PST");
    DateFormat formatDate = new SimpleDateFormat("HH:mm MM-dd-yyyy");
    formatDate.setTimeZone(timePST);
    String time = formatDate.format(new Date(System.currentTimeMillis()));

    String fullAnnouncement =
        String.format(
            "%1$s from %2$s in %3$s sent at %4$s",
            announcement.getProperty(Constants.CONTENT_PROP),
            announcement.getProperty(Constants.AUTHOR_PROP),
            announcement.getProperty(Constants.CLUB_PROP),
            time);
    return fullAnnouncement;
  }
}
