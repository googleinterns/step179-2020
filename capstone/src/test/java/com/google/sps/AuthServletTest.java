package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public final class AuthServletTest {
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private AuthServlet authServlet = new AuthServlet();
  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvIsAdmin(true);

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    localHelper.setUp();
  }

  @After
  public void tearDown() throws Exception {
    localHelper.tearDown();
  }

  private String getAuthServletResponse() throws ServletException, IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    when(response.getWriter()).thenReturn(printWriter);

    authServlet.doGet(request, response);
    return stringWriter.toString();
  }

  @Test
  public void doGet_StudentIsLoggedIn() throws ServletException, IOException {
    localHelper.setEnvIsLoggedIn(true);

    String response = getAuthServletResponse();

    Assert.assertTrue(response.contains("Logout"));
    Assert.assertTrue(!response.contains("Login"));
  }

  @Test
  public void doGet_StudentIsLoggedOut() throws ServletException, IOException {
    localHelper.setEnvIsLoggedIn(false);

    String response = getAuthServletResponse();

    Assert.assertTrue(response.contains("Login"));
    Assert.assertTrue(!response.contains("Logout"));
  }
}
