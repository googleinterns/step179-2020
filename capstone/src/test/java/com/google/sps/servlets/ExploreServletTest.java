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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

/**
 * Class to test ExploreServlet by adding a few mock clubs and ensuring their existence and
 * properties
 */
@RunWith(JUnit4.class)
public final class ExploreServletTest {

  private ExploreServlet servlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private DatastoreService datastore;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalURLFetchServiceTestConfig());

  @Before
  public void setUp() {
    this.servlet = new ExploreServlet();
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    this.datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void correctNumberAndObjectsReturned() throws IOException {

    String KEVIN = "kshao@google.com";
    String MEGHA = "kakm@google.com";
    String MEGAN = "meganshi@google.com";

    String CLUB_1 = "Club 1";
    String CLUB_2 = "Club 2";
    String CLUB_3 = "Club 3";

    String SITE_1 = "www.fakesite.com";
    String SITE_2 = "www.realfakesite.com";

    String DESCRIPTION_1 = "Helping people.";
    String DESCRIPTION_2 = "Not helping people.";

    String BLOB_KEY_1 = "fake blob key";
    String BLOB_KEY_2 = "another fake blob key";

    String CALENDAR_ID_1 = "calendar 1";
    String CALENDAR_ID_2 = "calendar 2";

    long TIME_1 = 10;
    long TIME_2 = 20;

    helper.setEnvEmail("kshao").setEnvAuthDomain("gmail.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.SORT_PROP)).thenReturn(Constants.DEFAULT_SORT_PROP);

    Entity club1 = new Entity(Constants.CLUB_ENTITY_PROP);
    club1.setProperty(Constants.PROPERTY_NAME, CLUB_1);
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGHA, MEGAN, KEVIN));
    club1.setProperty(Constants.OFFICER_PROP, ImmutableList.of(MEGHA));
    club1.setProperty(Constants.DESCRIP_PROP, DESCRIPTION_1);
    club1.setProperty(Constants.WEBSITE_PROP, SITE_1);
    club1.setProperty(Constants.LOGO_PROP, BLOB_KEY_1);
    club1.setProperty(Constants.CALENDAR_PROP, CALENDAR_ID_1);
    club1.setProperty(Constants.TIME_PROP, TIME_1);

    Entity club2 = new Entity(Constants.CLUB_ENTITY_PROP);
    club2.setProperty(Constants.PROPERTY_NAME, CLUB_2);
    club2.setProperty(Constants.MEMBER_PROP, ImmutableList.of(KEVIN));
    club2.setProperty(Constants.OFFICER_PROP, ImmutableList.of(KEVIN));
    club2.setProperty(Constants.DESCRIP_PROP, DESCRIPTION_2);
    club2.setProperty(Constants.WEBSITE_PROP, SITE_2);
    club2.setProperty(Constants.LOGO_PROP, BLOB_KEY_2);
    club2.setProperty(Constants.CALENDAR_PROP, CALENDAR_ID_2);
    club2.setProperty(Constants.TIME_PROP, TIME_2);

    this.datastore.put(club1);
    this.datastore.put(club2);

    JsonArray response = getServletResponse(servlet);

    int expectedSize = 2;
    Assert.assertEquals(expectedSize, response.size());

    JsonElement element0 = response.get(0);
    Assert.assertTrue(element0.isJsonObject());
    JsonObject object0 = (JsonObject) element0;
    Assert.assertEquals(object0.get(Constants.PROPERTY_NAME).getAsString(), CLUB_1);
    // Remove additional quotation marks from JSON Array and convert to ImmutableList
    ImmutableList members0 =
        Streams.stream(object0.get(Constants.MEMBER_PROP).getAsJsonArray())
            .map(member -> member.toString().replaceAll("\"", ""))
            .collect(toImmutableList());
    ImmutableList officers0 =
        Streams.stream(object0.get(Constants.OFFICER_PROP).getAsJsonArray())
            .map(officer -> officer.toString().replaceAll("\"", ""))
            .collect(toImmutableList());

    Assert.assertEquals(members0, ImmutableList.of(MEGHA, MEGAN, KEVIN));
    Assert.assertEquals(officers0, ImmutableList.of(MEGHA));
    Assert.assertEquals(object0.get(Constants.DESCRIP_PROP).getAsString(), DESCRIPTION_1);
    Assert.assertEquals(object0.get(Constants.WEBSITE_PROP).getAsString(), SITE_1);
    Assert.assertEquals(object0.get(Constants.LOGO_PROP).getAsString(), BLOB_KEY_1);
    Assert.assertEquals(object0.get(Constants.CALENDAR_PROP).getAsString(), CALENDAR_ID_1);
    Assert.assertEquals(object0.get(Constants.TIME_PROP).getAsLong(), TIME_1);

    JsonElement element1 = response.get(1);
    Assert.assertTrue(element1.isJsonObject());
    JsonObject object1 = (JsonObject) element1;
    Assert.assertEquals(object1.get(Constants.PROPERTY_NAME).getAsString(), CLUB_2);

    // Remove additional quotation marks from JSON Array and convert to ImmutableList
    ImmutableList members1 =
        Streams.stream(object1.get(Constants.MEMBER_PROP).getAsJsonArray())
            .map(member -> member.toString().replaceAll("\"", ""))
            .collect(toImmutableList());
    ImmutableList officers1 =
        Streams.stream(object1.get(Constants.OFFICER_PROP).getAsJsonArray())
            .map(officer -> officer.toString().replaceAll("\"", ""))
            .collect(toImmutableList());
    Assert.assertEquals(members1, ImmutableList.of(KEVIN));
    Assert.assertEquals(officers1, ImmutableList.of(KEVIN));
    Assert.assertEquals(object1.get(Constants.DESCRIP_PROP).getAsString(), DESCRIPTION_2);
    Assert.assertEquals(object1.get(Constants.WEBSITE_PROP).getAsString(), SITE_2);
    Assert.assertEquals(object1.get(Constants.LOGO_PROP).getAsString(), BLOB_KEY_2);
    Assert.assertEquals(object1.get(Constants.CALENDAR_PROP).getAsString(), CALENDAR_ID_2);
    Assert.assertEquals(object1.get(Constants.TIME_PROP).getAsLong(), TIME_2);
  }

  private JsonArray getServletResponse(ExploreServlet servlet) throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    String responseStr = stringWriter.toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);

    return responseJsonElement.getAsJsonArray();
  }
}
