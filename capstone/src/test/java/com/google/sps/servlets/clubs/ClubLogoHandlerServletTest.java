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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.List;
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
public class ClubLogoHandlerServletTest {
  private final String SAMPLE_CLUB_NAME = "Test Club";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private BlobstoreService blobstore;
  private DatastoreService datastore;
  private ClubLogoHandlerServlet clubLogoHandlerServlet;

  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    localHelper.setUp();
    MockitoAnnotations.initMocks(this);
    blobstore = Mockito.mock(BlobstoreService.class);
    datastore = DatastoreServiceFactory.getDatastoreService();
    clubLogoHandlerServlet = new ClubLogoHandlerServlet();
  }

  @After
  public void tearDown() {
    localHelper.tearDown();
  }

  @Test
  public void doPost_uploadClubLogo() throws IOException {
    prepClubEnv();
    String sampleBlob = "test blob";
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.LOGO_PROP)).thenReturn(sampleBlob);

    ImmutableList<BlobKey> keys = ImmutableList.of(new BlobKey(sampleBlob));
    ImmutableMap<String, List<BlobKey>> blobMap = ImmutableMap.of(Constants.LOGO_PROP, keys);
    when(blobstore.getUploads(request)).thenReturn(blobMap);
    clubLogoHandlerServlet.doPostHelper(request, response, blobstore, datastore);
    Query query =
        new Query("Club")
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    Assert.assertEquals(sampleBlob, clubEntity.getProperty(Constants.LOGO_PROP));
  }

  private void prepClubEnv() throws IOException {
    String email = "meganshi@google.com";
    localHelper.setEnvEmail(email).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    Entity clubEntity = new Entity("Club");
    clubEntity.setProperty(Constants.PROPERTY_NAME, SAMPLE_CLUB_NAME);
    clubEntity.setProperty(Constants.DESCRIP_PROP, "Test club description");
    clubEntity.setProperty(Constants.WEBSITE_PROP, "www.test-club.com");
    clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(email));
    clubEntity.setProperty(
        Constants.MEMBER_PROP, ImmutableList.of(email, "kakm@google.com", "kshao@google.com"));
    datastore.put(clubEntity);
  }
}
