package com.google.sps.servlets;

import java.util.List;

/** Each instance of Student contains all necessary student information */
public class Student {
  private String name;
  private Integer gradYear;
  private String major;
  private String email;
  private List<String> clubs;

  public Student(String name, Integer gradYear, String major, String email, List<String> clubs) {
    this.name = name;
    this.gradYear = gradYear;
    this.major = major;
    this.email = email;
    this.clubs = clubs;
  }

  public String getName() {
    return name;
  }

  public Integer getGradYear() {
    return gradYear;
  }

  public String getMajor() {
    return major;
  }

  public String getEmail() {
    return email;
  }

  public List<String> getClubList() {
    return clubs;
  }
}
