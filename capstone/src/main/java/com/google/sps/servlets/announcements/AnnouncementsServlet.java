package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.sps.gmail.EmailFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/announcements")
public class AnnouncementsServlet extends AbstractAppEngineAuthorizationCodeServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get announcements object based on the logged in email
    String userEmail = request.getUserPrincipal().getName();
    String clubName = request.getParameter(Constants.PROPERTY_NAME);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    AnnouncementsSweeper.sweepAnnouncements();

    Query query =
        new Query(Constants.ANNOUNCEMENT_PROP)
            .setFilter(new FilterPredicate(Constants.CLUB_PROP, FilterOperator.EQUAL, clubName));
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Announcement> announcements =
        Streams.stream(results.asIterable())
            .limit(Constants.LOAD_LIMIT)
            .map(
                entity ->
                    new Announcement(
                        entity.getProperty(Constants.AUTHOR_PROP).toString(),
                        entity.getProperty(Constants.CLUB_PROP).toString(),
                        Long.parseLong(entity.getProperty(Constants.TIME_PROP).toString()),
                        entity.getProperty(Constants.CONTENT_PROP).toString(),
                        userEmail.equals(entity.getProperty(Constants.AUTHOR_PROP).toString()),
                        ServletUtil.getNameByEmail(
                            entity.getProperty(Constants.AUTHOR_PROP).toString()),
                        Boolean.parseBoolean(entity.getProperty(Constants.EDITED_PROP).toString()),
                        ServletUtil.getPictureByEmail(
                            entity.getProperty(Constants.AUTHOR_PROP).toString())))
            .collect(toImmutableList())
            .reverse();

    Gson gson = new Gson();
    String json = gson.toJson(announcements);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userEmail = request.getUserPrincipal().getName();
    String clubName = request.getParameter(Constants.PROPERTY_NAME);
    String announcementContent = request.getParameter(Constants.CONTENT_PROP);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Club club = getClub(datastore, clubName);
    if (!club.hasOfficer(userEmail)) {
      return; // Not authenticated to post.
    }

    Entity announcementEntity = new Entity(Constants.ANNOUNCEMENT_PROP);
    announcementEntity.setProperty(Constants.AUTHOR_PROP, userEmail);
    announcementEntity.setProperty(Constants.TIME_PROP, System.currentTimeMillis());
    announcementEntity.setProperty(Constants.CONTENT_PROP, announcementContent);
    announcementEntity.setProperty(Constants.CLUB_PROP, clubName);
    announcementEntity.setProperty(Constants.EDITED_PROP, false);

    datastore.put(announcementEntity);
    EmailFactory.sendEmailToAllMembers(clubName, announcementEntity);

    response.sendRedirect("/about-us.html?name=" + clubName + "&tab=announcements");
  }

  public static Gmail getGmailService() throws IOException, GeneralSecurityException {
    final NetHttpTransport GMAIL_HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    Credential credential = ServletUtil.newFlow().loadCredential(userId);
    return new Gmail.Builder(GMAIL_HTTP_TRANSPORT, Constants.JSON_FACTORY, credential)
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

  private Club getClub(DatastoreService datastore, String clubName) {
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
    Entity entity = datastore.prepare(query).asSingleEntity();
    if (entity == null) {
      return null;
    }
    String key = "";
    if (entity.getProperty(Constants.LOGO_PROP) != null) {
      key = entity.getProperty(Constants.LOGO_PROP).toString();
    }

    return new Club(
        entity.getProperty(Constants.PROPERTY_NAME).toString(),
        ImmutableList.copyOf((ArrayList<String>) entity.getProperty(Constants.MEMBER_PROP)),
        ImmutableList.copyOf((ArrayList<String>) entity.getProperty(Constants.OFFICER_PROP)),
        entity.getProperty(Constants.DESCRIP_PROP).toString(),
        entity.getProperty(Constants.WEBSITE_PROP).toString(),
        key,
        entity.getProperty(Constants.CALENDAR_PROP).toString(),
        ServletUtil.getPropertyList(entity, Constants.LABELS_PROP),
        Long.parseLong(entity.getProperty(Constants.TIME_PROP).toString()));
  }
}
