package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
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
public class AddEventsServletTest {
  private static final String CLUB_1 = "Club 1";
  private static final String EVENT_TITLE = "Club 1 Hangout";
  private final String EVENT_DESCRIPTION = "Meeting description";
  private final String EVENT_START_TIME = "2020-08-25T19:30";
  private final String EVENT_END_TIME = "2020-08-25T21:30";
  private final String CALENDAR_ID = "Club 1 Calendar ID";
  private final String OFFICER_EMAIL = "kakm@google.com";
  private final AddEventsServlet servlet = new AddEventsServlet();

  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Spy AddEventsServlet servletSpy;
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
  public void doPost_addEventToCalendar()
      throws ServletException, IOException, GeneralSecurityException {
    // Get expected event
    EventDateTime startTime = getEventDateTime(EVENT_START_TIME);
    EventDateTime endTime = getEventDateTime(EVENT_END_TIME);
    Event expected =
        new Event()
            .setSummary(EVENT_TITLE)
            .setDescription(EVENT_DESCRIPTION)
            .setStart(startTime)
            .setEnd(endTime);
    String actualEvent = doPost_addEventsServletResponse(expected);

    Assert.assertEquals(expected.toString(), actualEvent);
  }

  private String doPost_addEventsServletResponse(Event expected)
      throws IOException, GeneralSecurityException {
    helper.setEnvEmail(OFFICER_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.CLUB_PROP)).thenReturn(CLUB_1);

    // Set up club entity with calendar ID
    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP);
    clubEntity.setProperty(Constants.PROPERTY_NAME, CLUB_1);
    clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(OFFICER_EMAIL));
    clubEntity.setProperty(Constants.CALENDAR_PROP, CALENDAR_ID);
    datastore.put(clubEntity);

    // Set up event information
    when(request.getParameter(Constants.START_TIME_PROP)).thenReturn(EVENT_START_TIME);
    when(request.getParameter(Constants.END_TIME_PROP)).thenReturn(EVENT_END_TIME);
    when(request.getParameter(Constants.EVENT_TITLE_PROP)).thenReturn(EVENT_TITLE);
    when(request.getParameter(Constants.EVENT_DESCRIPTION_PROP)).thenReturn(EVENT_DESCRIPTION);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    // Add expected event to calendar
    Mockito.doReturn(expected)
        .when(servletSpy)
        .addEventToCalendar(
            CALENDAR_ID,
            EVENT_TITLE,
            EVENT_DESCRIPTION,
            EVENT_START_TIME + Constants.TIMEZONE_OFFSET,
            EVENT_END_TIME + Constants.TIMEZONE_OFFSET);
    servletSpy.doPost(request, response);
    return stringWriter.getBuffer().toString().trim();
  }

  private static EventDateTime getEventDateTime(String time) {
    return new EventDateTime()
        .setDateTime(new DateTime(time + Constants.TIMEZONE_OFFSET))
        .setTimeZone(Constants.TIME_ZONE);
  }
}
