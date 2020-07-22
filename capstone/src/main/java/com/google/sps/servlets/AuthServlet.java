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
      response
          .getWriter()
          .println(
              "<button class='auth-button' onclick='window.location.href=\""
                  + logoutUrl
                  + "\"'>Logout here</button>");
    } else {
      String loginUrl = userService.createLoginURL(redirectURL);
      response
          .getWriter()
          .println(
              "<button class='auth-button' onclick='window.location.href=\""
                  + loginUrl
                  + "\"'>Login here to view your profile</button>");
    }
  }
}
