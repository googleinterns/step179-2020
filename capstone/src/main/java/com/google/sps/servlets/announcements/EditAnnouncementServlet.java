package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/edit-announcement")
public class EditAnnouncementServlet extends AbstractAppEngineAuthorizationCodeServlet {

  private final String ID_PROP = "id";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userEmail = request.getUserPrincipal().getName();
    String id = request.getParameter(ID_PROP);
    String content = request.getParameter(Constants.CONTENT_PROP);
    String club = request.getParameter(Constants.CLUB_PROP);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query(Constants.ANNOUNCEMENT_PROP);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> entities = doPostHelper(results, club, id);

    if (entities.isEmpty()) {
      return; // No such announcement matches parameters.
    }
    Entity entity = entities.get(0);
    if (!entity.getProperty(Constants.AUTHOR_PROP).equals(userEmail)) {
      return; // Not authorized to edit this announcement!
    }

    entity.setProperty(Constants.CONTENT_PROP, content);
    entity.setProperty(Constants.EDITED_PROP, true);
    datastore.put(entity);

    response.sendRedirect("/about-us.html?name=" + club + "&tab=announcements");
  }

  ImmutableList<Entity> doPostHelper(PreparedQuery results, String clubName, String id) {
    ImmutableList<Entity> entities =
        Streams.stream(results.asIterable())
            .filter(
                entity ->
                    clubName.equals(entity.getProperty(Constants.CLUB_PROP))
                        && id.equals(
                            entity.getProperty(Constants.AUTHOR_PROP).toString()
                                + entity.getProperty(Constants.CONTENT_PROP).toString()
                                + entity.getProperty(Constants.TIME_PROP).toString()))
            .collect(toImmutableList());
    return entities;
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
