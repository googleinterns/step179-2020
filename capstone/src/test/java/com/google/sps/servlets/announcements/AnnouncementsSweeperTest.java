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
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
public final class AnnouncementsSweeperTest {

  private final long SAMPLE_TIME = 123456789;
  private final String SAMPLE_CLUB_NAME = "Test Club";
  private final String SAMPLE_CONTENT = "A random announcement";
  private final String CONTENT_1 = "Thing 1";
  private final String CONTENT_2 = "Thing 2";
  private final String CONTENT_3 = "Thing 3";
  private final String CONTENT_4 = "Thing 4";
  private final String TEST_EMAIL = "test-email@gmail.com";

  private final long TIME_10 = 10;
  private final long TIME_20 = 20;
  private final long TIME_30 = 30;
  private final long TIME_40 = 40;

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private DatastoreService datastore;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException, ServletException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void sweepAnnouncements_expectedBehavior() throws ServletException, IOException {
    Instant instant = Instant.ofEpochMilli(0);
    AnnouncementsSweeper.setClock(Clock.fixed(instant, ZoneId.of("Z")));

    Entity announcement1 = new Entity(Constants.FUTURE_ANNOUNCEMENT_PROP);
    announcement1.setProperty(Constants.AUTHOR_PROP, TEST_EMAIL);
    announcement1.setProperty(Constants.CLUB_PROP, SAMPLE_CLUB_NAME);
    announcement1.setProperty(Constants.CONTENT_PROP, CONTENT_1);
    announcement1.setProperty(Constants.TIME_PROP, TIME_10);
    announcement1.setProperty(Constants.EDITED_PROP, false);

    Entity announcement2 = new Entity(Constants.FUTURE_ANNOUNCEMENT_PROP);
    announcement2.setProperty(Constants.AUTHOR_PROP, TEST_EMAIL);
    announcement2.setProperty(Constants.CLUB_PROP, SAMPLE_CLUB_NAME);
    announcement2.setProperty(Constants.CONTENT_PROP, CONTENT_2);
    announcement2.setProperty(Constants.TIME_PROP, TIME_20);
    announcement2.setProperty(Constants.EDITED_PROP, false);

    Entity announcement3 = new Entity(Constants.FUTURE_ANNOUNCEMENT_PROP);
    announcement3.setProperty(Constants.AUTHOR_PROP, TEST_EMAIL);
    announcement3.setProperty(Constants.CLUB_PROP, SAMPLE_CLUB_NAME);
    announcement3.setProperty(Constants.CONTENT_PROP, CONTENT_3);
    announcement3.setProperty(Constants.TIME_PROP, TIME_30);
    announcement3.setProperty(Constants.EDITED_PROP, false);

    Entity announcement4 = new Entity(Constants.FUTURE_ANNOUNCEMENT_PROP);
    announcement4.setProperty(Constants.AUTHOR_PROP, TEST_EMAIL);
    announcement4.setProperty(Constants.CLUB_PROP, SAMPLE_CLUB_NAME);
    announcement4.setProperty(Constants.CONTENT_PROP, CONTENT_4);
    announcement4.setProperty(Constants.TIME_PROP, TIME_40);
    announcement4.setProperty(Constants.EDITED_PROP, false);

    datastore.put(announcement1);
    datastore.put(announcement2);
    datastore.put(announcement3);
    datastore.put(announcement4);

    instant = Instant.ofEpochMilli(25); // So only announcements 1 and 2 should post.
    AnnouncementsSweeper.setClock(Clock.fixed(instant, ZoneId.of("Z")));
    AnnouncementsSweeper.sweepAnnouncements();

    Assert.assertTrue(checkForAnnouncement(SAMPLE_CLUB_NAME, CONTENT_1));
    Assert.assertTrue(checkForAnnouncement(SAMPLE_CLUB_NAME, CONTENT_2));
    Assert.assertFalse(checkForAnnouncement(SAMPLE_CLUB_NAME, CONTENT_3));
    Assert.assertFalse(checkForAnnouncement(SAMPLE_CLUB_NAME, CONTENT_4));
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
