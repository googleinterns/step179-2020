package com.google.sps.servlets;

import java.util.List;

/* Contains all information relevant to a club. */
public class Club {
  private String name;
  private List<String> members;
  private List<String> officers;
  private String description;
  private String website;
  private List<String> announcements;

  public Club(
      String name,
      List<String> members,
      List<String> officers,
      String description,
      String website,
      List<String> announcements) {
    this.name = name;
    this.members = members;
    this.officers = officers;
    this.description = description;
    this.website = website;
    this.announcements = announcements;
  }
}
