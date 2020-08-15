package com.google.sps.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

public class GmailAPILoader {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String APPLICATION_NAME = "clubhub-step-2020";

  public static Gmail getGmailService() throws IOException, GeneralSecurityException {
    // Add credentials for using API
    Credential authorize =
        new GoogleCredential.Builder()
            .setTransport(GoogleNetHttpTransport.newTrustedTransport())
            .setJsonFactory(JSON_FACTORY)
            .setClientSecrets(Secrets.CLIENT_ID, Secrets.CLIENT_SECRET)
            .build()
            .setRefreshToken(Secrets.REFRESH_TOKEN)
            .setAccessToken(getAccessToken());

    // Create Gmail API Service
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Gmail service =
        new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
            .setApplicationName(APPLICATION_NAME)
            .build();
    return service;
  }

  private static String getAccessToken() throws IOException {
    try {
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("grant_type", "refresh_token");
      params.put("client_id", Secrets.CLIENT_ID);
      params.put("client_secret", Secrets.CLIENT_SECRET);
      params.put("refresh_token", Secrets.REFRESH_TOKEN);

      StringBuilder postData = new StringBuilder();
      for (Map.Entry<String, Object> param : params.entrySet()) {
        if (postData.length() != 0) {
          postData.append('&');
        }
        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
        postData.append('=');
        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
      }
      byte[] postDataBytes = postData.toString().getBytes("UTF-8");

      URL url = new URL("https://accounts.google.com/o/oauth2/token");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setUseCaches(false);
      con.setRequestMethod("POST");
      con.getOutputStream().write(postDataBytes);

      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      StringBuffer buffer = new StringBuffer();
      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        buffer.append(line);
      }

      JSONObject json = new JSONObject(buffer.toString());
      String accessToken = json.getString("access_token");
      return accessToken;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
}
