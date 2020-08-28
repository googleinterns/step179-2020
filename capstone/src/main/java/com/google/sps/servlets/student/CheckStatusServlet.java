package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that redirects user to correct page after login */
@WebServlet("/check-status")
public class CheckStatusServlet extends AbstractAppEngineAuthorizationCodeServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = request.getUserPrincipal().getName();
    Query query = new Query(userEmail);
    PreparedQuery results = datastore.prepare(query);
    ImmutableList<Entity> students = ImmutableList.copyOf(results.asIterable());
    if (students.isEmpty()) {
      response.sendRedirect("/explore?sort=default&labels=");
    } else {
      response.sendRedirect("/explore.html");
    }
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
