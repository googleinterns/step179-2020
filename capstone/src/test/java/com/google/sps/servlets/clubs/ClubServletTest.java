package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.mockito.Mockito.when;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Calendars;
import com.google.api.services.calendar.Calendar.Calendars.Insert;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
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
import java.security.GeneralSecurityException;
import java.security.Principal;
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
public class ClubServletTest {

  private final String SAMPLE_CLUB_NAME = "Club 1";
  private final String SAMPLE_CLUB_DESC_1 = "Test club description";
  private final String SAMPLE_CLUB_WEB = "www.test-club.com";
  private final String SAMPLE_BLOB = "test-blobkey";
  private final String TEST_EMAIL = "test-email@gmail.com";
  private final String SAMPLE_CAL_ID = "Club 1 Calendar ID";
  private final long SAMPLE_TIME = 25;
  private final ImmutableList<String> STUDENT_LIST = ImmutableList.of(TEST_EMAIL);

  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Mock Principal principal;
  @Mock Calendar calendarService;
  @Mock Calendars mockCalendar;
  @Mock Insert calendarInsert;
  @Mock com.google.api.services.calendar.model.Calendar modelCalendar;
  private BlobstoreService blobstore;
  private ClubServlet clubServlet;
  private DatastoreService datastore;
  private BlobstoreServlet blobstoreServlet;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    this.clubServlet = new ClubServlet();
    this.blobstoreServlet = new BlobstoreServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doPost_registerNewValidClub()
      throws ServletException, IOException, GeneralSecurityException {
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("test-email@gmail.com");
    when(request.getParameter(Constants.EXCLUSIVE_PROP)).thenReturn(null);
    doPost_helper();

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();

    Assert.assertEquals(SAMPLE_CLUB_NAME, clubEntity.getProperty(Constants.PROPERTY_NAME));
    Assert.assertEquals(SAMPLE_CLUB_DESC_1, clubEntity.getProperty(Constants.DESCRIP_PROP));
    Assert.assertEquals(SAMPLE_CLUB_WEB, clubEntity.getProperty(Constants.WEBSITE_PROP));
    Assert.assertEquals(STUDENT_LIST, clubEntity.getProperty(Constants.MEMBER_PROP));
    Assert.assertEquals(STUDENT_LIST, clubEntity.getProperty(Constants.OFFICER_PROP));
    Assert.assertEquals(SAMPLE_CAL_ID, clubEntity.getProperty(Constants.CALENDAR_PROP));
    Assert.assertFalse((Boolean) clubEntity.getProperty(Constants.EXCLUSIVE_PROP));

    Mockito.verify(response).sendRedirect("/registration-msg.html?is-valid=true");
  }

  @Test
  public void doPost_registerNewValidExclusiveClub()
      throws ServletException, IOException, GeneralSecurityException {
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("test-email@gmail.com");
    when(request.getParameter(Constants.EXCLUSIVE_PROP)).thenReturn("on");
    doPost_helper();

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();

    Assert.assertEquals(SAMPLE_CLUB_NAME, clubEntity.getProperty(Constants.PROPERTY_NAME));
    Assert.assertTrue((Boolean) clubEntity.getProperty(Constants.EXCLUSIVE_PROP));
    Mockito.verify(response).sendRedirect("/registration-msg.html?is-valid=true");
  }

  @Test
  public void doPost_registerNewInvalidClub()
      throws ServletException, IOException, GeneralSecurityException {
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("officer@example.com");
    doPost_helper();
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn("club desc");
    doPost_helper();

    Mockito.verify(response).sendRedirect("/registration-msg.html?is-valid=false");
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    Assert.assertEquals(SAMPLE_CLUB_DESC_1, clubEntity.getProperty(Constants.DESCRIP_PROP));
  }

  @Test
  public void doGet_clubExists() throws ServletException, IOException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    String officerEmail = "officer@example.com";
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(officerEmail);
    ImmutableList<String> expectedMembers = ImmutableList.of("student@example.com", officerEmail);
    ImmutableList<String> expectedOfficers = ImmutableList.of(officerEmail);

    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP);
    clubEntity.setProperty(Constants.PROPERTY_NAME, SAMPLE_CLUB_NAME);
    clubEntity.setProperty(Constants.DESCRIP_PROP, "test description");
    clubEntity.setProperty(Constants.MEMBER_PROP, expectedMembers);
    clubEntity.setProperty(Constants.OFFICER_PROP, expectedOfficers);
    clubEntity.setProperty(Constants.WEBSITE_PROP, "website.com");
    clubEntity.setProperty(Constants.LOGO_PROP, SAMPLE_BLOB);
    clubEntity.setProperty(Constants.CALENDAR_PROP, SAMPLE_CAL_ID);
    clubEntity.setProperty(Constants.EXCLUSIVE_PROP, true);
    clubEntity.setProperty(Constants.TIME_PROP, SAMPLE_TIME);
    datastore.put(clubEntity);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    clubServlet.doGet(request, response);

    String responseStr = stringWriter.toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);
    JsonObject response = (JsonObject) responseJsonElement;
    response = response.get("club").getAsJsonObject();

    ImmutableList<String> actualMembers = convertJsonList(response.get(Constants.MEMBER_PROP));
    ImmutableList<String> actualOfficers = convertJsonList(response.get(Constants.OFFICER_PROP));

    Assert.assertEquals(SAMPLE_CLUB_NAME, response.get(Constants.PROPERTY_NAME).getAsString());
    Assert.assertEquals("test description", response.get(Constants.DESCRIP_PROP).getAsString());
    Assert.assertEquals(expectedMembers, actualMembers);
    Assert.assertEquals(expectedOfficers, actualOfficers);
    Assert.assertEquals("website.com", response.get(Constants.WEBSITE_PROP).getAsString());
    Assert.assertEquals(SAMPLE_CAL_ID, response.get(Constants.CALENDAR_PROP).getAsString());
    Assert.assertTrue(response.get(Constants.EXCLUSIVE_PROP).getAsBoolean());
    Assert.assertEquals(SAMPLE_TIME, response.get(Constants.TIME_PROP).getAsLong());
  }

  @Test
  public void doGet_clubDoesNotExist() throws ServletException, IOException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn("Imaginary Club");
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    clubServlet.doGet(request, response);
    Mockito.verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  private ImmutableList<String> convertJsonList(JsonElement responseProp) {
    ImmutableList<String> converted =
        Streams.stream(responseProp.getAsJsonArray())
            .map(element -> element.toString().replaceAll("\"", ""))
            .collect(toImmutableList());
    return converted;
  }

  private void doPost_helper() throws IOException, GeneralSecurityException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);

    com.google.api.services.calendar.model.Calendar calendar =
        new com.google.api.services.calendar.model.Calendar()
            .setSummary(SAMPLE_CLUB_NAME + " Calendar")
            .setTimeZone("America/Los_Angeles");

    when(calendarService.calendars()).thenReturn(mockCalendar);
    when(mockCalendar.insert(calendar)).thenReturn(calendarInsert);
    when(calendarInsert.execute()).thenReturn(modelCalendar);
    when(modelCalendar.getId()).thenReturn(SAMPLE_CAL_ID);

    ClubServlet servletSpy = Mockito.spy(clubServlet);
    Mockito.doReturn(SAMPLE_CAL_ID)
        .when(servletSpy)
        .createCalendar(SAMPLE_CLUB_NAME, calendarService);
    servletSpy.doPostHelper(request, response, datastore, calendarService);
  }
}
