package com.google.sps.servlets;

import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;

public final class ServletUtil {
  public static ImmutableList<String> getPropertyList(Entity entity, String property) {
    if (entity.getProperty(property) != null) {
      return ImmutableList.copyOf((ArrayList<String>) entity.getProperty(property));
    }
    return ImmutableList.of();
  }
}
