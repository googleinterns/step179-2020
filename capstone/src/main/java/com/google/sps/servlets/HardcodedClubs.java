package com.google.sps.servlets;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;

/* Creates hardcoded club information for prototype. */
public class HardcodedClubs {
  public static Map<String, Club> clubs = new HashMap<String, Club>();

  public HardcodedClubs() {
    Club club1 =
        new Club(
            "Club 1",
            ImmutableList.of("Megan", "Megha", "Kevin", "Chris", "Linh"),
            ImmutableList.of("Megan", "Megha", "Kevin"),
            "This is our club. It is very cool. We do lots of things. We help people who are in need of help and those who have fallen down and can’t get up.",
            "www.club1.com",
            ImmutableList.of("Please pay your dues", "Another announcement very important"));

    Club club2 =
        new Club(
            "Club 2",
            ImmutableList.of("Megan", "Megha", "Kevin"),
            ImmutableList.of("Megha"),
            "We do cool club things",
            "www.club2atSchool.com",
            ImmutableList.of("Meeting tomorrow :)"));

    Club club3 =
        new Club(
            "Club 3",
            ImmutableList.of("Kevin"),
            ImmutableList.of("Kevin"),
            "Lonely club :( please join",
            "www.please-join.com",
            ImmutableList.of("Hi my name is Kevin"));
    clubs.put("club 1", club1);
    clubs.put("club 2", club2);
    clubs.put("club 3", club3);
  }
}
