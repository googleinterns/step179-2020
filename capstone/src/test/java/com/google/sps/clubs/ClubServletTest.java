package com.google.sps.servlets;

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
import java.io.IOException;
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
  private final String SAMPLE_CLUB_DESC_2 = "Another club description";
  private final String SAMPLE_CLUB_WEB = "www.test-club.com";
  private final BlobKey SAMPLE_BLOB = new BlobKey("test-blobkey");
  private final String TEST_EMAIL = "test-email@gmail.com";
  private final ImmutableList<String> STUDENT_LIST = ImmutableList.of(TEST_EMAIL);
  private final String VALID_URL = "/registration-msg.html?is-valid=true";
  private final String INVALID_URL = "/registration-msg.html?is-valid=false";

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
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);

    ImmutableList<BlobKey> keys = ImmutableList.of(SAMPLE_BLOB);
    ImmutableMap<String, List<BlobKey>> blobMap = ImmutableMap.of(Constants.LOGO_PROP, keys);
    when(blobstore.getUploads(request)).thenReturn(blobMap);

    clubServlet.doPostHelper(request, response, blobstore, datastore);

    Query query =
        new Query("Club")
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();

    Assert.assertEquals(SAMPLE_CLUB_NAME, clubEntity.getProperty(Constants.PROPERTY_NAME));
    Assert.assertEquals(SAMPLE_CLUB_DESC_1, clubEntity.getProperty(Constants.DESCRIP_PROP));
    Assert.assertEquals(SAMPLE_CLUB_WEB, clubEntity.getProperty(Constants.WEBSITE_PROP));
    Assert.assertEquals(SAMPLE_BLOB, clubEntity.getProperty(Constants.LOGO_PROP));
    Assert.assertEquals(STUDENT_LIST, clubEntity.getProperty(Constants.MEMBER_PROP));
    Assert.assertEquals(STUDENT_LIST, clubEntity.getProperty(Constants.OFFICER_PROP));

    Mockito.verify(response).sendRedirect(VALID_URL);
  }

  @Test
  public void doPost_registerNewInvalidClub() throws ServletException, IOException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);

    ImmutableList<BlobKey> keys = ImmutableList.of(SAMPLE_BLOB);
    ImmutableMap<String, List<BlobKey>> blobMap = ImmutableMap.of(Constants.LOGO_PROP, keys);
    when(blobstore.getUploads(request)).thenReturn(blobMap);

    clubServlet.doPostHelper(request, response, blobstore, datastore);

    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_2);
    clubServlet.doPostHelper(request, response, blobstore, datastore);

    Mockito.verify(response).sendRedirect(INVALID_URL);
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
}
