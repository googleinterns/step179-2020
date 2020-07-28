package com.google.sps.servlets;

import com.google.appengine.api.datastore.Entity;

public class Announcement {
  private String author;
  private String club;
  private long time;
  private String content;

  public Announcement(Entity entity) {
    this.author = entity.getProperty("author").toString();
    this.club = entity.getProperty("club").toString();
    this.content = entity.getProperty("content").toString();
    this.time = Long.parseLong(entity.getProperty("time").toString());
  }
}
