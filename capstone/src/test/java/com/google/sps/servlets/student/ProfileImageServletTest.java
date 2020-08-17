package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.security.Principal;
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
public class ProfileImageServletTest {
  private static final String MEGAN_EMAIL = "meganshi@google.com";
  private static final String SAMPLE_BLOB = "test-blobkey";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  private BlobstoreService blobstore;
  private DatastoreService datastore;
  private ProfileImageServlet profileImageServlet;

  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    localHelper.setUp();
    MockitoAnnotations.initMocks(this);
    blobstore = Mockito.mock(BlobstoreService.class);
    datastore = DatastoreServiceFactory.getDatastoreService();
    profileImageServlet = new ProfileImageServlet();
  }

  @After
  public void tearDown() {
    localHelper.tearDown();
  }

  @Test
  public void doPost_studentUpdatesProfilePicture() throws IOException {
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_EMAIL)).thenReturn(MEGAN_EMAIL);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);

    Entity studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    datastore.put(studentMegan);
    doPost_helper();

    // Access local Datastore to get student's uploaded blob key
    Query query = new Query(MEGAN_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(students.isEmpty());
    Assert.assertTrue(students.size() == 1);
    Entity student = students.get(0);

    Assert.assertEquals(SAMPLE_BLOB, student.getProperty(Constants.PROFILE_PIC_PROP));
  }

  private void doPost_helper() throws IOException {
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROFILE_PIC_PROP)).thenReturn(SAMPLE_BLOB);

    // Set up Blobstore with sample blob
    ImmutableList<BlobKey> keys = ImmutableList.of(new BlobKey(SAMPLE_BLOB));
    ImmutableMap<String, List<BlobKey>> blobMap = ImmutableMap.of(Constants.PROFILE_PIC_PROP, keys);
    when(blobstore.getUploads(request)).thenReturn(blobMap);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(MEGAN_EMAIL);
    profileImageServlet.doPostHelper(request, response, blobstore, datastore);
  }
}
