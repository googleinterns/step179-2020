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

  private static final String CLUB_NAME_PROP = "name";
  private static final String ANNOUNCEMENTS_CONTENT_PROP = "content";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get announcements object based on the logged in email
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String clubName = request.getParameter(CLUB_NAME_PROP);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query =
        new Query("Announcement")
            .setFilter(new FilterPredicate("club", FilterOperator.EQUAL, clubName))
            .addSort("time", SortDirection.DESCENDING);
    // Query<Entity> query =
    //     Query.newEntityQueryBuilder()
    //         .setKind("Announcement")
    //         .setFilter(PropertyFilter.eq("club", clubName))
    //         .setOrderBy(OrderBy.desc("time"))
    //         .setLimit(10)
    //         .build();
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Announcement> announcements =
        Streams.stream(results.asIterable())
            .limit(Constants.LOAD_LIMIT)
            .map(entity -> new Announcement(entity))
            .collect(toImmutableList());

    Gson gson = new Gson();
    // ImmutableList<String> announcements =
    //     PrototypeClubs.PROTOTYPE_CLUBS_MAP
    //         .get(request.getParameter(Constants.CLUB_NAME_PROP))
    //         .getAnnouncements();
    String json = gson.toJson(announcements);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    String clubName = request.getParameter(CLUB_NAME_PROP);
    String announcementContent = request.getParameter(ANNOUNCEMENTS_CONTENT_PROP);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // TODO need to authenticate user as club officer

    Entity announcementEntity = new Entity("Announcement");
    announcementEntity.setProperty("author", userEmail);
    announcementEntity.setProperty("time", System.currentTimeMillis());
    announcementEntity.setProperty("content", announcementContent);
    announcementEntity.setProperty("club", clubName);

    datastore.put(announcementEntity);

    response.sendRedirect("/about-us.html?name=" + clubName + "&tab=announcements");
  }
}
