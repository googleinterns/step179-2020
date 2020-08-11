package com.google.sps.servlets;

import static org.mockito.Mockito.when;

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
public final class ServletUtilTest {
  public static final String MEGAN_EMAIL = "meganshi@google.com";
  public static final String CLUB_1 = "Club 1";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Entity studentMegan;
  private Entity club1;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    localHelper.setUp();

    studentMegan = new Entity(MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_EMAIL, MEGAN_EMAIL);
    studentMegan.setProperty(Constants.PROPERTY_CLUBS, ImmutableList.of());

    club1 = new Entity("Club");
    club1.setProperty(Constants.PROPERTY_NAME, CLUB_1);
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGAN_EMAIL));
  }

  @After
  public void tearDown() throws Exception {
    localHelper.tearDown();
  }

  @Test
  public void getPropertyList_getEmptyClubList() throws ServletException, IOException {
    datastore.put(studentMegan);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_EMAIL)).thenReturn(MEGAN_EMAIL);

    // Get student entity from Datastore
    Query query = new Query(MEGAN_EMAIL);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(students.isEmpty());
    Assert.assertTrue(students.size() == 1);
    Entity student = students.get(0);
    ImmutableList<String> clubList = ServletUtil.getPropertyList(student, Constants.PROPERTY_CLUBS);

    Assert.assertEquals(ImmutableList.of(), clubList);
  }

  @Test
  public void getPropertyList_getNonEmptyMemberList() throws ServletException, IOException {
    datastore.put(club1);
    localHelper.setEnvEmail(MEGAN_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(CLUB_1);

    // Get club entity from Datastore
    Query query =
        new Query("Club")
            .setFilter(new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, CLUB_1));
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> clubs = ImmutableList.copyOf(results.asIterable());
    Assert.assertFalse(clubs.isEmpty());
    Assert.assertTrue(clubs.size() == 1);
    Entity club = clubs.get(0);
    ImmutableList<String> actualMemberList =
        ServletUtil.getPropertyList(club, Constants.MEMBER_PROP);

    Assert.assertEquals(ImmutableList.of(MEGAN_EMAIL), actualMemberList);
  }
}
