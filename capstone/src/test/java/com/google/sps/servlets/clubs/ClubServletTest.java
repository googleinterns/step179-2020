package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.mockito.Mockito.when;

import com.google.appengine.api.blobstore.BlobKey;
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
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
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
  private final ImmutableList<String> STUDENT_LIST = ImmutableList.of(TEST_EMAIL);

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private BlobstoreService blobstore;
  private ClubServlet clubServlet;
  private DatastoreService datastore;
  private BlobstoreServlet blobstoreServlet;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalUserServiceTestConfig(),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    this.clubServlet = new ClubServlet();
    this.blobstoreServlet = new BlobstoreServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
    blobstore = Mockito.mock(BlobstoreService.class);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doPost_registerNewValidClub() throws ServletException, IOException {
    doPost_helper();
    clubServlet.doPostHelper(request, response, blobstore, datastore);

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

    Mockito.verify(response).sendRedirect("/registration-msg.html?is-valid=true");
  }

  @Test
  public void doPost_registerNewInvalidClub() throws ServletException, IOException {
    doPost_helper();
    clubServlet.doPostHelper(request, response, blobstore, datastore);

    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn("club desc");
    clubServlet.doPostHelper(request, response, blobstore, datastore);

    Mockito.verify(response).sendRedirect("/registration-msg.html?is-valid=false");
    Query query =
        new Query("Club")
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
    ImmutableList<String> expectedMembers = ImmutableList.of("student@example.com", officerEmail);
    ImmutableList<String> expectedOfficers = ImmutableList.of(officerEmail);

    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP);
    clubEntity.setProperty(Constants.PROPERTY_NAME, SAMPLE_CLUB_NAME);
    clubEntity.setProperty(Constants.DESCRIP_PROP, "test description");
    clubEntity.setProperty(Constants.MEMBER_PROP, expectedMembers);
    clubEntity.setProperty(Constants.OFFICER_PROP, expectedOfficers);
    clubEntity.setProperty(Constants.WEBSITE_PROP, "website.com");
    clubEntity.setProperty(Constants.LOGO_PROP, SAMPLE_BLOB);
    datastore.put(clubEntity);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    clubServlet.doGet(request, response);

    String responseStr = stringWriter.toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);
    JsonObject response = responseJsonElement.getAsJsonObject();

    ImmutableList<String> actualMembers = convertJsonList(response.get(Constants.MEMBER_PROP));
    ImmutableList<String> actualOfficers = convertJsonList(response.get(Constants.OFFICER_PROP));

    Assert.assertEquals(SAMPLE_CLUB_NAME, response.get(Constants.PROPERTY_NAME).getAsString());
    Assert.assertEquals("test description", response.get(Constants.DESCRIP_PROP).getAsString());
    Assert.assertEquals(expectedMembers, actualMembers);
    Assert.assertEquals(expectedOfficers, actualOfficers);
    Assert.assertEquals("website.com", response.get(Constants.WEBSITE_PROP).getAsString());
  }

  @Test
  public void doGet_clubDoesNotExist() throws ServletException, IOException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn("Imaginary Club");
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

  private void doPost_helper() {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);

    ImmutableList<BlobKey> keys = ImmutableList.of(new BlobKey(SAMPLE_BLOB));
    ImmutableMap<String, List<BlobKey>> blobMap = ImmutableMap.of(Constants.LOGO_PROP, keys);
    when(blobstore.getUploads(request)).thenReturn(blobMap);
  }
}
