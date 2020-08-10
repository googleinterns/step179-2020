package com.google.sps.servlets;

public class Announcement {
  private String author;
  private String club;
  private long time;
  private String content;
  private boolean isAuthor;
  private String authorName;

  public Announcement(
      String author, String club, long time, String content, boolean isAuthor, String authorName) {
    this.author = author;
    this.club = club;
    this.time = time;
    this.content = content;
    this.isAuthor = isAuthor;
    this.authorName = authorName;
  }
}
