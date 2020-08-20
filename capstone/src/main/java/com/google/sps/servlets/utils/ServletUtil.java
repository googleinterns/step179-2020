package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

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
