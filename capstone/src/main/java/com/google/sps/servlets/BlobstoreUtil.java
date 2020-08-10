package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public final class BlobstoreUtil {
  /* Return BlobKey for image uploaded through form. */
  public static BlobKey getBlobKey(
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
}
