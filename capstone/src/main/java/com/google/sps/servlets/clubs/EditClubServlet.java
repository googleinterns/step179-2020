package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

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
@WebServlet("/club-edit")
public class EditClubServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    String founderEmail = userService.getCurrentUser().getEmail();
    ImmutableList<String> officers =
        ImmutableList.copyOf(request.getParameter(Constants.OFFICER_PROP).split(","));

    Query query =
        new Query("Club")
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();

    // Only accepts officers that are listed as members of the club
    if (clubEntity != null) {
      ImmutableList<String> members =
          ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.MEMBER_PROP));

      ImmutableList<String> intersect =
          members.stream().filter(officers::contains).collect(toImmutableList());

      boolean isInvalid = intersect.isEmpty();
      // If all officers are invalid, officers list does not change
      if (intersect.isEmpty()) {
        intersect =
            ImmutableList.copyOf(
                (ArrayList<String>) clubEntity.getProperty(Constants.OFFICER_PROP));
      }

      clubEntity.setProperty(Constants.DESCRIP_PROP, request.getParameter(Constants.DESCRIP_PROP));
      clubEntity.setProperty(Constants.WEBSITE_PROP, request.getParameter(Constants.WEBSITE_PROP));
      clubEntity.setProperty(Constants.OFFICER_PROP, intersect);
      datastore.put(clubEntity);

      response.sendRedirect(
          "/about-us.html?name="
              + clubEntity.getProperty(Constants.PROPERTY_NAME)
              + "&is-invalid="
              + isInvalid);
    }
  }
}