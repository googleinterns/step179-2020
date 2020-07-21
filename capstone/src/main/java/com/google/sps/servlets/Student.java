package com.google.sps.servlets;

import com.google.common.collect.ImmutableList;

/** Each instance of Student contains all necessary student information */
public class Student {
  private String name;
  private Integer gradYear;
  private String major;
  private String email;
  private ImmutableList<String> clubs;

  public Student(
      String name, Integer gradYear, String major, String email, ImmutableList<String> clubs) {
    this.name = name;
    this.gradYear = gradYear;
    this.major = major;
    this.email = email;
    this.clubs = clubs;
  }
}