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
  private final String JOIN_EMAIL_1 = "email_1@gmail.com";
  private final String JOIN_EMAIL_2 = "email_2@gmail.com";
  private final String CHECKBOX_ON = "on";

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
    Entity startingEntity = makeClubEntity();
    datastore.put(startingEntity);
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
    Entity startingEntity = makeClubEntity();
    datastore.put(startingEntity);
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
    Entity startingEntity = makeClubEntity();
    datastore.put(startingEntity);
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
    Entity startingEntity = makeClubEntity();
    datastore.put(startingEntity);
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

  @Test
  public void doPost_makeClubExclusive() throws ServletException, IOException {
    Entity startingEntity = makeClubEntity();
    startingEntity.setProperty(Constants.EXCLUSIVE_PROP, false);
    datastore.put(startingEntity);
    String newDescription = "new test description";
    String newWebsite = "new-website.com";
    String newOfficer = "kakm@google.com";
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn(TEST_EMAIL + "," + newOfficer);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(newDescription);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(newWebsite);
    when(request.getParameter(Constants.EXCLUSIVE_PROP)).thenReturn(CHECKBOX_ON);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    Assert.assertNotNull(clubEntity);
    Assert.assertTrue((Boolean) clubEntity.getProperty(Constants.EXCLUSIVE_PROP));
  }

  @Test
  public void doPost_makeClubNotExclusive() throws ServletException, IOException {
    Entity startingEntity = makeClubEntity();
    startingEntity.setProperty(Constants.EXCLUSIVE_PROP, true);
    datastore.put(startingEntity);
    String newDescription = "new test description";
    String newWebsite = "new-website.com";
    String newOfficer = "kakm@google.com";
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn(TEST_EMAIL + "," + newOfficer);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(newDescription);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(newWebsite);
    when(request.getParameter(Constants.EXCLUSIVE_PROP)).thenReturn(null);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    Assert.assertNotNull(clubEntity);
    Assert.assertFalse((Boolean) clubEntity.getProperty(Constants.EXCLUSIVE_PROP));
  }

  @Test
  public void doPost_addAllRequestedMembers() throws ServletException, IOException {
    Entity startingEntity = makeClubEntity();
    startingEntity.setProperty(Constants.EXCLUSIVE_PROP, false);
    datastore.put(startingEntity);

    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn(TEST_EMAIL);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);
    when(request.getParameter(Constants.EXCLUSIVE_PROP)).thenReturn(CHECKBOX_ON);
    when(request.getParameter(JOIN_EMAIL_1)).thenReturn(JOIN_EMAIL_1);
    when(request.getParameter(JOIN_EMAIL_2)).thenReturn(JOIN_EMAIL_2);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    ImmutableList<String> newMembers =
        ServletUtil.getPropertyList(clubEntity, Constants.MEMBER_PROP);
    ImmutableList<String> newRequests =
        ServletUtil.getPropertyList(clubEntity, Constants.REQUEST_PROP);

    Assert.assertEquals(6, newMembers.size());
    Assert.assertTrue(newMembers.contains(JOIN_EMAIL_1));
    Assert.assertTrue(newMembers.contains(JOIN_EMAIL_2));

    Assert.assertEquals(0, newRequests.size());
  }

  @Test
  public void doPost_addNoRequestedMembers() throws ServletException, IOException {
    Entity startingEntity = makeClubEntity();
    startingEntity.setProperty(Constants.EXCLUSIVE_PROP, false);
    datastore.put(startingEntity);

    Entity testStudent = new Entity(TEST_EMAIL);
    testStudent.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());
    datastore.put(testStudent);

    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn(TEST_EMAIL);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);
    when(request.getParameter(Constants.EXCLUSIVE_PROP)).thenReturn(CHECKBOX_ON);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    ImmutableList<String> newMembers =
        ServletUtil.getPropertyList(clubEntity, Constants.MEMBER_PROP);
    ImmutableList<String> newRequests =
        ServletUtil.getPropertyList(clubEntity, Constants.REQUEST_PROP);

    Assert.assertEquals(4, newMembers.size());
    Assert.assertFalse(newMembers.contains(JOIN_EMAIL_1));
    Assert.assertFalse(newMembers.contains(JOIN_EMAIL_2));

    Assert.assertEquals(2, newRequests.size());
    Assert.assertEquals(JOIN_EMAIL_1, newRequests.get(0));
    Assert.assertEquals(JOIN_EMAIL_2, newRequests.get(1));
  }

  @Test
  public void doPost_addOneRequestedMember() throws ServletException, IOException {
    Entity startingEntity = makeClubEntity();
    startingEntity.setProperty(Constants.EXCLUSIVE_PROP, false);
    datastore.put(startingEntity);

    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.OFFICER_PROP)).thenReturn(TEST_EMAIL);
    when(request.getParameter(Constants.DESCRIP_PROP)).thenReturn(SAMPLE_CLUB_DESC_1);
    when(request.getParameter(Constants.WEBSITE_PROP)).thenReturn(SAMPLE_CLUB_WEB);
    when(request.getParameter(Constants.EXCLUSIVE_PROP)).thenReturn(CHECKBOX_ON);
    when(request.getParameter(JOIN_EMAIL_1)).thenReturn(JOIN_EMAIL_1);
    editClubServlet.doPost(request, response);

    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME, FilterOperator.EQUAL, SAMPLE_CLUB_NAME));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    ImmutableList<String> newMembers =
        ServletUtil.getPropertyList(clubEntity, Constants.MEMBER_PROP);
    ImmutableList<String> newRequests =
        ServletUtil.getPropertyList(clubEntity, Constants.REQUEST_PROP);

    Assert.assertEquals(5, newMembers.size());
    Assert.assertTrue(newMembers.contains(JOIN_EMAIL_1));

    Assert.assertEquals(1, newRequests.size());
    Assert.assertEquals(JOIN_EMAIL_2, newRequests.get(0));
  }

  private Entity makeClubEntity() {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP);
    clubEntity.setProperty(Constants.PROPERTY_NAME, SAMPLE_CLUB_NAME);
    clubEntity.setProperty(Constants.DESCRIP_PROP, SAMPLE_CLUB_DESC_1);
    clubEntity.setProperty(Constants.WEBSITE_PROP, SAMPLE_CLUB_WEB);
    clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(TEST_EMAIL));
    clubEntity.setProperty(
        Constants.MEMBER_PROP,
        ImmutableList.of(TEST_EMAIL, "meganshi@google.com", "kakm@google.com", "kshao@google.com"));
    clubEntity.setProperty(Constants.REQUEST_PROP, ImmutableList.of(JOIN_EMAIL_1, JOIN_EMAIL_2));

    Entity testStudent1 = new Entity(JOIN_EMAIL_1);
    testStudent1.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());
    datastore.put(testStudent1);

    Entity testStudent2 = new Entity(JOIN_EMAIL_2);
    testStudent2.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());
    datastore.put(testStudent2);

    return clubEntity;
  }
}
