package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/club-edit")
public class EditClubServlet extends AbstractAppEngineAuthorizationCodeServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    ImmutableList<String> officers;
    String newOfficerList = request.getParameter(Constants.OFFICER_PROP);
    if (newOfficerList != null && !newOfficerList.isEmpty()) {
      officers = ImmutableList.copyOf(newOfficerList.split(","));
    } else {
      officers = ImmutableList.of();
    }
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    Entity clubEntity = datastore.prepare(query).asSingleEntity();
    // Only accepts officers that are listed as members of the club
    if (clubEntity != null) {
      ImmutableList<String> currentOfficers =
          ServletUtil.getPropertyList(clubEntity, Constants.OFFICER_PROP);
      if (!currentOfficers.contains(userEmail)) {
        return; // Not authenticated to post
      }
      ImmutableList<String> members =
          ServletUtil.getPropertyList(clubEntity, Constants.MEMBER_PROP);

      ImmutableList<String> intersect =
          members.stream().filter(officers::contains).collect(toImmutableList());

      boolean isInvalid = intersect.isEmpty();
      // If all officers are invalid, officers list does not change
      if (intersect.isEmpty()) {
        intersect = ServletUtil.getPropertyList(clubEntity, Constants.OFFICER_PROP);
      }

      boolean isExclusive = request.getParameter(Constants.EXCLUSIVE_PROP) != null;
      ImmutableList<String> requests =
          ServletUtil.getPropertyList(clubEntity, Constants.REQUEST_PROP);
      requests.stream()
          .forEach(
              joinRequest ->
                  updateMemberRequestList(joinRequest, request, clubEntity, isExclusive));

      String newLabelsList = request.getParameter(Constants.LABELS_PROP);
      ImmutableList<String> rawLabels =
          Strings.isNullOrEmpty(newLabelsList)
              ? ImmutableList.of()
              : ImmutableList.copyOf(newLabelsList.split(","));
      ImmutableList<String> labels =
          rawLabels.stream()
              .map(
                  label ->
                      label
                          .toLowerCase()
                          .replaceAll("\\s", "")) // Removes all whitespace and moves to lower case.
              .filter(Predicates.not(Strings::isNullOrEmpty))
              .collect(toImmutableList());

      clubEntity.setProperty(Constants.DESCRIP_PROP, request.getParameter(Constants.DESCRIP_PROP));
      clubEntity.setProperty(Constants.WEBSITE_PROP, request.getParameter(Constants.WEBSITE_PROP));
      clubEntity.setProperty(Constants.OFFICER_PROP, intersect);
      clubEntity.setProperty(Constants.LABELS_PROP, labels);
      clubEntity.setProperty(Constants.EXCLUSIVE_PROP, isExclusive);

      datastore.put(clubEntity);
      response.sendRedirect(
          "/about-us.html?name="
              + clubEntity.getProperty(Constants.PROPERTY_NAME)
              + "&is-invalid="
              + isInvalid);
    }
  }

  private void updateMemberRequestList(
      String nameToUpdate, HttpServletRequest request, Entity clubEntity, boolean isExclusive) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    if (request.getParameter(nameToUpdate) != null || !isExclusive) {
      clubEntity = ServletUtil.addItemToEntity(clubEntity, nameToUpdate, Constants.MEMBER_PROP);
      clubEntity =
          ServletUtil.removeItemFromEntity(clubEntity, nameToUpdate, Constants.REQUEST_PROP);
      Query query = new Query(nameToUpdate);
      Entity student = datastore.prepare(query).asSingleEntity();
      ServletUtil.addItemToEntity(
          student,
          clubEntity.getProperty(Constants.PROPERTY_NAME).toString(),
          Constants.PROPERTY_CLUBS);
      datastore.put(student);
      datastore.put(clubEntity);
    }
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
