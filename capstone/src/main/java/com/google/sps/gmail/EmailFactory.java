package com.google.sps.servlets;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.sps.gmail.GmailAPILoader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
  private static final String SENDER_EMAIL = "kakm@google.com"; // TODO: create dummy email to send email notifications

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

    email.setFrom(new InternetAddress(SENDER_EMAIL));
    email.addRecipient(RecipientType.TO, new InternetAddress(recipientEmail));
    email.setSubject(subject);
    email.setText(body);
    return email;
  }

  public static void sendEmail(String recipientEmail, String body, String clubName) {
    try {
      String subject = String.format("ClubHub: New announcement from %s!", clubName);
      Gmail service = GmailAPILoader.getGmailService();
      MimeMessage email = createEmail(recipientEmail, subject, body);
      Message message = createMessageWithEmail(email);
      service.users().messages().send(AUTH_USER, message).execute();

    } catch (Exception e) {
      System.out.println("ERROR: Unable to send message : " + e.toString());
    }
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

    // Send email to all members of the club
    String body = StudentServlet.getAnnouncementAsString(announcement);
    Streams.stream(members).forEach(memberEmail -> sendEmail(memberEmail, body, clubName));
  }
}
