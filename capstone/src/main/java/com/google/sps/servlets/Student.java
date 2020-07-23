package com.google.sps.servlets;

import java.util.ArrayList;

/** Each instance of Student contains all necessary student information */
public class Student {
  private String name;
  private int gradYear;
  private String major;
  private String email;
  private ArrayList<String> clubs;

  public Student(String name, int gradYear, String major, String email, ArrayList<String> clubs) {
    this.name = name;
    this.gradYear = gradYear;
    this.major = major;
    this.email = email;
    this.clubs = clubs;
  }

  public String getName() {
    return name;
  }

  public int getGradYear() {
    return gradYear;
  }

  public String getMajor() {
    return major;
  }

  public String getEmail() {
    return email;
  }

  public ArrayList<String> getClubList() {
    return clubs;
  }

  public void removeClub(String club) {
    clubs.remove(club);
  }
}
