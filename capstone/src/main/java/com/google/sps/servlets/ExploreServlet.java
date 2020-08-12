package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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

    Query query = new Query(Constants.CLUB_ENTITY_PROP);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Club> clubs =
        Streams.stream(results.asIterable())
            .map(entity -> createClubFromEntity(entity))
            .sorted(comparator)
            .limit(Constants.LOAD_LIMIT)
            .collect(toImmutableList());

    String json = gson.toJson(clubs);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private Club createClubFromEntity(Entity entity) {
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
        Long.parseLong(entity.getProperty(Constants.TIME_PROP).toString()));
  }

  private Comparator<Club> getComparator(String sort) {
    if (sort.equals(Constants.ALPHA_SORT_PROP)) {
      return Comparator.comparing(
          club -> club.getName().toLowerCase()); // Should be case-insensitive
    } else if (sort.equals(Constants.SIZE_SORT_PROP)) {
      return Collections.reverseOrder(Comparator.comparing(club -> club.getSize()));
    } else {
      return Comparator.comparing(club -> club.getCreationTime());
    }
  }
}
