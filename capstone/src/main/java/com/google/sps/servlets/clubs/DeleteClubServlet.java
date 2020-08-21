package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/delete-club")
public class DeleteClubServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    PreparedQuery prepared = datastore.prepare(query);
    Entity clubEntity = prepared.asSingleEntity();
    if (clubEntity == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
      ImmutableList<String> currentOfficers =
          ServletUtil.getPropertyList(clubEntity, Constants.OFFICER_PROP);
      if (!currentOfficers.contains(userEmail)) {
        return; // Not authenticated to post
      }
      Key clubKey =
          KeyFactory.createKey(
              Constants.CLUB_ENTITY_PROP, request.getParameter(Constants.PROPERTY_NAME));
      datastore.delete(clubKey);
    }
    response.sendRedirect("/index.html");
  }
}
