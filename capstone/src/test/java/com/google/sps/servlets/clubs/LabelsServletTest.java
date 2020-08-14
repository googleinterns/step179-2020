package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public class LabelsServletTest {
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private LabelsServlet servlet;
  private DatastoreService datastore;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    servlet = new LabelsServlet();
    this.datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void correctLabelsAndOrder() throws ServletException, IOException {
    String NAME = "kshao@google.com";
    String CLUB = "Club 1";
    String SITE = "www.fakesite.com";
    String DESCRIPTION = "Helping people.";
    String BLOB_KEY = "fake blob key";
    long TIME = 10;

    String LABEL_1 = "label1";
    String LABEL_2 = "label2";
    String LABEL_3 = "label3";

    helper.setEnvEmail("kshao").setEnvAuthDomain("gmail.com").setEnvIsLoggedIn(true);

    Entity club1 = new Entity(Constants.CLUB_ENTITY_PROP);
    club1.setProperty(Constants.PROPERTY_NAME, CLUB);
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(NAME));
    club1.setProperty(Constants.OFFICER_PROP, ImmutableList.of(NAME));
    club1.setProperty(Constants.DESCRIP_PROP, DESCRIPTION);
    club1.setProperty(Constants.WEBSITE_PROP, SITE);
    club1.setProperty(Constants.LOGO_PROP, BLOB_KEY);
    club1.setProperty(Constants.TIME_PROP, TIME);
    club1.setProperty(Constants.LABELS_PROP, ImmutableList.of(LABEL_1, LABEL_2));

    Entity club2 = new Entity(Constants.CLUB_ENTITY_PROP);
    club2.setProperty(Constants.PROPERTY_NAME, CLUB);
    club2.setProperty(Constants.MEMBER_PROP, ImmutableList.of(NAME));
    club2.setProperty(Constants.OFFICER_PROP, ImmutableList.of(NAME));
    club2.setProperty(Constants.DESCRIP_PROP, DESCRIPTION);
    club2.setProperty(Constants.WEBSITE_PROP, SITE);
    club2.setProperty(Constants.LOGO_PROP, BLOB_KEY);
    club2.setProperty(Constants.TIME_PROP, TIME);
    club2.setProperty(Constants.LABELS_PROP, ImmutableList.of(LABEL_1, LABEL_2, LABEL_3));

    Entity club3 = new Entity(Constants.CLUB_ENTITY_PROP);
    club3.setProperty(Constants.PROPERTY_NAME, CLUB);
    club3.setProperty(Constants.MEMBER_PROP, ImmutableList.of(NAME));
    club3.setProperty(Constants.OFFICER_PROP, ImmutableList.of(NAME));
    club3.setProperty(Constants.DESCRIP_PROP, DESCRIPTION);
    club3.setProperty(Constants.WEBSITE_PROP, SITE);
    club3.setProperty(Constants.LOGO_PROP, BLOB_KEY);
    club3.setProperty(Constants.TIME_PROP, TIME);
    club3.setProperty(Constants.LABELS_PROP, ImmutableList.of(LABEL_2));

    this.datastore.put(club1);
    this.datastore.put(club2);
    this.datastore.put(club3);

    JsonArray response = getServletResponse(servlet);
    Assert.assertEquals(3, response.size());
    Assert.assertEquals(LABEL_2, response.get(0).getAsString());
    Assert.assertEquals(LABEL_1, response.get(1).getAsString());
    Assert.assertEquals(LABEL_3, response.get(2).getAsString());
  }

  private JsonArray getServletResponse(LabelsServlet servlet) throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    servlet.doGet(request, response);

    String responseStr = stringWriter.toString().trim();
    JsonElement responseJsonElement = new JsonParser().parse(responseStr);

    return responseJsonElement.getAsJsonArray();
  }
}
