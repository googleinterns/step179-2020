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
import java.util.ArrayList;
import java.util.List;
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
    String clubName = request.getParameter(Constants.PROPERTY_NAME);
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
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
      Key clubKey = KeyFactory.createKey(Constants.CLUB_ENTITY_PROP, clubName);
      ImmutableList<String> members =
          ServletUtil.getPropertyList(clubEntity, Constants.MEMBER_PROP);
      members.stream().forEach(member -> deleteStudentClub(member, clubName, datastore));
      datastore.delete(clubKey);
    }
    response.sendRedirect("/explore.html");
  }

  private static void deleteStudentClub(
      String memberEmail, String clubName, DatastoreService datastore) {
    Query query = new Query(memberEmail);
    Entity studentEntity = datastore.prepare(query).asSingleEntity();
    if (studentEntity != null) {
      // Remove club from student's list
      List<String> clubList =
          new ArrayList<String>(
              ServletUtil.getPropertyList(studentEntity, Constants.PROPERTY_CLUBS));
      if (clubList.contains(clubName)) {
        clubList.remove(clubName);
      }
      studentEntity.setProperty(Constants.PROPERTY_CLUBS, clubList);
      datastore.put(studentEntity);
    }
  }
}
