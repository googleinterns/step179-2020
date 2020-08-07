package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class BlobstoreUtil {
  /* Return BlobKey for image uploaded through form. */
  public BlobKey getBlobKey(
      HttpServletRequest request, String formInputElementName, BlobstoreService blobstoreService) {
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);
    return blobKey;
  }

  public void doGetHelper(
      HttpServletRequest request,
      HttpServletResponse response,
      BlobstoreService blobstore,
      String url)
      throws IOException {
    String uploadUrl = blobstore.createUploadUrl(url);

    response.setContentType("text/html");
    response.getWriter().println(uploadUrl);
  }
}
