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
import java.util.ArrayList;
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
public class EditClubServletTest {

  private final String SAMPLE_CLUB_NAME = "Test Club";
  private final String SAMPLE_CLUB_DESC_1 = "Test club description";
  private final String SAMPLE_CLUB_WEB = "www.test-club.com";
  private final String TEST_EMAIL = "test-email@gmail.com";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  private DatastoreService datastore;
  private EditClubServlet editClubServlet;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    this.editClubServlet = new EditClubServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doPost_editClubAllValidOfficers() throws ServletException, IOException {
    prepClubEnv();
    String newDescription = "new test description";
    String newWebsite = "new-website.com";
    String newOfficer = "kakm@google.com";
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn(TEST_EMAIL + "," + newOfficer);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(newDescription);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(newWebsite);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    ImmutableList<String> updatedOfficers =
        ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.OFFICER_PROP));

    Assert.assertNotNull(clubEntity);
    Assert.assertEquals(newDescription, clubEntity.getProperty(Constants.DESCRIP_PROP));
    Assert.assertEquals(newWebsite, clubEntity.getProperty(Constants.WEBSITE_PROP));
    Assert.assertEquals(2, updatedOfficers.size());
    Assert.assertEquals(TEST_EMAIL, updatedOfficers.get(0));
    Assert.assertEquals(newOfficer, updatedOfficers.get(1));
  }

  @Test
  public void doPost_editClubSomeValidOfficers() throws ServletException, IOException {
    prepClubEnv();
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP))
        .thenReturn("fake-person@fake.com," + TEST_EMAIL);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    ImmutableList<String> updatedOfficers =
        ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.OFFICER_PROP));

    Assert.assertNotNull(clubEntity);
    Assert.assertEquals(SAMPLE_CLUB_DESC_1, clubEntity.getProperty(Constants.DESCRIP_PROP));
    Assert.assertEquals(SAMPLE_CLUB_WEB, clubEntity.getProperty(Constants.WEBSITE_PROP));
    Assert.assertEquals(1, updatedOfficers.size());
    Assert.assertEquals(TEST_EMAIL, updatedOfficers.get(0));
  }

  @Test
  public void doPost_editClubNoValidOfficers() throws ServletException, IOException {
    prepClubEnv();
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn("fake-person@fake.com");
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    ImmutableList<String> updatedOfficers =
        ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.OFFICER_PROP));

    Assert.assertNotNull(clubEntity);
    Assert.assertEquals(SAMPLE_CLUB_DESC_1, clubEntity.getProperty(Constants.DESCRIP_PROP));
    Assert.assertEquals(SAMPLE_CLUB_WEB, clubEntity.getProperty(Constants.WEBSITE_PROP));
  }

  @Test
  public void doPost_editLabels() throws ServletException, IOException {
    prepClubEnv();

    String label1 = "ST e    M ";
    String label2 = "HGBdi - sg ";
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn("fake-person@fake.com");
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);
    when(request.getParameter(Constants.LABELS_PROP)).thenReturn(label1 + "," + label2);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    ImmutableList<String> labels =
        ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.LABELS_PROP));

    Assert.assertNotNull(clubEntity);
    Assert.assertEquals(2, labels.size());
    Assert.assertEquals(labels.get(0), "stem");
    Assert.assertEquals(labels.get(1), "hgbdi-sg");
  }

  private void prepClubEnv() {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP);
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
