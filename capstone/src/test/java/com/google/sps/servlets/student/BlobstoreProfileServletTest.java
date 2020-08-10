package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class BlobstoreProfileServletTest {
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private BlobstoreService blobstore;
  private BlobstoreProfileServlet blobstoreServlet;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    blobstoreServlet = new BlobstoreProfileServlet();
    blobstore = Mockito.mock(BlobstoreService.class);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_getBlobstoreUploadLink() throws ServletException, IOException {
    String sampleUrl = "sample-url";
    when(blobstore.createUploadUrl("/profile-image")).thenReturn(sampleUrl);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    // Call doGetHelper instead of doGet to ensure use of local Blobstore
    blobstoreServlet.doGetHelper(request, response, blobstore);

    Assert.assertEquals(sampleUrl, stringWriter.toString().trim());
  }
}
