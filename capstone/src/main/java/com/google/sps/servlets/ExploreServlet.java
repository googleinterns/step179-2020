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
import java.util.List;
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

    Query query = new Query(Constants.CLUB_ENTITY_PROP);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Club> clubs =
        Streams.stream(results.asIterable())
            .limit(Constants.LOAD_LIMIT)
            .map(entity -> createClubFromEntity(entity))
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
        entity.getProperty(Constants.CLUB_NAME_PROP).toString(),
        ImmutableList.copyOf((List<String>) entity.getProperty(Constants.MEMBER_PROP)),
        ImmutableList.copyOf((List<String>) entity.getProperty(Constants.OFFICER_PROP)),
        entity.getProperty(Constants.DESCRIP_PROP).toString(),
        entity.getProperty(Constants.WEBSITE_PROP).toString(),
        key);
  }
}
