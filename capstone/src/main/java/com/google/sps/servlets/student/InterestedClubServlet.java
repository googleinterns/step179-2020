package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a student's interested club list */
@WebServlet("/interested-clubs")
public class InterestedClubServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get student object based on the logged in email
    String userEmail = request.getUserPrincipal().getName();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity student = getStudent(userEmail, datastore);

    if (student != null) {
      ImmutableList<String> interestedClubs =
          ServletUtil.getPropertyList(student, Constants.INTERESTED_CLUB_PROP);
      String interestedJson = convertToJsonUsingGson(interestedClubs);
      response.setContentType("application/json;");
      response.getWriter().println(interestedJson);
    }
  }

  private static String convertToJsonUsingGson(ImmutableList<String> interestedClubs) {
    Gson gson = new Gson();
    String json = gson.toJson(interestedClubs);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get student object based on the logged in email
    String userEmail = request.getUserPrincipal().getName();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity student = getStudent(userEmail, datastore);

    // TODO: ADD TESTS

    // Add interested club if necessary
    String interestedClubToJoin = request.getParameter(Constants.INTERESTED_JOIN_PROP);
    if (!Strings.isNullOrEmpty(interestedClubToJoin)) {
      // Add member to  club's interested member list and update Datastore
      Entity club = retrieveClub(interestedClubToJoin, datastore, response);
      if (club == null) {
        return;
      }
      // Update Datastore with edited entities
      addOrRemoveItemToEntity(club, datastore, userEmail, Constants.INTERESTED_MEMBER_PROP, true);
      addOrRemoveItemToEntity(
          student, datastore, interestedClubToJoin, Constants.INTERESTED_CLUB_PROP, true);
    }
    response.sendRedirect("/explore.html");
  }

  private Entity getStudent(String userEmail, DatastoreService datastore) throws IOException {
    // Get the user's information from Datastore
    Query query = new Query(userEmail);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());

    // Add user to Datastore if this is the first time they login
    if (students.isEmpty()) {
      return null;
    }
    // A user can only be logged in with one email address at a time
    return students.get(0);
  }

  private Entity retrieveClub(
      String clubName, DatastoreService datastore, HttpServletResponse response) {
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> clubs = ImmutableList.copyOf(results.asIterable());
    if (clubs.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return null;
    }
    return clubs.get(0);
  }

  private static void addOrRemoveItemToEntity(
      Entity entity,
      DatastoreService datastore,
      String itemToAddOrRemove,
      String property,
      Boolean addItem) {
    // Create empty List if property does not exist yet
    List<String> generalList = new ArrayList<String>(ServletUtil.getPropertyList(entity, property));
    if (addItem && !generalList.contains(itemToAddOrRemove)) {
      generalList.add(itemToAddOrRemove);
    }
    if (!addItem && generalList.contains(itemToAddOrRemove)) {
      generalList.remove(itemToAddOrRemove);
    }
    // Add updated entity to Datastore
    entity.setProperty(property, generalList);
    datastore.put(entity);
  }
}
