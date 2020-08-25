package com.google.sps.gmail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.sps.servlets.Constants;
import com.google.sps.servlets.ServletUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class EmailFactoryTest {
  private static final String CLUB_1 = "Club 1";
  private static final String MEGHA_EMAIL = "kakm@google.com";
  //   private static final String KEVIN_EMAIL = "kshao@google.com";
  //   private static final String MEGAN_EMAIL = "meganshi@google.com";
  private static final String ANNOUNCEMENT = "Here is an announcement";

  private Entity club1;
  private Entity announcement1;
  private EmailFactory emailFactory;
  private LocalServiceTestHelper localHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Mock private Gmail gmail;
  @Mock private Gmail.Users users;
  @Mock private Gmail.Users.Messages messages;
  @Mock private Gmail.Users.Messages.Send send;

  @Before
  public void setUp() throws IOException {
    localHelper.setUp();
    MockitoAnnotations.initMocks(this);

    // Set up gmail mock variables and EmailFactory instance based on mock gmail service
    gmail = mock(Gmail.class);
    emailFactory = new EmailFactory(gmail);
    users = mock(Gmail.Users.class);
    messages = mock(Gmail.Users.Messages.class);
    send = mock(Gmail.Users.Messages.Send.class);

    // Add dependencies between mock variables
    when(gmail.users()).thenReturn(users);
    when(users.messages()).thenReturn(messages);
    when(messages.send(any(), any())).thenReturn(send);

    club1 = new Entity("Club");
    club1.setProperty(Constants.PROPERTY_NAME, CLUB_1);

    announcement1 = new Entity(Constants.ANNOUNCEMENT_PROP);
    announcement1.setProperty(Constants.AUTHOR_PROP, MEGHA_EMAIL);
    announcement1.setProperty(Constants.TIME_PROP, System.currentTimeMillis());
    announcement1.setProperty(Constants.CONTENT_PROP, ANNOUNCEMENT);
    announcement1.setProperty(Constants.CLUB_PROP, CLUB_1);
    announcement1.setProperty(Constants.EDITED_PROP, false);
  }

  @After
  public void tearDown() {
    localHelper.tearDown();
  }

  @Test
  public void sendEmailToAllMembers_clubWithOnlyOneMember() throws IOException, MessagingException {
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGHA_EMAIL));
    datastore.put(club1);
    datastore.put(announcement1);

    ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
    emailFactory.sendEmailToAllMembers(CLUB_1, announcement1);
    verify(messages).send(any(), argument.capture());
    String body = convertToMimeMessage(argument.getValue()).getContent().toString();

    Assert.assertEquals(getEmailContent(CLUB_1, announcement1), body);
  }

  @Test
  public void sendEmailToAllMembers_clubWithMultipleMembers()
      throws IOException, MessagingException {
    club1.setProperty(Constants.MEMBER_PROP, ImmutableList.of(MEGHA_EMAIL, "test@example.com"));
    datastore.put(club1);
    datastore.put(announcement1);

    ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
    emailFactory.sendEmailToAllMembers(CLUB_1, announcement1);
    // Need to add times(3) because there are 3 recipients of the same email
    verify(messages, times(2)).send(any(), argument.capture());
    String body = convertToMimeMessage(argument.getValue()).getContent().toString();

    Assert.assertEquals(getEmailContent(CLUB_1, announcement1), body);
  }

  @Test
  public void sendWelcomeEmail_studentLogsInForFirstTime() throws IOException, MessagingException {
    ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
    emailFactory.sendWelcomeEmail(MEGHA_EMAIL);
    verify(messages).send(any(), argument.capture());
    String body = convertToMimeMessage(argument.getValue()).getContent().toString();

    Assert.assertEquals(getEmailBody("/welcome-email.html"), body);
  }

  private static MimeMessage convertToMimeMessage(Message message) throws MessagingException {
    Properties properties = System.getProperties();
    Session session = Session.getDefaultInstance(properties);
    byte[] bytes = message.decodeRaw();
    InputStream targetStream = new ByteArrayInputStream(bytes);
    return new MimeMessage(session, targetStream);
  }

  private String getEmailContent(String clubName, Entity announcement) throws IOException {
    // Format announcement date and time
    TimeZone timePST = TimeZone.getTimeZone("PST");
    DateFormat formatDate = new SimpleDateFormat("HH:mm MM-dd-yyyy");
    formatDate.setTimeZone(timePST);
    String time =
        formatDate.format(
            new Date(Long.parseLong(announcement.getProperty(Constants.TIME_PROP).toString())));

    String fullAnnouncement =
        String.format(
            "%1$s from %2$s in %3$s sent at %4$s",
            announcement.getProperty(Constants.CONTENT_PROP),
            ServletUtil.getNameByEmail(announcement.getProperty(Constants.AUTHOR_PROP).toString()),
            announcement.getProperty(Constants.CLUB_PROP),
            time);
    String emailBody =
        getEmailBody("/announcement-email.html")
            .replace("[CLUB_NAME]", clubName)
            .replace("[ANNOUNCEMENT]", fullAnnouncement);
    return emailBody;
  }

  private String getEmailBody(String path) throws IOException {
    // String fullPath = new File(".").getCanonicalPath() + Constants.EMAIL_PATH + path;
    // File htmlTemplate = new File(fullPath);
    // String emailBody = FileUtils.readFileToString(htmlTemplate, "utf-8");
    // return emailBody;

    InputStream is = EmailFactory.class.getResourceAsStream(Constants.EMAIL_PATH + path);
    ByteSource byteSource =
        new ByteSource() {
          @Override
          public InputStream openStream() throws IOException {
            return is;
          }
        };

    String text = byteSource.asCharSource(Charsets.UTF_8).read();
    return text;
  }
}
