package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.calendar.Calendar;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/clubs")
public class ClubServlet extends AbstractAppEngineAuthorizationCodeServlet {
  private final String SCOPE_TYPE = "user";
  private final String USER_CALENDAR_PERMISSIONS = "reader";
  private static final HttpTransport HTTP_TRANSPORT = UrlFetchTransport.getDefaultInstance();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = request.getUserPrincipal().getName();

    ImmutableList<String> studentClubs =
        getStudentClubList(userEmail, Constants.PROPERTY_CLUBS, datastore);
    ImmutableList<String> interestedClubs =
        getStudentClubList(userEmail, Constants.INTERESTED_CLUB_PROP, datastore);

    Entity clubEntity = retrieveClub(request, datastore).asSingleEntity();
    if (clubEntity != null) {
      String name = clubEntity.getProperty(Constants.PROPERTY_NAME).toString();
      ImmutableList<String> members =
          ServletUtil.getPropertyList(clubEntity, Constants.MEMBER_PROP);
      ImmutableList<String> officers =
          ServletUtil.getPropertyList(clubEntity, Constants.OFFICER_PROP);
      ImmutableList<String> labels = ServletUtil.getPropertyList(clubEntity, Constants.LABELS_PROP);
      String description = clubEntity.getProperty(Constants.DESCRIP_PROP).toString();
      String website = clubEntity.getProperty(Constants.WEBSITE_PROP).toString();
      String logoKey = "";
      if (clubEntity.getProperty(Constants.LOGO_PROP) != null) {
        logoKey = clubEntity.getProperty(Constants.LOGO_PROP).toString();
      }
      String calendar = clubEntity.getProperty(Constants.CALENDAR_PROP).toString();
      boolean isExclusive = (Boolean) clubEntity.getProperty(Constants.EXCLUSIVE_PROP);
      ImmutableList<String>
          requests = // ImmutableList.of("test-email@google.com", "hi there", "hewwo");
          ServletUtil.getPropertyList(clubEntity, Constants.REQUEST_PROP);
      long creationTime = Long.parseLong(clubEntity.getProperty(Constants.TIME_PROP).toString());
      if (!members.contains(userEmail) && isExclusive) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      Club club =
          new Club(
              name,
              members,
              officers,
              description,
              website,
              logoKey,
              calendar,
              labels,
              isExclusive,
              requests,
              creationTime);
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(club);
      boolean isOfficer = officers.contains(userEmail);
      jsonElement.getAsJsonObject().addProperty("isOfficer", isOfficer);
      ClubInfo clubInfo = new ClubInfo(jsonElement, studentClubs, interestedClubs);
      String json = gson.toJson(clubInfo);
      response.setContentType("text/html;");
      response.getWriter().println(json);
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    try {
      Calendar service = getCalendarService();
      doPostHelper(request, response, datastore, service);
    } catch (GeneralSecurityException calendarError) {
      response.getWriter().println(calendarError);
    }
  }

  public void doPostHelper(
      HttpServletRequest request,
      HttpServletResponse response,
      DatastoreService datastore,
      Calendar service)
      throws IOException {
    // Must use UserService to access logged in user here
    UserService userService = UserServiceFactory.getUserService();
    String founderEmail = userService.getCurrentUser().getEmail();

    // Check if club name is valid
    PreparedQuery prepared = retrieveClub(request, datastore);
    boolean isValid = Iterables.isEmpty(prepared.asIterable());

    if (isValid) {
      String clubName = request.getParameter(Constants.PROPERTY_NAME);
      String description = request.getParameter(Constants.DESCRIP_PROP);
      String website = request.getParameter(Constants.WEBSITE_PROP);
      boolean isExclusive = request.getParameter(Constants.EXCLUSIVE_PROP) != null;
      String calendarId = "";
      try {
        calendarId = createCalendar(clubName, service);
      } catch (Exception entityError) {
        response.getWriter().println("Error: " + entityError);
      }
      Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP, clubName);
      clubEntity.setProperty(Constants.PROPERTY_NAME, clubName);
      clubEntity.setProperty(Constants.DESCRIP_PROP, description);
      clubEntity.setProperty(Constants.WEBSITE_PROP, website);
      clubEntity.setProperty(Constants.MEMBER_PROP, ImmutableList.of(founderEmail));
      clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(founderEmail));
      clubEntity.setProperty(Constants.LABELS_PROP, ImmutableList.of());
      clubEntity.setProperty(Constants.TIME_PROP, System.currentTimeMillis());
      clubEntity.setProperty(Constants.LOGO_PROP, "");
      clubEntity.setProperty(Constants.CALENDAR_PROP, calendarId);
      clubEntity.setProperty(Constants.EXCLUSIVE_PROP, isExclusive);
      // clubEntity.setProperty(Constants.REQUEST_PROP, ImmutableList.of());
      clubEntity.setProperty(
          Constants.REQUEST_PROP, ImmutableList.of("test-email@google.com", "hi there", "hewwo"));
      datastore.put(clubEntity);
      addClubToFoundersClubList(datastore, founderEmail, clubName);
    }
    response.sendRedirect("/registration-msg.html?is-valid=" + isValid);
  }

  private static ImmutableList<String> getStudentClubList(
      String userEmail, String property, DatastoreService datastore) {
    Query query = new Query(userEmail);
    PreparedQuery results = datastore.prepare(query);
    Entity student = results.asSingleEntity();
    if (student == null) {
      return null;
    }
    return ServletUtil.getPropertyList(student, property);
  }

  private void addClubToFoundersClubList(
      DatastoreService datastore, String founderEmail, String clubName) {
    Query query = new Query(founderEmail);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());

    // Update founder's club list with registered club
    if (!students.isEmpty() && students.size() == 1) {
      Entity student = students.get(0);
      List<String> clubList =
          new ArrayList(ServletUtil.getPropertyList(student, Constants.PROPERTY_CLUBS));
      if (!clubList.contains(clubName)) {
        clubList.add(clubName);
      }
      student.setProperty(Constants.PROPERTY_CLUBS, clubList);
      datastore.put(student);
    }
  }

  private PreparedQuery retrieveClub(HttpServletRequest request, DatastoreService datastore) {
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    PreparedQuery prepared = datastore.prepare(query);
    return prepared;
  }

  /** Return the Calendar ID after creating a calendar for the given club name. */
  private String createCalendar(String clubName, Calendar service)
      throws IOException, GeneralSecurityException {
    com.google.api.services.calendar.model.Calendar calendar =
        new com.google.api.services.calendar.model.Calendar()
            .setSummary(clubName + " Calendar")
            .setTimeZone(Constants.TIME_ZONE);
    String createdCalendarId = service.calendars().insert(calendar).execute().getId();
    return createdCalendarId;
  }

  public static Calendar getCalendarService() throws IOException, GeneralSecurityException {
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    Credential credential = ServletUtil.newFlow().loadCredential(userId);
    return new Calendar.Builder(HTTP_TRANSPORT, Constants.JSON_FACTORY, credential)
        .setApplicationName(Constants.GOOGLE_APPLICATION_NAME)
        .build();
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return ServletUtil.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return ServletUtil.newFlow();
  }
}

class ClubInfo {
  private JsonElement club;
  private ImmutableList<String> studentClubs;
  private ImmutableList<String> studentInterestedClubs;

  public ClubInfo(
      JsonElement club,
      ImmutableList<String> studentClubs,
      ImmutableList<String> studentInterestedClubs) {
    this.club = club;
    this.studentClubs = studentClubs;
    this.studentInterestedClubs = studentInterestedClubs;
  }
}
