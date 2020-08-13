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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.Principal;
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
public final class EditAnnouncementServletTest {

  private final long SAMPLE_TIME = 123456789;
  private final String SAMPLE_CLUB_NAME = "Test Club";
  private final String SAMPLE_CONTENT = "A random announcement";
  private final String SAMPLE_NEW_CONTENT = "A new announcement";
  private final String TEST_EMAIL = "test-email@gmail.com";
  private final String ID_PROP = "id";

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock Principal principal;
  private DatastoreService datastore;
  private EditAnnouncementServlet servlet;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    this.servlet = new EditAnnouncementServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
    prepare();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doPost_authorizedEdit() throws ServletException, IOException {
    helper.setEnvEmail(TEST_EMAIL).setEnvAuthDomain("google.com").setEnvIsLoggedIn(true);

    String id = TEST_EMAIL + SAMPLE_CONTENT + SAMPLE_TIME;
    when(request.getParameter(Constants.CONTENT_PROP)).thenReturn(SAMPLE_NEW_CONTENT);
    when(request.getParameter(Constants.CLUB_PROP)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(ID_PROP)).thenReturn(id);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn(TEST_EMAIL);
    servlet.doPost(request, response);

    Query query = new Query(Constants.ANNOUNCEMENT_PROP);
    PreparedQuery results = datastore.prepare(query);

    String NEW_ID = TEST_EMAIL + SAMPLE_NEW_CONTENT + SAMPLE_TIME;
    ImmutableList<Entity> entities = servlet.doPostHelper(results, SAMPLE_CLUB_NAME, NEW_ID);

    Assert.assertFalse(entities.isEmpty());
    Entity entity = entities.get(0);

    Assert.assertNotNull(entity);
    Assert.assertEquals(SAMPLE_NEW_CONTENT, entity.getProperty(Constants.CONTENT_PROP));
  }

  @Test
  public void doPost_unauthorizedEdit() throws ServletException, IOException {
    helper
        .setEnvEmail("anotherEmail@google.com")
        .setEnvAuthDomain("google.com")
        .setEnvIsLoggedIn(true);

    String SAMPLE_ID = TEST_EMAIL + SAMPLE_CONTENT + SAMPLE_TIME;
    when(request.getParameter(Constants.CONTENT_PROP)).thenReturn(SAMPLE_NEW_CONTENT);
    when(request.getParameter(Constants.CLUB_PROP)).thenReturn(SAMPLE_CLUB_NAME);
    when(request.getParameter(ID_PROP)).thenReturn(SAMPLE_ID);
    when(request.getUserPrincipal()).thenReturn(principal);
    when(principal.getName()).thenReturn("anotherEmail@google.com");
    servlet.doPost(request, response);

    Query query = new Query(Constants.ANNOUNCEMENT_PROP);
    PreparedQuery results = datastore.prepare(query);

    ImmutableList<Entity> entities = servlet.doPostHelper(results, SAMPLE_CLUB_NAME, SAMPLE_ID);

    Assert.assertFalse(entities.isEmpty());
    Entity entity = entities.get(0);

    Assert.assertNotNull(entity);
    Assert.assertEquals(SAMPLE_CONTENT, entity.getProperty(Constants.CONTENT_PROP));
  }

  private void prepare() {
    Entity announcementEntity = new Entity(Constants.ANNOUNCEMENT_PROP);
    announcementEntity.setProperty(Constants.AUTHOR_PROP, TEST_EMAIL);
    announcementEntity.setProperty(Constants.TIME_PROP, SAMPLE_TIME);
    announcementEntity.setProperty(Constants.CONTENT_PROP, SAMPLE_CONTENT);
    announcementEntity.setProperty(Constants.CLUB_PROP, SAMPLE_CLUB_NAME);

    datastore.put(announcementEntity);
  }
}
