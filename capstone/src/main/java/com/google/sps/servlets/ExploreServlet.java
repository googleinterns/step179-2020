package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content. */
@WebServlet("/explore")
public class ExploreServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String sort = request.getParameter(Constants.SORT_PROP);
    Comparator<Club> comparator = getComparator(sort);

    ImmutableList<String> rawLabels =
        ImmutableList.copyOf(request.getParameter(Constants.LABELS_PROP).split(","));
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

    String json = gson.toJson(clubs);
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
        ServletUtil.getPropertyList(entity, Constants.LABELS_PROP),
        Long.parseLong(entity.getProperty(Constants.TIME_PROP).toString()));
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
