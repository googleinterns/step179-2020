package com.google.sps.servlets;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;

public final class ServletUtil {
  static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
  private static GoogleClientSecrets clientSecrets = null;

  public static ImmutableList<String> getPropertyList(Entity entity, String property) {
    if (entity.getProperty(property) != null) {
      return ImmutableList.copyOf((ArrayList<String>) entity.getProperty(property));
    }
    return ImmutableList.of();
  }

  public static String getNameByEmail(String email) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(email);
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    return entity.getProperty(Constants.PROPERTY_NAME).toString();
  }

  //   // Loads client secrets from a stored file
  //   public static GoogleClientSecrets getClientCredential() throws IOException {
  //     if (clientSecrets == null) {
  //       clientSecrets =
  //           GoogleClientSecrets.load(
  //               JSON_FACTORY,
  //               new
  // InputStreamReader(ServletUtil.class.getResourceAsStream("/client_secrets.json")));
  //     }
  //     return clientSecrets;
  //   }

  //   public static GoogleAuthorizationCodeFlow newFlow() throws IOException {
  //     return new GoogleAuthorizationCodeFlow.Builder(
  //             new NetHttpTransport(),
  //             JSON_FACTORY,
  //             getClientCredential(),
  //             Collections.singleton(CalendarScopes.CALENDAR))
  //         .setDataStoreFactory(AppEngineDataStoreFactory.getDefaultInstance())
  //         .setAccessType("offline")
  //         .build();
  //   }

  //   public static Calendar loadCalendarClient() throws IOException {
  //     String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
  //     Credential credential = newFlow().loadCredential(userId);
  //     return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
  //   }
}
