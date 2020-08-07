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
public class BlobstoreServletTest {
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private BlobstoreService blobstore;
  private BlobstoreUtil blobstoreUtil;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalBlobstoreServiceTestConfig());

  @Before
  public void setUp() throws IOException {
    helper.setUp();
    MockitoAnnotations.initMocks(this);
    blobstoreUtil = new BlobstoreUtil();
    blobstore = Mockito.mock(BlobstoreService.class);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_getBlobstoreUploadLink() throws ServletException, IOException {
    when(blobstore.createUploadUrl("/clubs")).thenReturn("sample-url");

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    blobstoreUtil.doGetHelper(request, response, blobstore, "/clubs");
    Assert.assertEquals("sample-url", stringWriter.toString().trim());
  }
}
