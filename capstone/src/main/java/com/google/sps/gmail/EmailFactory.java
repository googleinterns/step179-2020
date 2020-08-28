package com.google.sps.gmail;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.io.ByteSource;
import com.google.sps.servlets.AnnouncementsServlet;
import com.google.sps.servlets.Constants;
import com.google.sps.servlets.ServletUtil;
import com.google.sps.servlets.StudentServlet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.codec.binary.Base64;

public class EmailFactory {
  // The special value "me" can be used to indicate the authenticated user
  private static final String AUTH_USER = "me";
  private static final String SENDER_EMAIL =
      "kakm+clubhub@google.com"; // TODO: create dummy email to send email notifications from
  private static Gmail service;

  public EmailFactory(Gmail service) {
    this.service = service;
  }

  private static Message createMessageWithEmail(MimeMessage emailContent)
      throws MessagingException, IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    emailContent.writeTo(buffer);
    byte[] bytes = buffer.toByteArray();
    String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

    Message message = new Message();
    message.setRaw(encodedEmail);
    return message;
  }

  private static MimeMessage createEmail(String recipientEmail, String subject, String body)
      throws MessagingException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage email = new MimeMessage(session);

    // Set necessary information for email
    email.setFrom(new InternetAddress(SENDER_EMAIL));
    email.addRecipient(RecipientType.TO, new InternetAddress(recipientEmail));
    email.setSubject(subject);
    email.setContent(body, "text/html;charset=utf-8");
    return email;
  }

  public static void sendEmail(String recipientEmail, String body, String subject) {
    try {
      // Set up Gmail service if necessary and send email
      if (service == null) {
        service = AnnouncementsServlet.getGmailService();
      }
      MimeMessage email = EmailFactory.createEmail(recipientEmail, subject, body);
      Message message = EmailFactory.createMessageWithEmail(email);
      service.users().messages().send(AUTH_USER, message).execute();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String getHTMLAsString(String path) throws IOException {
    // Load HTML file and convert to String
    InputStream inputStream = EmailFactory.class.getResourceAsStream(Constants.EMAIL_PATH + path);
    ByteSource byteSource =
        new ByteSource() {
          @Override
          public InputStream openStream() throws IOException {
            return inputStream;
          }
        };

    String emailBody = byteSource.asCharSource(Charsets.UTF_8).read();
    return emailBody;
  }

  public static void sendWelcomeEmail(String recipientEmail) throws IOException {
    // Prepare welcome email content and send
    String subject = String.format("Welcome to ClubHub!");
    String emailBody = getHTMLAsString("/welcome-email.html");
    sendEmail(recipientEmail, emailBody, subject);
  }

  public static void sendEmailToAllMembers(String clubName, Entity announcement)
      throws IOException {
    // Find all members of the club that posted the announcement
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query(Constants.CLUB_ENTITY_PROP)
            .setFilter(
                new FilterPredicate(Constants.PROPERTY_NAME, FilterOperator.EQUAL, clubName));
    PreparedQuery results = datastore.prepare(query);
    Entity club = results.asSingleEntity();
    ImmutableList<String> members = ServletUtil.getPropertyList(club, Constants.MEMBER_PROP);

    // Prepare email content
    String announcementString = StudentServlet.getAnnouncementAsString(announcement);
    String emailBody =
        getHTMLAsString("/announcement-email.html")
            .replace("[CLUB_NAME]", clubName)
            .replace("[ANNOUNCEMENT]", announcementString);
    String subject = String.format("ClubHub: New announcement from %s!", clubName);

    // Send email to all members of the club
    Streams.stream(members).forEach(memberEmail -> sendEmail(memberEmail, emailBody, subject));
  }
}
