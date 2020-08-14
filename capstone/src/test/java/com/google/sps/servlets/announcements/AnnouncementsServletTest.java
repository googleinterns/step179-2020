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

import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
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
public final class AnnouncementsServletTest {

  private static final String ANNOUNCEMENT_PROP = "Announcement";

  private static final String AUTHOR_1 = "kshao@google.com";
  private static final String AUTHOR_2 = "kakm@google.com";
  private static final String AUTHOR_3 = "meganshi@google.com";

  private static final String CLUB_1 = "Club 1";
  private static final String CLUB_2 = "Club 2";
  private static final String CLUB_3 = "Club 3";
  private static final String CONTENT_1 = "Cool test comment.";

  private static final long TIME_10 = 10L;

  private AnnouncementsServlet servlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  private DatastoreService datastore;

  private Entity announcement1;
  private Entity announcement2;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

  @Before
  public void setUp() {
    this.servlet = new AnnouncementsServlet();
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    this.datastore = DatastoreServiceFactory.getDatastoreService();

    announcement1 = new Entity(ANNOUNCEMENT_PROP);
    announcement1.setProperty(Constants.AUTHOR_PROP, AUTHOR_1);
    announcement1.setProperty(Constants.CLUB_PROP, CLUB_1);
    announcement1.setProperty(Constants.CONTENT_PROP, CONTENT_1);
    announcement1.setProperty(Constants.TIME_PROP, TIME_10);
    announcement1.setProperty(Constants.EDITED_PROP, false);

    this.datastore.put(announcement1);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void clubHasNoAnnouncements() throws IOException {
    helper.setEnvEmail("kshao").setEnvAuthDomain("gmail.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(CLUB_2);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(AUTHOR_1);

    JsonArray response = getServletResponse(servlet);

    int expectedSize = 0;
    Assert.assertEquals(expectedSize, response.size());
  }

  @Test
  public void clubHasOnlyOneAnnouncement() throws IOException {
    helper.setEnvEmail("kshao").setEnvAuthDomain("gmail.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.PROPERTY_NAME)).thenReturn(CLUB_1);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(AUTHOR_1);

    JsonArray response = getServletResponse(servlet);

    int expectedSize = 1;
    Assert.assertEquals(expectedSize, response.size());

    JsonObject announcement = response.get(0).getAsJsonObject();
    Assert.assertEquals(AUTHOR_1, announcement.get(Constants.AUTHOR_PROP).getAsString());
    Assert.assertEquals(CLUB_1, announcement.get(Constants.CLUB_PROP).getAsString());
    Assert.assertEquals(CONTENT_1, announcement.get(Constants.CONTENT_PROP).getAsString());
    Assert.assertEquals(TIME_10, announcement.get(Constants.TIME_PROP).getAsLong());
    Assert.assertFalse(announcement.get(Constants.EDITED_PROP).getAsBoolean());
  }

  private JsonArray getServletResponse(AnnouncementsServlet servlet) throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    String responseStr = stringWriter.toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);

    return responseJsonElement.getAsJsonArray();
  }
}
