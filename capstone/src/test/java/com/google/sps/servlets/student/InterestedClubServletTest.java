package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public final class InterestedClubServletTest {
  public static final String MEGHA_EMAIL = "kakm@google.com";
  public static final String MEGAN_EMAIL = "meganshi@google.com";
  public static final String CLUB_1 = "Club 1";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  private InterestedClubServlet interestedClubServlet = new InterestedClubServlet();
  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Entity studentMegan;
  private Entity studentMegha;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    localHelper.setUp();

    studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    studentMegan.setProperty(Constants.INTERESTED_CLUB_PROP, ImmutableList.of(CLUB_1));

    studentMegha = new Entity(MEGHA_EMAIL);
    studentMegha.setProperty(Constants.PROPERTY_EMAIL, MEGHA_EMAIL);
    studentMegha.setProperty(Constants.INTERESTED_CLUB_PROP, ImmutableList.of());
  }

  @After
  public void tearDown() throws Exception {
    localHelper.tearDown();
  }

  private JsonElement doGet_interestedClubServletResponse() throws ServletException, IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    interestedClubServlet.doGet(request, response);

    String responseStr = stringWriter.getBuffer().toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);
    return responseJsonElement;
  }

  @Test
  public void doGet_studentHasNoInterestedClubs() throws ServletException, IOException {
    datastore.put(studentMegha);
    localHelper.setEnvEmail(MEGHA_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGHA_EMAIL);

    // Get JSON response and convert to an ImmutableList
    JsonElement responseJson = doGet_interestedClubServletResponse();
    ImmutableList<String> responseClubs =
        Streams.stream(responseJson.getAsJsonArray())
            .map(Object::toString)
            .collect(toImmutableList());

    Assert.assertEquals(studentMegha.getProperty(Constants.INTERESTED_CLUB_PROP), responseClubs);
  }

  @Test
  public void doGet_studentHasInterestedClubs() throws ServletException, IOException {
    datastore.put(studentMegan);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    // Get JSON response and convert to an ImmutableList
    JsonElement responseJson = doGet_interestedClubServletResponse();
    ImmutableList<String> responseClubs =
        Streams.stream(responseJson.getAsJsonArray())
            .map(
                club ->
                    club.toString()
                        .replaceAll("\"", "")) // Remove additional quotation marks from JSON
            .collect(toImmutableList());

    Assert.assertEquals(studentMegan.getProperty(Constants.INTERESTED_CLUB_PROP), responseClubs);
  }

  @Test
  public void doPost_studentClicksInterested() throws ServletException, IOException {
    datastore.put(studentMegha);
    localHelper.setEnvEmail(MEGHA_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.INTERESTED_JOIN_PROP)).thenReturn(CLUB_1);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGHA_EMAIL);

    interestedClubServlet.doPost(request, response);

    // Access local Datastore to get student's new interested club list
    Query query = new Query(MEGHA_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    Entity student = results.asSingleEntity();
    Assert.assertTrue(student != null);
    ImmutableList<String> interestedClubList =
        ServletUtil.getPropertyList(student, Constants.INTERESTED_CLUB_PROP);
    Assert.assertFalse(interestedClubList.isEmpty());
    String interestedClub = interestedClubList.get(0);

    Assert.assertEquals(1, interestedClubList.size());
    Assert.assertEquals(CLUB_1, interestedClub);
    Mockito.verify(response).sendRedirect("/about-us.html?name=" + interestedClub);
  }

  @Test
  public void doPost_studentLeavesInterestedClub() throws ServletException, IOException {
    datastore.put(studentMegan);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.INTERESTED_LEAVE_PROP)).thenReturn(CLUB_1);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    interestedClubServlet.doPost(request, response);

    // Access local Datastore to get student's updated interested club list
    Query query = new Query(MEGAN_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    Entity student = results.asSingleEntity();
    Assert.assertTrue(student != null);
    ImmutableList<String> interestedClubList =
        ServletUtil.getPropertyList(student, Constants.INTERESTED_CLUB_PROP);

    Assert.assertEquals(ImmutableList.of(), interestedClubList);
    Mockito.verify(response).sendRedirect("/profile.html");
  }
}
