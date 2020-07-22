package com.google.sps.servlets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/** Hard-coded student data for prototype */
public final class PrototypeStudents {
  public static final String MEGAN_EMAIL = "meghashi@google.com";
  public static final String MEGHA_EMAIL = "kakm@google.com";
  public static final String KEVIN_EMAIL = "kshao@google.com";
  public static final String MEGAN_NAME = "Megan Shi";
  public static final String MEGHA_NAME = "Megha Kak";
  public static final String KEVIN_NAME = "Kevin Shao";
  public static final int YEAR_2022 = 2022;
  public static final int YEAR_2023 = 2023;
  public static final String MAJOR = "Computer Science";
  public static final String CLUB_1 = "Club 1";
  public static final String CLUB_2 = "Club 2";
  public static final String CLUB_3 = "Club 3";

  public static final ImmutableMap<String, Student> PROTOTYPE_STUDENTS =
      ImmutableMap.of(
          MEGAN_EMAIL,
              new Student(
                  MEGAN_NAME, YEAR_2022, MAJOR, MEGAN_EMAIL, ImmutableList.of(CLUB_1, CLUB_2)),
          MEGHA_EMAIL,
              new Student(
                  MEGHA_NAME, YEAR_2022, MAJOR, MEGHA_EMAIL, ImmutableList.of(CLUB_2, CLUB_3)),
          KEVIN_EMAIL,
              new Student(
                  KEVIN_NAME,
                  YEAR_2023,
                  MAJOR,
                  KEVIN_EMAIL,
                  ImmutableList.of(CLUB_1, CLUB_2, CLUB_3)));
}
