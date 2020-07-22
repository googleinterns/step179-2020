package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");
    String redirectURL = "/profile.html";

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL(redirectURL);
      response.getWriter().println("<p>Logout <a href=\"" + logoutUrl + "\">here</a></p>");
    } else {
      String loginUrl = userService.createLoginURL(redirectURL);
      response
          .getWriter()
          .println("<p>Login <a href=\"" + loginUrl + "\">here</a> to view your profile</p>");
    }
  }
}
