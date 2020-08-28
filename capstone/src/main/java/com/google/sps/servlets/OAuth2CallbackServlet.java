package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Callback servlet handles callbacks for OAuth.
@WebServlet("/oauth2callback")
public class OAuth2CallbackServlet extends AbstractAppEngineAuthorizationCodeCallbackServlet {
  String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();

  // On success the callback servlet redirects to the main servlet.
  @Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/explore.html");
    resp.getWriter()
        .print(userEmail + " is logged in and has given access to their calendar and Gmail.");
  }

  // On failure (i.e user denies access) the callback servlet displays a simple error message."
  @Override
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    resp.getWriter().print(userEmail + " has not given access to their calendar and/or Gmail");
    resp.setStatus(200);
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
