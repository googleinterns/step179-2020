package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When the fetch() function requests the /blobstore-upload-url URL, the content of the response is
 * the URL that allows a user to upload a file to Blobstore. If this sounds confusing, try running a
 * dev server and navigating to /blobstore-upload-url to see the Blobstore URL.
 */
@WebServlet("/blobstore-url")
public class BlobstoreServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    doGetHelper(request, response, blobstoreService);
  }

  public void doGetHelper(
      HttpServletRequest request, HttpServletResponse response, BlobstoreService blobstore)
      throws IOException {
    String uploadUrl = blobstore.createUploadUrl("/club-logo");

    response.setContentType("text/html");
    response.getWriter().println(uploadUrl);
  }
}
