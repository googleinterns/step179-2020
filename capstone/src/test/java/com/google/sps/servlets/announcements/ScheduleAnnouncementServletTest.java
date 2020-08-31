// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import javax.servlet.ServletConfig;
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

/** */
@RunWith(JUnit4.class)
public final class ScheduleAnnouncementServletTest {

  private final long SAMPLE_TIME = 123456789;
  private final String SAMPLE_CLUB_NAME = "Test Club";
  private final String SAMPLE_CONTENT = "A random announcement";
  private final String SAMPLE_NEW_CONTENT = "A new announcement";
  private final String TEST_EMAIL = "test-email@gmail.com";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  @Mock ServletConfig config;
  private DatastoreService datastore;
  private ScheduleAnnouncementServlet servlet;
  private Clock clock;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException, ServletException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    this.servlet = new ScheduleAnnouncementServlet();
    this.servlet.init(config);
    datastore = DatastoreServiceFactory.getDatastoreService();

    Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP);
    clubEntity.setProperty(Constants.PROPERTY_NAME, SAMPLE_CLUB_NAME);
    clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(TEST_EMAIL));
    clubEntity.setProperty(Constants.MEMBER_PROP, ImmutableList.of(TEST_EMAIL));
    clubEntity.setProperty(Constants.DESCRIP_PROP, "");
    clubEntity.setProperty(Constants.WEBSITE_PROP, "");
    clubEntity.setProperty(Constants.CALENDAR_PROP, "");
    clubEntity.setProperty(Constants.EXCLUSIVE_PROP, false);
    clubEntity.setProperty(Constants.LABELS_PROP, ImmutableList.of());
    clubEntity.setProperty(Constants.TIME_PROP, 0);
    datastore.put(clubEntity);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_desiredBehavior() throws IOException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("gmail.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);

    Entity announcement1 = new Entity(Constants.FUTURE_ANNOUNCEMENT_PROP);
    announcement1.setProperty(Constants.AUTHOR_PROP, TEST_EMAIL);
    announcement1.setProperty(Constants.CLUB_PROP, SAMPLE_CLUB_NAME);
    announcement1.setProperty(Constants.CONTENT_PROP, SAMPLE_NEW_CONTENT);
    announcement1.setProperty(Constants.TIME_PROP, SAMPLE_TIME);
    announcement1.setProperty(Constants.EDITED_PROP, false);
    this.datastore.put(announcement1);

    JsonArray response = getServletResponse(servlet);
    Assert.assertEquals(1, response.size());

    JsonObject announcement = response.get(0).getAsJsonObject();
    Assert.assertEquals(TEST_EMAIL, announcement.get(Constants.AUTHOR_PROP).getAsString());
    Assert.assertEquals(SAMPLE_CLUB_NAME, announcement.get(Constants.CLUB_PROP).getAsString());
    Assert.assertEquals(SAMPLE_NEW_CONTENT, announcement.get(Constants.CONTENT_PROP).getAsString());
    Assert.assertEquals(SAMPLE_TIME, announcement.get(Constants.TIME_PROP).getAsLong());
    Assert.assertFalse(announcement.get(Constants.EDITED_PROP).getAsBoolean());
  }

  private JsonArray getServletResponse(ScheduleAnnouncementServlet servlet) throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    String responseStr = stringWriter.toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);

    return responseJsonElement.getAsJsonArray();
  }

  @Test
  public void scheduleAnnouncement() throws ServletException, IOException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);
    ZoneId zone = ZoneId.of(Constants.TIME_ZONE);

    when(request.getParameter(Constants.CONTENT_PROP)).thenReturn(SAMPLE_NEW_CONTENT);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(Constants.TIMEZONE_PROP)).thenReturn("America/Los_Angeles");

    when(request.getParameter(Constants.SCHEDULED_DATE_PROP)).thenReturn("2016-01-23T12:35");
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    Instant instant = Instant.parse("2016-01-23T20:34:00.00Z"); // This should be UTC time
    servlet.setClock(Clock.fixed(instant, ZoneId.of("Z")));
    servlet.doPost(request, response);

    // Announcement has been made to happen after a minute. Check after 50 and 70 seconds.
    instant = Instant.parse("2016-01-23T20:34:50.00Z");
    servlet.setClock(Clock.fixed(instant, ZoneId.of("Z")));
    AnnouncementsSweeper.setClock(Clock.fixed(instant, ZoneId.of("Z")));
    AnnouncementsSweeper.sweepAnnouncements();
    Assert.assertFalse(checkForAnnouncement(SAMPLE_CLUB_NAME, SAMPLE_NEW_CONTENT));

    instant = Instant.parse("2016-01-23T20:35:10.00Z");
    servlet.setClock(Clock.fixed(instant, ZoneId.of("Z")));
    AnnouncementsSweeper.setClock(Clock.fixed(instant, ZoneId.of("Z")));
    AnnouncementsSweeper.sweepAnnouncements();
    Assert.assertTrue(checkForAnnouncement(SAMPLE_CLUB_NAME, SAMPLE_NEW_CONTENT));
  }

  private boolean checkForAnnouncement(String club, String content) {
    Query query = new Query(Constants.ANNOUNCEMENT_PROP);
    PreparedQuery results = datastore.prepare(query);

    ImmutableList<Entity> entities =
        Streams.stream(results.asIterable())
            .filter(
                entity ->
                    club.equals(entity.getProperty(Constants.CLUB_PROP))
                        && content.equals(entity.getProperty(Constants.CONTENT_PROP)))
            .collect(toImmutableList());
    return !entities.isEmpty();
  }
}
