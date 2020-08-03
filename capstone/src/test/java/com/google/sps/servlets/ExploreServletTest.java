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
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

/** */
@RunWith(JUnit4.class)
public final class ExploreServletTest {

  private static final String KEVIN = "kshao@google.com";
  private static final String MEGHA = "kakm@google.com";
  private static final String MEGAN = "meganshi@google.com";

  private static final String CLUB_1 = "Club 1";
  private static final String CLUB_2 = "Club 2";
  private static final String CLUB_3 = "Club 3";

  private static final String SITE_1 = "www.fakesite.com";
  private static final String SITE_2 = "www.realfakesite.com";

  private static final String DESCRIPTION_1 = "Helping people.";
  private static final String DESCRIPTION_2 = "Not helping people.";

  private ExploreServlet servlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private DatastoreService datastore;

  private Entity club1;
  private Entity club2;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(),
          new LocalUserServiceTestConfig(),
          new LocalURLFetchServiceTestConfig());

  @Before
  public void setUp() {
    this.servlet = new ExploreServlet();
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    this.datastore = DatastoreServiceFactory.getDatastoreService();

    club1 = new Entity(Constants.CLUB_PROP);
    club1.setProperty(Constants.CLUB_NAME_PROP, CLUB_1);
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGHA, MEGAN, KEVIN));
    club1.setProperty(Constants.OFFICER_PROP, ImmutableList.of(MEGHA));
    club1.setProperty(Constants.DESCRIP_PROP, DESCRIPTION_1);
    club1.setProperty(Constants.WEBSITE_PROP, SITE_1);

    club2 = new Entity(Constants.CLUB_PROP);
    club2.setProperty(Constants.CLUB_NAME_PROP, CLUB_2);
    club2.setProperty(Constants.MEMBER_PROP, ImmutableList.of(KEVIN));
    club2.setProperty(Constants.OFFICER_PROP, ImmutableList.of(KEVIN));
    club2.setProperty(Constants.DESCRIP_PROP, DESCRIPTION_2);
    club2.setProperty(Constants.WEBSITE_PROP, SITE_2);

    this.datastore.put(club1);
    this.datastore.put(club2);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void correctNumberReturned() throws IOException {
    helper.setEnvEmail("kshao").setEnvAuthDomain("gmail.com").setEnvIsLoggedIn(true);
    when(request.getParameter(Constants.CLUB_NAME_PROP)).thenReturn(CLUB_2);

    JsonArray response = getServletResponse(servlet);

    int expectedSize = 2;
    Assert.assertEquals(expectedSize, response.size());
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
