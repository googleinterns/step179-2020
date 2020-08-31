package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content. */
@WebServlet("/explore")
public class ExploreServlet extends AbstractAppEngineAuthorizationCodeServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userEmail = request.getUserPrincipal().getName();
    Gson gson = new Gson();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String sort =
        request.getParameter(Constants.SORT_PROP) == null
            ? Constants.DEFAULT
            : request.getParameter(Constants.SORT_PROP);
    Comparator<Club> comparator = getComparator(sort);

    String rawLabelsStr =
        request.getParameter(Constants.LABELS_PROP) == null
            ? ""
            : request.getParameter(Constants.LABELS_PROP);
    ImmutableList<String> rawLabels = ImmutableList.copyOf(rawLabelsStr.split(","));
    ImmutableList<String> labels =
        rawLabels.stream()
            .filter(Predicates.not(Strings::isNullOrEmpty))
            .collect(toImmutableList());
    Query query = new Query(Constants.CLUB_ENTITY_PROP);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Club> clubs =
        Streams.stream(results.asIterable())
            .map(ExploreServlet::createClubFromEntity)
            .filter(club -> matchesLabels(club, labels))
            .sorted(comparator)
            .limit(Constants.LOAD_LIMIT)
            .collect(toImmutableList());

    // Get or create student entity and store club lists
    Entity student = StudentServlet.getStudent(request, userEmail, datastore);
    ImmutableList<String> studentClubs =
        ServletUtil.getPropertyList(student, Constants.PROPERTY_CLUBS);
    ImmutableList<String> interestedClubs =
        ServletUtil.getPropertyList(student, Constants.INTERESTED_CLUB_PROP);

    ExploreInfo exploreInfo = new ExploreInfo(clubs, studentClubs, interestedClubs);
    String json = gson.toJson(exploreInfo);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private static boolean matchesLabels(Club club, ImmutableList<String> labels) {
    if (labels.isEmpty()) { // No labels means no filtering.
      return true;
    }

    ImmutableList<String> clubLabels = club.getLabels();
    ImmutableList<String> intersect =
        clubLabels.stream().filter(labels::contains).collect(toImmutableList());
    return intersect.size() == labels.size();
  }

  private static Club createClubFromEntity(Entity entity) {
    String key = "";
    if (entity.getProperty(Constants.LOGO_PROP) != null) {
      key = entity.getProperty(Constants.LOGO_PROP).toString();
    }
    return new Club(
        entity.getProperty(Constants.PROPERTY_NAME).toString(),
        ServletUtil.getPropertyList(entity, Constants.MEMBER_PROP),
        ServletUtil.getPropertyList(entity, Constants.OFFICER_PROP),
        entity.getProperty(Constants.DESCRIP_PROP).toString(),
        entity.getProperty(Constants.WEBSITE_PROP).toString(),
        key,
        entity.getProperty(Constants.CALENDAR_PROP).toString(),
        ServletUtil.getPropertyList(entity, Constants.LABELS_PROP),
        (Boolean) entity.getProperty(Constants.EXCLUSIVE_PROP),
        Long.parseLong(entity.getProperty(Constants.TIME_PROP).toString()));
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return ServletUtil.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return ServletUtil.newFlow();
  }

  private static Comparator<Club> getComparator(String sort) {
    switch (sort) {
      case Constants.ALPHA_SORT_PROP:
        return Comparator.comparing(
            club -> club.getName().toLowerCase()); // Should be case-insensitive
      case Constants.SIZE_SORT_PROP:
        return Collections.reverseOrder(Comparator.comparing(club -> club.getSize()));
      default:
        return Comparator.comparing(club -> club.getCreationTime());
    }
  }
}

class ExploreInfo {
  private ImmutableList<Club> clubs;
  private ImmutableList<String> studentClubs;
  private ImmutableList<String> studentInterestedClubs;

  public ExploreInfo(
      ImmutableList<Club> clubs,
      ImmutableList<String> studentClubs,
      ImmutableList<String> studentInterestedClubs) {
    this.clubs = clubs;
    this.studentClubs = studentClubs;
    this.studentInterestedClubs = studentInterestedClubs;
  }
}
