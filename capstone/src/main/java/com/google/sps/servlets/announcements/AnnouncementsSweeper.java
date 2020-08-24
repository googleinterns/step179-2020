package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.collect.Streams;
import java.time.Clock;
import java.time.ZoneId;

public class AnnouncementsSweeper {

  private static ZoneId timeZone = ZoneId.of(Constants.TIME_ZONE);
  private static Clock clock = Clock.system(timeZone);

  protected static void setClock(Clock clock) {
    AnnouncementsSweeper.clock = clock;
  }

  public static void sweepAnnouncements() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query(Constants.FUTURE_ANNOUNCEMENT_PROP)
            .setFilter(
                new FilterPredicate(Constants.TIME_PROP, FilterOperator.LESS_THAN, clock.millis()));
    PreparedQuery prepared = datastore.prepare(query);

    Streams.stream(prepared.asIterable()).forEach(entity -> postAnnouncement(datastore, entity));
  }

  private static void postAnnouncement(DatastoreService datastore, Entity entity) {
    Entity announcementEntity = new Entity(Constants.ANNOUNCEMENT_PROP);
    announcementEntity.setProperty(
        Constants.AUTHOR_PROP, entity.getProperty(Constants.AUTHOR_PROP));
    announcementEntity.setProperty(Constants.TIME_PROP, entity.getProperty(Constants.TIME_PROP));
    announcementEntity.setProperty(
        Constants.CONTENT_PROP, entity.getProperty(Constants.CONTENT_PROP));
    announcementEntity.setProperty(Constants.CLUB_PROP, entity.getProperty(Constants.CLUB_PROP));
    announcementEntity.setProperty(Constants.EDITED_PROP, false);

    datastore.delete(entity.getKey());
    datastore.put(announcementEntity);
  }
}
