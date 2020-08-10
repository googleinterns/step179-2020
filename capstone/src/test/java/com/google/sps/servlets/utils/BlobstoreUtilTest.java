package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class BlobstoreUtilTest {
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  private BlobstoreService blobstore;
  private BlobstoreUtil blobstoreUtil;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalBlobstoreServiceTestConfig(), new LocalBlobstoreServiceTestConfig());

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
  public void getBlobKey_studentUploadsProfilePicture() throws ServletException, IOException {
    String sampleBlobKey = "1234ABCD";
    Map<String, List<BlobKey>> blobs = new HashMap<String, List<BlobKey>>();
    List<BlobKey> keys = new ArrayList<BlobKey>();
    keys.add(new BlobKey(sampleBlobKey));
    blobs.put(Constants.PROFILE_PIC_PROP, keys);
    when(blobstore.getUploads(request)).thenReturn(blobs);

    BlobKey actualBlobKey =
        BlobstoreUtil.getBlobKey(request, Constants.PROFILE_PIC_PROP, blobstore);

    Assert.assertTrue(actualBlobKey != null);
    Assert.assertEquals(sampleBlobKey, actualBlobKey.getKeyString());
  }
}
