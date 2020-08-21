package com.google.sps.servlets;

import static org.mockito.Mockito.when;

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
import java.io.IOException;
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
public class DeleteClubServletTest {

  private final String SAMPLE_CLUB_NAME = "Test Club";
  private final String SAMPLE_CLUB_DESC_1 = "Test club description";
  private final String SAMPLE_CLUB_WEB = "www.test-club.com";
  private final String TEST_EMAIL = "test-email@gmail.com";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  private DatastoreService datastore;
  private DeleteClubServlet deleteClubServlet;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    this.deleteClubServlet = new DeleteClubServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doPost_deleteValidClub() throws ServletException, IOException {
    prepClubEnv();
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    deleteClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    Assert.assertNull(clubEntity);
  }

  @Test
  public void doPost_deleteNonexistentClub() throws ServletException, IOException {
    prepClubEnv();
    String fakeClubName = "hello";
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(fakeClubName);
    deleteClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, fakeClubName));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    Assert.assertNull(clubEntity);
    Mockito.verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }

  private void prepClubEnv() {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP, SAMPLE_CLUB_NAME);
    clubEntity.setProperty(Constants.PROPERTY_NAME, SAMPLE_CLUB_NAME);
    clubEntity.setProperty(Constants.DESCRIP_PROP, SAMPLE_CLUB_DESC_1);
    clubEntity.setProperty(Constants.WEBSITE_PROP, SAMPLE_CLUB_WEB);
    clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(TEST_EMAIL));
    clubEntity.setProperty(
        Constants.MEMBER_PROP,
        ImmutableList.of(TEST_EMAIL, "meganshi@google.com", "kakm@google.com", "kshao@google.com"));
    datastore.put(clubEntity);
  }
}
