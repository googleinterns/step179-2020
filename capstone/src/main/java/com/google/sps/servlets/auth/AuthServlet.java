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
      response.getWriter().println("<a href=\"" + logoutUrl + "\">Logout</a>");
    } else {
      String loginUrl = userService.createLoginURL(redirectURL);
      response.getWriter().println(getLoginInfo(loginUrl));
    }
  }

  private static String getLoginInfo(String loginUrl) {
    String loginIntro =
        "<h1 class='login-intro'>Welcome to the ClubHub Profile page! Please login below to view your profile</h1>";
    String loginButton =
        "<button class='login-button' onclick='window.location.href=\""
            + loginUrl
            + "\"'>Login Here</button>";
    return loginIntro + loginButton;
  }
}
