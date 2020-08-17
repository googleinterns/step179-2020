package com.google.sps.servlets;

import com.google.common.collect.ImmutableList;

/* Contains all information relevant to a club. */
public class Club {
  private String name;
  private ImmutableList<String> members;
  private ImmutableList<String> officers;
  private String description;
  private String website;
  private String logo;
  private ImmutableList<String> labels;
  private long time;

  public Club(
      String name,
      ImmutableList<String> members,
      ImmutableList<String> officers,
      String description,
      String website,
      String logo,
      ImmutableList<String> labels,
      long creationTime) {
    this.name = name;
    this.members = members;
    this.officers = officers;
    this.description = description;
    this.website = website;
    this.logo = logo;
    this.labels = labels;
    this.time = creationTime;
  }

  public boolean hasOfficer(String officer) {
    return officers.contains(officer);
  }

  public String getName() {
    return name;
  }

  public int getSize() {
    return members.size();
  }

  public long getCreationTime() {
    return time;
  }

  public ImmutableList<String> getLabels() {
    return labels;
  }
}
