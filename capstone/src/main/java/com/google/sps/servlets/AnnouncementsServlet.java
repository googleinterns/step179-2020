package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/announcements")
public class AnnouncementsServlet extends HttpServlet {

  private static final String ANNOUNCEMENTS_CONTENT_PROP = "content";
  private static final String ANNOUNCEMENT_PROP = "Announcement";
  private static final String AUTHOR_PROP = "author";
  private static final String TIME_PROP = "time";
  private static final String CONTENT_PROP = "content";
  private static final String CLUB_PROP = "club";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get announcements object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String clubName = request.getParameter(Constants.CLUB_NAME_PROP);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query =
        new Query(ANNOUNCEMENT_PROP)
            .setFilter(new FilterPredicate(CLUB_PROP, FilterOperator.EQUAL, clubName))
            .addSort("time", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Announcement> announcements =
        Streams.stream(results.asIterable())
            .limit(Constants.LOAD_LIMIT)
            .map(entity -> new Announcement(entity))
            .collect(toImmutableList());

    Gson gson = new Gson();
    String json = gson.toJson(announcements);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String clubName = request.getParameter(Constants.CLUB_NAME_PROP);
    String announcementContent = request.getParameter(ANNOUNCEMENTS_CONTENT_PROP);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // TODO need to authenticate user as club officer

    Entity announcementEntity = new Entity(ANNOUNCEMENT_PROP);
    announcementEntity.setProperty(AUTHOR_PROP, userEmail);
    announcementEntity.setProperty(TIME_PROP, System.currentTimeMillis());
    announcementEntity.setProperty(CONTENT_PROP, announcementContent);
    announcementEntity.setProperty(CLUB_PROP, clubName);

    datastore.put(announcementEntity);

    response.sendRedirect("/about-us.html?name=" + clubName + "&tab=announcements");
  }
}
