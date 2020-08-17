package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/clubs")
public class ClubServlet extends HttpServlet {
  private final String SCOPE_TYPE = "user";
  private final String USER_CALENDAR_PERMISSIONS = "reader";
  protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  protected static final List<String> SCOPES =
      ImmutableList.of(
          "https://www.googleapis.com/auth/calendar",
          "https://www.googleapis.com/auth/calendar.events",
          "https://www.googleapis.com/auth/calendar.events.readonly",
          "https://www.googleapis.com/auth/calendar.readonly",
          "https://www.googleapis.com/auth/calendar.settings.readonly",
          "https://www.googleapis.com/calendar/v3/calendars",
          "https://www.googleapis.com/auth/calendar.events.public.readonly",
          "https://www.googleapis.com/auth/calendar.app.created");
  private static final String CREDENTIALS_FILE_PATH =
      "/tmp/tmp.OF0kWf8TmL/application_default_credentials.json";
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final String CLIENT_ID =
      "264953829929-ffv5q12afu4a7bm3vd7pnlfg8d96tlom.apps.googleusercontent.com";
  private static final String CLIENT_SECRET = "-g4ch6SGO-gu5CCpoY6pQ9qH";
  private static final String REFRESH_TOKEN =
      "4/3AGw35EXK_fBdvnlMvImHlYf86ulkGJJsG8eOUYIJqik0I1ceAJtucEwV_ilQYjGJXNW7_GT_YRK2MiO4faLwGY";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = request.getUserPrincipal().getName();

    Entity clubEntity = retrieveClub(request, datastore).asSingleEntity();
    if (clubEntity != null) {
      String name = clubEntity.getProperty(Constants.PROPERTY_NAME).toString();
      ImmutableList<String> members =
          ServletUtil.getPropertyList(clubEntity, Constants.MEMBER_PROP);
      ImmutableList<String> officers =
          ServletUtil.getPropertyList(clubEntity, Constants.OFFICER_PROP);
      String description = clubEntity.getProperty(Constants.DESCRIP_PROP).toString();
      String website = clubEntity.getProperty(Constants.WEBSITE_PROP).toString();
      String logoKey = "";
      if (clubEntity.getProperty(Constants.LOGO_PROP) != null) {
        logoKey = clubEntity.getProperty(Constants.LOGO_PROP).toString();
      }
      String calendar = clubEntity.getProperty("calendar").toString();
      boolean isOfficer = officers.contains(userEmail);
      long creationTime = Long.parseLong(clubEntity.getProperty(Constants.TIME_PROP).toString());
      Club club =
          new Club(name, members, officers, description, website, logoKey, calendar, creationTime);
      Gson gson = new Gson();
      JsonElement jsonElement = gson.toJsonTree(club);
      jsonElement.getAsJsonObject().addProperty("isOfficer", isOfficer);
      String json = gson.toJson(jsonElement);
      response.setContentType("text/html;");
      response.getWriter().println(json);
    } else {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    doPostHelper(request, response, datastore);
  }

  public void doPostHelper(
      HttpServletRequest request, HttpServletResponse response, DatastoreService datastore)
      throws IOException {
    // Must use UserService to access logged in user here
    UserService userService = UserServiceFactory.getUserService();
    String founderEmail = userService.getCurrentUser().getEmail();
    // String userEmail = request.getUserPrincipal().getName();

    // Check if club name is valid

    PreparedQuery prepared = retrieveClub(request, datastore);
    boolean isValid = Iterables.isEmpty(prepared.asIterable());

    if (isValid) {
      String clubName = request.getParameter(Constants.PROPERTY_NAME);
      String description = request.getParameter(Constants.DESCRIP_PROP);
      String website = request.getParameter(Constants.WEBSITE_PROP);
      String calendarId;
      try {
        calendarId = createCalendar(clubName);
        // setGroupCalendarId(clubName, calendarId);
      } catch (Exception entityError) {
        System.out.println("error is: " + entityError);
        calendarId = entityError.toString();
        // response.getWriter().println("Error");
      }
      //   calendarId = createCalendar(clubName);
      Entity clubEntity = new Entity(Constants.CLUB_ENTITY_PROP, clubName);
      clubEntity.setProperty(Constants.PROPERTY_NAME, clubName);
      clubEntity.setProperty(Constants.DESCRIP_PROP, description);
      clubEntity.setProperty(Constants.WEBSITE_PROP, website);
      clubEntity.setProperty(Constants.MEMBER_PROP, ImmutableList.of(founderEmail));
      clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(founderEmail));
      clubEntity.setProperty(Constants.TIME_PROP, System.currentTimeMillis());
      clubEntity.setProperty(Constants.LOGO_PROP, "");
      clubEntity.setProperty("calendar", calendarId);
      datastore.put(clubEntity);
      addClubToFoundersClubList(datastore, founderEmail, clubName);
    }
    response.sendRedirect("/registration-msg.html?is-valid=" + isValid);
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
  public String createCalendar(String clubName) throws IOException, GeneralSecurityException {
    com.google.api.services.calendar.model.Calendar calendar =
        new com.google.api.services.calendar.model.Calendar()
            .setSummary(clubName + " calendar")
            .setTimeZone("America/Los_Angeles");
    Calendar service = getCalendarService();
    String createdCalendarId = service.calendars().insert(calendar).execute().getId();
    System.out.println("id: " + createdCalendarId);

    // Enable reader permission for user
    // AclRule rule =
    //     new AclRule().setScope(new
    // Scope().setType(SCOPE_TYPE)).setRole(USER_CALENDAR_PERMISSIONS);
    // service.acl().insert(createdCalendarId, rule).execute();

    return createdCalendarId;
  }

  private Calendar getCalendarService() throws IOException, GeneralSecurityException {
    // final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    final HttpTransport HTTP_TRANSPORT = UrlFetchTransport.getDefaultInstance();
    // Credential credential =
    //     new GoogleCredential.Builder()
    //         .setTransport(GoogleNetHttpTransport.newTrustedTransport())
    //         .setJsonFactory(JSON_FACTORY)
    //         .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
    //         .build()
    //         .setAccessToken(getAccessToken())
    //         .setRefreshToken(REFRESH_TOKEN);
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    Credential credential = newFlow().loadCredential(userId);
    return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName("google.com:clubhub-step-2020")
        .build();

    // return new Calendar.Builder(
    //         HTTP_TRANSPORT, JSON_FACTORY, new AppIdentityCredential(CalendarScopes.all()))
    //     .setApplicationName("google.com:clubhub-step-2020")
    //     .build();

    // return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
    //     .setApplicationName("google.com:clubhub-step-2020")
    //     .build();
  }

  private static Credential getCredentials(final HttpTransport HTTP_TRANSPORT) throws IOException {
    // Load client secrets.
    InputStream in = ClubServlet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  private static String getAccessToken() {
    try {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("grant_type", "refresh_token");
      params.put("client_id", CLIENT_ID);
      params.put("client_secret", CLIENT_SECRET);
      params.put("refresh_token", REFRESH_TOKEN);

      StringBuilder postData = new StringBuilder();
      for (Map.Entry<String, Object> param : params.entrySet()) {
        if (postData.length() != 0) {
          postData.append('&');
        }
        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
        postData.append('=');
        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
      }

      byte[] postDataBytes = postData.toString().getBytes("UTF-8");

      URL url = new URL("https://accounts.google.com/o/oauth2/token");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setUseCaches(false);
      con.setRequestMethod("POST");
      con.getOutputStream().write(postDataBytes);

      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      StringBuffer buffer = new StringBuffer();
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        buffer.append(line);
      }

      JSONObject json = new JSONObject(buffer.toString());
      String accessToken = json.getString("access_token");
      return accessToken;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  static GoogleAuthorizationCodeFlow newFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            getClientCredential(),
            Collections.singleton(CalendarScopes.CALENDAR))
        .setDataStoreFactory(AppEngineDataStoreFactory.getDefaultInstance())
        .setAccessType("offline")
        .build();
  }

  static GoogleClientSecrets getClientCredential() throws IOException {
    System.out.println(ClubServlet.class.getResourceAsStream("/client_secrets.json"));
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(
            JSON_FACTORY,
            new InputStreamReader(ClubServlet.class.getResourceAsStream("/client_secrets.json")));

    return clientSecrets;
  }
}
