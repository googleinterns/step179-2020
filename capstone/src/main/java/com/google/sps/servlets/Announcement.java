package com.google.sps.servlets;


public class Announcement {
  private String author;
  private String club;
  private long time;
  private String content;

  public Announcement(String author, String club, long time, String content) {
    this.author = author;
    this.club = club;
    this.time = time;
    this.content = content;
  }
}
