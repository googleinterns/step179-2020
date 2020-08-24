package com.google.sps.servlets;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public final class ServletUtil {
  static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
  private static GoogleClientSecrets clientSecrets = null;
  private static final String OFFLINE_ACCESS_TYPE = "offline";

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

  public static String getRedirectUri(HttpServletRequest req) {
    // TODO: change redirect URI when web app is deployed.
    // If you want to run this locally, you will need to replace this with your dev server URI
    // - then add "/oauth2callback" to the end of it and add that to you API console under
    // Authorized URIs.
    return "https://clubhub-step-2020.googleplex.com/oauth2callback";
    // return
    // "https://8080-4417b0ad-e7ff-4d2f-acc8-c9ddcf7b56d9.us-west1.cloudshell.dev/oauth2callback";
  }

  public static GoogleAuthorizationCodeFlow newFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            Constants.JSON_FACTORY,
            getClientCredential(),
            Collections.singleton(CalendarScopes.CALENDAR))
        .setDataStoreFactory(AppEngineDataStoreFactory.getDefaultInstance())
        .setAccessType(OFFLINE_ACCESS_TYPE)
        .build();
  }

  static GoogleClientSecrets getClientCredential() throws IOException {
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(
            Constants.JSON_FACTORY,
            new InputStreamReader(ServletUtil.class.getResourceAsStream("/client_secrets.json")));
    return clientSecrets;
  }

  public static Entity addItemToEntity(Entity entity, String itemToAdd, String property) {
    // Create empty List if property does not exist yet
    List<String> generalList = new ArrayList<String>(ServletUtil.getPropertyList(entity, property));
    if (!generalList.contains(itemToAdd)) {
      generalList.add(itemToAdd);
    }
    entity.setProperty(property, generalList);
    return entity;
  }

  public static Entity removeItemFromEntity(Entity entity, String itemToRemove, String property) {
    // Create empty List if property does not exist yet
    List<String> generalList = new ArrayList<String>(ServletUtil.getPropertyList(entity, property));
    if (generalList.contains(itemToRemove)) {
      generalList.remove(itemToRemove);
    }
    entity.setProperty(property, generalList);
    return entity;
  }
}
