package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.api.services.calendar.model.Event;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.ArrayList;
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
import org.mockito.Spy;

@RunWith(JUnit4.class)
public class EventsServletTest {
  private static final String CLUB_1 = "Club 1";
  private static final String EVENT_TITLE = "Club 1 Hangout";
  private final String EVENT_DESCRIPTION = "Meeting description";
  private final String CALENDAR_ID = "Club 1 Calendar ID";
  private final String OFFICER_EMAIL = "kshao@google.com";
  private final EventsServlet servlet = new EventsServlet();

  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Mock Principal principal;
  @Spy EventsServlet servletSpy;
  private DatastoreService datastore;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig()
              .setDefaultHighRepJobPolicyUnappliedJobPercentage(0));

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    servletSpy = Mockito.spy(servlet);
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doPost_desiredBehavior()
      throws ServletException, IOException, GeneralSecurityException {
    helper.setEnvEmail(OFFICER_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(OFFICER_EMAIL);
    String eventId = "event";
    String calendarId = "calendar";

    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP);
    clubEntity.setProperty(Constants.PROPERTY_NAME, CLUB_1);
    clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(OFFICER_EMAIL));
    clubEntity.setProperty(Constants.CALENDAR_PROP, CALENDAR_ID);
    datastore.put(clubEntity);

    Event event = new Event().setSummary(EVENT_TITLE).setDescription(EVENT_DESCRIPTION);
    List<Event> events = new ArrayList<Event>();
    events.add(event);
    Mockito.doReturn(events).when(servletSpy).getEvents(CALENDAR_ID);

    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(CLUB_1);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servletSpy.doGet(request, response);

    Gson gson = new Gson();
    String expectedJson = gson.toJson(events);
    Assert.assertEquals(expectedJson, stringWriter.getBuffer().toString().trim());
  }
}
