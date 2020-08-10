package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that updates a user's profile picture in Datastore */
@WebServlet("/profile-image")
public class ProfileImageServlet extends HttpServlet {

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
    String userEmail = userService.getCurrentUser().getEmail();
    BlobKey key = BlobstoreUtil.getBlobKey(request, Constants.PROFILE_PIC_PROP, blobstore);
    String blobKey = key != null ? key.getKeyString() : "";

    Query query = new Query(userEmail);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    if (!students.isEmpty() && students.size() == 1) {
      Entity student = students.get(0);
      student.setProperty(Constants.PROFILE_PIC_PROP, blobKey);
      datastore.put(student);
    }
    response.sendRedirect("/profile.html");
  }
}
