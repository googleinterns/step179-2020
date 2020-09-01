package com.google.sps.servlets;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

// TODO: Refactor and organize this class
/* Stores constants shared between multiple servlets. */
public final class Constants {
  public Constants() {}

  public static final String CALENDAR_PROP = "calendar";
  public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  public static final String GOOGLE_APPLICATION_NAME = "google.com:clubhub-step-2020";
  public static final String CLUB_PROP = "club";
  public static final String INTERESTED_CLUB_PROP = "interestedClubs";
  public static final String INTERESTED_JOIN_PROP = "interested-join";
  public static final String INTERESTED_LEAVE_PROP = "interested-leave";
  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_EMAIL = "email";
  public static final String PROPERTY_GRADYEAR = "gradYear";
  public static final String PROPERTY_MAJOR = "major";
  public static final String PROPERTY_CLUBS = "clubs";
  public static final String DESCRIP_PROP = "description";
  public static final String WEBSITE_PROP = "website";
  public static final String LABELS_PROP = "labels";
  public static final String SORT_PROP = "sort";
  public static final String DEFAULT_SORT_PROP = "default";
  public static final String ALPHA_SORT_PROP = "alpha";
  public static final String SIZE_SORT_PROP = "size";
  public static final String LOGO_PROP = "logo";
  public static final String MEMBER_PROP = "members";
  public static final String OFFICER_PROP = "officers";
  public static final int LOAD_LIMIT = 25;
  public static final String CLUB_ENTITY_PROP = "Club";
  public static final String ANNOUNCEMENT_PROP = "Announcement";
  public static final String FUTURE_ANNOUNCEMENT_PROP = "FutureAnnouncement";
  public static final String AUTHOR_PROP = "author";
  public static final String TIME_PROP = "time";
  public static final String EDITED_PROP = "edited";
  public static final String CONTENT_PROP = "content";
  public static final String JOIN_CLUB_PROP = "join";
  public static final String BLOB_KEY_PROP = "blobKey";
  public static final String LEAVE_CLUB_PROP = "leave";
  public static final String NEW_NAME_PROP = "new-name";
  public static final String NEW_YEAR_PROP = "new-year";
  public static final String NEW_MAJOR_PROP = "new-major";
  public static final String PROFILE_PIC_PROP = "upload-profile";
  public static final String SCHEDULED_DATE_PROP = "scheduled-date";
  public static final String APPLICATION_NAME = "clubhub-step-2020";
  public static final String TIME_ZONE = "America/Los_Angeles";
  public static final String EXCLUSIVE_PROP = "exclusive";
  public static final String START_TIME_PROP = "start-time";
  public static final String END_TIME_PROP = "end-time";
  public static final String EVENT_TITLE_PROP = "event-title";
  public static final String EVENT_DESCRIPTION_PROP = "event-description";
  public static final String TIMEZONE_OFFSET = ":00.000-07:00";
  public static final String TIMEZONE_PROP = "timezone";
  public static final String EMAIL_PATH = "/emailTemplates/";
  public static final String REQUEST_PROP = "requests";
}
