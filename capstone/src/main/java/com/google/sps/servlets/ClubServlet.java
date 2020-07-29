package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/clubs")
public class ClubServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String json =
        gson.toJson(
            PrototypeClubs.PROTOTYPE_CLUBS_MAP.get(request.getParameter(Constants.CLUB_NAME_PROP)));
    response.setContentType("text/html;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    String founderEmail = userService.getCurrentUser().getEmail();

    // Check if club name is valid
    Query query =
        new Query("Club")
            .setFilter(
                new FilterPredicate(
                    Constants.CLUB_NAME_PROP,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.CLUB_NAME_PROP)));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepared = datastore.prepare(query);
    response.setContentType("text/html;");
    boolean isValid = Iterables.isEmpty(prepared.asIterable());

    response.getWriter().println("<p>isValid = " + isValid + "</p>");
    System.out.println("isValid = " + isValid);

    String clubName = request.getParameter(Constants.CLUB_NAME_PROP);
    String description = request.getParameter(Constants.DESCRIP_PROP);
    String website = request.getParameter(Constants.WEBSITE_PROP);
    BlobKey key = getBlobKey(request, Constants.LOGO_PROP);

    Entity clubEntity = new Entity("Club", clubName);
    clubEntity.setProperty(Constants.CLUB_NAME_PROP, clubName);
    clubEntity.setProperty(Constants.DESCRIP_PROP, description);
    clubEntity.setProperty(Constants.WEBSITE_PROP, website);
    clubEntity.setProperty(Constants.MEMBER_PROP, ImmutableList.of(founderEmail));
    clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(founderEmail));
    clubEntity.setProperty(Constants.ANNOUNCE_PROP, ImmutableList.of(""));
    clubEntity.setProperty(Constants.LOGO_PROP, key);
    response.sendRedirect("/club-registration.html");
    datastore.put(clubEntity);
  }

  /* Return BlobKey for image uploaded through form. */
  private BlobKey getBlobKey(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);
    return blobKey;
  }
}
