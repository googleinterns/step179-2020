package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;

public final class ServletUtil {
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
}
