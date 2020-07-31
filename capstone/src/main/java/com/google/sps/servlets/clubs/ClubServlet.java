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
import java.util.ArrayList;
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
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity clubEntity = retrieveClub(request, datastore).asSingleEntity();

    String name = clubEntity.getProperty(Constants.PROPERTY_NAME).toString();
    ImmutableList<String> members =
        ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.MEMBER_PROP));
    ImmutableList<String> officers =
        ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.OFFICER_PROP));
    String description = clubEntity.getProperty(Constants.DESCRIP_PROP).toString();
    String website = clubEntity.getProperty(Constants.WEBSITE_PROP).toString();
    ImmutableList<String> announcements =
        ImmutableList.copyOf((ArrayList<String>) clubEntity.getProperty(Constants.ANNOUNCE_PROP));
    Club club = new Club(name, members, officers, description, website, announcements);

    Gson gson = new Gson();
    String json = gson.toJson(club);
    response.setContentType("text/html;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    doPostHelper(request, response, blobstore, datastore);
  }

  public void doPostHelper(
      HttpServletRequest request,
      HttpServletResponse response,
      BlobstoreService blobstore,
      DatastoreService datastore)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    String founderEmail = userService.getCurrentUser().getEmail();

    // Check if club name is valid
    PreparedQuery prepared = retrieveClub(request, datastore);
    boolean isValid = Iterables.isEmpty(prepared.asIterable());

    if (isValid) {
      String clubName = request.getParameter(Constants.PROPERTY_NAME);
      String description = request.getParameter(Constants.DESCRIP_PROP);
      String website = request.getParameter(Constants.WEBSITE_PROP);
      BlobKey key = getBlobKey(request, Constants.LOGO_PROP, blobstore);

      Entity clubEntity = new Entity("Club", clubName);
      clubEntity.setProperty(Constants.PROPERTY_NAME, clubName);
      clubEntity.setProperty(Constants.DESCRIP_PROP, description);
      clubEntity.setProperty(Constants.WEBSITE_PROP, website);
      clubEntity.setProperty(Constants.MEMBER_PROP, ImmutableList.of(founderEmail));
      clubEntity.setProperty(Constants.OFFICER_PROP, ImmutableList.of(founderEmail));
      clubEntity.setProperty(Constants.ANNOUNCE_PROP, ImmutableList.of(""));
      clubEntity.setProperty(Constants.LOGO_PROP, key);
      datastore.put(clubEntity);
    }

    response.sendRedirect("/registration-msg.html?is-valid=" + isValid);
  }

  /* Return BlobKey for image uploaded through form. */
  private BlobKey getBlobKey(
      HttpServletRequest request, String formInputElementName, BlobstoreService blobstoreService) {
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

  private PreparedQuery retrieveClub(HttpServletRequest request, DatastoreService datastore) {
    Query query =
        new Query("Club")
            .setFilter(
                new FilterPredicate(
                    Constants.PROPERTY_NAME,
                    FilterOperator.EQUAL,
                    request.getParameter(Constants.PROPERTY_NAME)));
    PreparedQuery prepared = datastore.prepare(query);
    return prepared;
  }
}
