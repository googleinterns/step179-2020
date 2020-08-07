package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public class OfficerServletTest {

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private OfficerServlet servlet;
  private DatastoreService datastore;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalUserServiceTestConfig(),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    this.servlet = new OfficerServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void isUserOfficer_validOfficer() throws IOException {
    String officer = "kshao@google.com";
    String clubName = "Club 1";
    Entity club = new Entity(Constants.CLUB_ENTITY_PROP);
    club.setProperty(Constants.PROPERTY_NAME, clubName);
    club.setProperty(Constants.DESCRIP_PROP, "Irrelevant");
    club.setProperty(Constants.MEMBER_PROP, ImmutableList.of(officer, "irrelevant"));
    club.setProperty(Constants.OFFICER_PROP, ImmutableList.of(officer, "irrelevant"));
    club.setProperty(Constants.WEBSITE_PROP, "website.com");
    this.datastore.put(club);

    helper.setEnvEmail("kshao@google.com").setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(clubName);

    boolean response = getServletResponse(servlet);
    Assert.assertTrue(response);
  }

  @Test
  public void isUserOfficer_invalidOfficer() throws IOException {
    String officer = "megan.shi@google.com";
    String member = "kshao@google.com";
    String clubName = "Club 2";
    Entity club = new Entity(Constants.CLUB_ENTITY_PROP);
    club.setProperty(Constants.PROPERTY_NAME, clubName);
    club.setProperty(Constants.DESCRIP_PROP, "Irrelevant");
    club.setProperty(Constants.MEMBER_PROP, ImmutableList.of(officer, member, "irrelevant"));
    club.setProperty(Constants.OFFICER_PROP, ImmutableList.of(officer, "irrelevant"));
    club.setProperty(Constants.WEBSITE_PROP, "website.com");
    this.datastore.put(club);

    helper.setEnvEmail("kshao@google.com").setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(clubName);

    boolean response = getServletResponse(servlet);
    Assert.assertFalse(response);
  }

  private boolean getServletResponse(OfficerServlet servlet) throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    String responseStr = stringWriter.toString().trim();
    return Boolean.parseBoolean(responseStr);
  }
}
