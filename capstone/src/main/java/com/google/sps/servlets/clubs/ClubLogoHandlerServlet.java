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
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Servlet that stores and returns data relating to clubs. */
@WebServlet("/club-logo")
public class ClubLogoHandlerServlet extends HttpServlet {

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
    Entity clubEntity = retrieveClub(request, datastore).asSingleEntity();
    BlobKey key = BlobstoreUtil.getBlobKey(request, Constants.LOGO_PROP, blobstore);
    String blobKey = key != null ? key.getKeyString() : "";
    if (clubEntity != null) {
      clubEntity.setProperty(Constants.LOGO_PROP, blobKey);
      datastore.put(clubEntity);
    }
    response.sendRedirect(
        "/about-us.html?name="
            + clubEntity.getProperty(Constants.PROPERTY_NAME)
            + "&is-invalid=false");
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
