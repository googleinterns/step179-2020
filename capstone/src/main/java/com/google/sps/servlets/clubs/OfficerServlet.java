package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/officer")
public class OfficerServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String club = request.getParameter(Constants.CLUB_NAME_PROP);
    UserService userService = UserServiceFactory.getUserService();
    String userEmail = userService.getCurrentUser().getEmail();
    boolean isOfficer = isUserOfficer(userEmail, club);
    response.setContentType("text/plain");
    response.getWriter().println(isOfficer);
  }

  public boolean isUserOfficer(String user, String clubName) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
    Entity entity = datastore.prepare(query).asSingleEntity();
    if (entity == null) {
      return false;
    }
    Club club =
        new Club(
            entity.getProperty(Constants.CLUB_NAME_PROP).toString(),
            ImmutableList.copyOf((ArrayList<String>) entity.getProperty(Constants.MEMBER_PROP)),
            ImmutableList.copyOf((ArrayList<String>) entity.getProperty(Constants.OFFICER_PROP)),
            entity.getProperty(Constants.DESCRIP_PROP).toString(),
            entity.getProperty(Constants.WEBSITE_PROP).toString());
    return club.hasOfficer(user);
  }
}