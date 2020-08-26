package com.google.sps.servlets;

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
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public final class ServletUtilTest {
  public static final String MEGAN_EMAIL = "meganshi@google.com";
  public static final String CLUB_1 = "Club 1";
  public static final String CLUB_2 = "Club 2";

  @Mock private HttpServletRequest request;
  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  Entity studentMegan;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    localHelper.setUp();

    studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());
  }

  @After
  public void tearDown() throws Exception {
    localHelper.tearDown();
  }

  @Test
  public void getPropertyList_getEmptyClubList() throws ServletException, IOException {
    datastore.put(studentMegan);
    Entity student = getEntityFromDatastore(MEGAN_EMAIL);
    Assert.assertTrue(student != null);

    Assert.assertEquals(
        ImmutableList.of(), ServletUtil.getPropertyList(student, Constants.PROPERTY_CLUBS));
  }

  @Test
  public void getPropertyList_getNonEmptyMemberList() throws ServletException, IOException {
    Entity club1 = new Entity("Club");
    club1.setProperty(Constants.PROPERTY_NAME, CLUB_1);
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGAN_EMAIL));
    datastore.put(club1);

    // Get club entity from Datastore
    Query query =
        new Query("Club")
            .setFilter(new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, CLUB_1));
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> clubs = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(clubs.isEmpty());
    Assert.assertTrue(clubs.size() == 1);
    Entity club = clubs.get(0);

    Assert.assertEquals(
        ImmutableList.of(MEGAN_EMAIL), ServletUtil.getPropertyList(club, Constants.MEMBER_PROP));
  }

  @Test
  public void getNameByEmail_getNameByValidEmail() throws ServletException, IOException {
    studentMegan.setProperty(Constants.PROPERTY_NAME, "Megan Shi");
    datastore.put(studentMegan);

    Assert.assertEquals(
        studentMegan.getProperty(Constants.PROPERTY_NAME), ServletUtil.getNameByEmail(MEGAN_EMAIL));
  }

  @Test
  public void getPictureByEmail_desiredBehavior() throws ServletException, IOException {
    studentMegan.setProperty(Constants.PROFILE_PIC_PROP, "url.png");
    datastore.put(studentMegan);

    Assert.assertEquals(
        studentMegan.getProperty(Constants.PROFILE_PIC_PROP),
        ServletUtil.getPictureByEmail(MEGAN_EMAIL));
  }

  @Test
  public void addItemToEntity_listIsInitiallyEmpty() throws ServletException, IOException {
    datastore.put(studentMegan);
    Entity student = getEntityFromDatastore(MEGAN_EMAIL);
    Assert.assertTrue(student != null);
    // Get updated entity
    studentMegan = ServletUtil.addItemToEntity(student, CLUB_1, Constants.PROPERTY_CLUBS);
    datastore.put(studentMegan);
    ImmutableList<String> expectedClubList = ImmutableList.of(CLUB_1);

    Assert.assertEquals(expectedClubList, studentMegan.getProperty(Constants.PROPERTY_CLUBS));
  }

  @Test
  public void addItemToEntity_listIsNotEmpty() throws ServletException, IOException {
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1));
    datastore.put(studentMegan);
    Entity student = getEntityFromDatastore(MEGAN_EMAIL);
    Assert.assertTrue(student != null);
    // Get updated entity
    studentMegan = ServletUtil.addItemToEntity(student, CLUB_2, Constants.PROPERTY_CLUBS);
    datastore.put(studentMegan);
    ImmutableList<String> expectedClubList = ImmutableList.of(CLUB_1, CLUB_2);

    Assert.assertEquals(expectedClubList, studentMegan.getProperty(Constants.PROPERTY_CLUBS));
  }

  @Test
  public void removeItemToEntity_listIsInitiallyEmpty() throws ServletException, IOException {
    datastore.put(studentMegan);
    Entity student = getEntityFromDatastore(MEGAN_EMAIL);
    Assert.assertTrue(student != null);
    // Get updated entity
    studentMegan = ServletUtil.removeItemFromEntity(student, CLUB_1, Constants.PROPERTY_CLUBS);
    datastore.put(studentMegan);
    ImmutableList<String> expectedClubList = ImmutableList.of();

    Assert.assertEquals(expectedClubList, studentMegan.getProperty(Constants.PROPERTY_CLUBS));
  }

  @Test
  public void removeItemToEntity_listIsNotEmpty() throws ServletException, IOException {
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of(CLUB_1, CLUB_2));
    datastore.put(studentMegan);
    Entity student = getEntityFromDatastore(MEGAN_EMAIL);
    Assert.assertTrue(student != null);
    // Get updated entity
    studentMegan = ServletUtil.removeItemFromEntity(student, CLUB_1, Constants.PROPERTY_CLUBS);
    datastore.put(studentMegan);
    ImmutableList<String> expectedClubList = ImmutableList.of(CLUB_2);

    Assert.assertEquals(expectedClubList, studentMegan.getProperty(Constants.PROPERTY_CLUBS));
  }

  private Entity getEntityFromDatastore(String email) {
    // Get student entity from Datastore to resolve casting errors
    Query query = new Query(email);
    PreparedQuery results = datastore.prepare(query);
    Entity student = results.asSingleEntity();
    return student;
  }
}
