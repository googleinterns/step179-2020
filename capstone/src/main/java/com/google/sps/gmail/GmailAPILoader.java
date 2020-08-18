package com.google.sps.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.common.collect.Streams;
import com.google.sps.servlets.Constants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class GmailAPILoader {
  public static Gmail getGmailService() throws IOException, GeneralSecurityException {
    // Add credentials for using API
    Credential authorize =
        new GoogleCredential.Builder()
            .setTransport(GoogleNetHttpTransport.newTrustedTransport())
            .setJsonFactory(Constants.JSON_FACTORY)
            .setClientSecrets(Secrets.CLIENT_ID, Secrets.CLIENT_SECRET)
            .build()
            .setRefreshToken(Secrets.REFRESH_TOKEN)
            .setAccessToken(getAccessToken());

    // Create Gmail API Service
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Gmail service =
        new Gmail.Builder(HTTP_TRANSPORT, Constants.JSON_FACTORY, authorize)
            .setApplicationName(Constants.APPLICATION_NAME)
            .build();
    return service;
  }

  private static String getAccessToken() {
    try {
      // Add secret values to Map
      Map<String, Object> params = new LinkedHashMap<>();
      params.put("grant_type", "refresh_token");
      params.put("client_id", Secrets.CLIENT_ID);
      params.put("client_secret", Secrets.CLIENT_SECRET);
      params.put("refresh_token", Secrets.REFRESH_TOKEN);

      // Add secret values as String for POST request
      String postData =
          Streams.stream(params.entrySet())
              .map(GmailAPILoader::getParamAsString)
              .collect(Collectors.joining());

      // Remove first '&' symbol and get value as byte array
      postData = postData.substring(1, postData.length());
      byte[] postDataBytes = postData.toString().getBytes("UTF-8");

      // Call POST request to receive access token
      URL url = new URL("https://accounts.google.com/o/oauth2/token");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setUseCaches(false);
      con.setRequestMethod("POST");
      con.getOutputStream().write(postDataBytes);

      // Get access token from POST response
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      JSONObject json = new JSONObject(reader.lines().collect(Collectors.joining()));
      String accessToken = json.getString("access_token");
      return accessToken;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getParamAsString(Map.Entry<String, Object> param) {
    try {
      String paramAsString =
          "&"
              + URLEncoder.encode(param.getKey(), "UTF-8")
              + "="
              + URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8");
      return paramAsString;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
