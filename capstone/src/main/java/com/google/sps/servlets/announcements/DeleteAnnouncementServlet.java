package com.google.sps.servlets;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example club content */
@WebServlet("/delete-announcement")
public class DeleteAnnouncementServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userEmail = request.getUserPrincipal().getName();
    String clubName = request.getParameter(Constants.CLUB_PROP);
    String content = request.getParameter(Constants.CONTENT_PROP);
    long time = Long.parseLong(request.getParameter(Constants.TIME_PROP));
    String author = request.getParameter(Constants.AUTHOR_PROP);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    if (!userEmail.equals(author)) {
      return; // Not authenticated to delete!
    }

    Query query =
        new Query(Constants.ANNOUNCEMENT_PROP)
            .setFilter(
                new CompositeFilter(
                    CompositeFilterOperator.AND,
                    Arrays.<Filter>asList(
                        new FilterPredicate(Constants.CLUB_PROP, FilterOperator.EQUAL, clubName),
                        new FilterPredicate(Constants.CONTENT_PROP, FilterOperator.EQUAL, content),
                        new FilterPredicate(Constants.TIME_PROP, FilterOperator.EQUAL, time),
                        new FilterPredicate(Constants.AUTHOR_PROP, FilterOperator.EQUAL, author))));

    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Key> comments =
        Streams.stream(results.asIterable()).map(Entity::getKey).collect(toImmutableList());
    datastore.delete(comments);

    response.sendRedirect("/about-us.html?name=" + clubName + "&tab=announcements");
  }
}
