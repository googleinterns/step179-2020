package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableList;

/** Each instance of Student contains all necessary student information */
public class Student {
  private String name;
  private int gradYear;
  private String major;
  private String email;
  private ImmutableList<String> clubs;

  public Student(
      String name, int gradYear, String major, String email, ImmutableList<String> clubs) {
    this.name = name;
    this.gradYear = gradYear;
    this.major = major;
    this.email = email;
    this.clubs = clubs;
  }

  public String getName() {
    return this.name;
  }

  public int getGradYear() {
    return this.gradYear;
  }

  public String getMajor() {
    return this.major;
  }

  public String getEmail() {
    return this.email;
  }

  public ImmutableList<String> getClubList() {
    return this.clubs;
  }

  static String getNameByEmail(String email) {
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
