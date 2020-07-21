package com.google.sps.servlets;

import com.google.common.collect.ImmutableList;

/* Contains all information relevant to a club. */
public class Club {
  private String name;
  private ImmutableList<String> members;
  private ImmutableList<String> officers;
  private String description;
  private String website;
  private ImmutableList<String> announcements;

  public Club(
      String name,
      ImmutableList<String> members,
      ImmutableList<String> officers,
      String description,
      String website,
      ImmutableList<String> announcements) {
    this.name = name;
    this.members = members;
    this.officers = officers;
    this.description = description;
    this.website = website;
    this.announcements = announcements;
  }
}
