/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2020
 *******************************************************************************/
package org.oscm.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class MaildevReaderTest {

  private static final Random RANDOM = new Random();

  private String maildevHost;
  private String maildevEmailPath;

  @InjectMocks private MaildevReader reader;

  @Mock ObjectMapper mapper;

  @Before
  public void setup() {
    this.maildevHost = "http://localhost:8082";
    this.maildevEmailPath = this.maildevHost + "/email";
    this.reader = new MaildevReader(maildevHost);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getLatestEmailWhenInboxIsEmptyTest() throws IOException {
    when(mapper.readValue(new URL(this.maildevEmailPath), Email[].class)).thenReturn(new Email[0]);
    final Email returnedEmail = reader.getLatestEmailBySubject("Some random text");
    assertNull(returnedEmail);
  }

  @Test
  public void getLatestEmailWhenInboxHaveOneItemAndSubjectDontMatch() throws IOException {
    final Email email = generateEmail("Some text");
    when(mapper.readValue(new URL(this.maildevEmailPath), Email[].class))
        .thenReturn(new Email[] {email});
    final Email returnedEmail = reader.getLatestEmailBySubject("Text don't match");
    assertNull(returnedEmail);
  }

  @Test
  public void getLatestEmailWhenInboxHaveOneItemAndSubjectMatch() throws IOException {
    final Email email = generateEmail("Some text");
    when(mapper.readValue(new URL(this.maildevEmailPath), Email[].class))
        .thenReturn(new Email[] {email});
    final Email returnedEmail = reader.getLatestEmailBySubject("Some text");
    assertEquals(email, returnedEmail);
  }

  @Test
  public void getLatestEmailWhenInboxHaveManyRandomItemsAndSubjectDontMatch() throws IOException {
    final AtomicInteger integer = new AtomicInteger();
    List<Email> emailList =
        Stream.generate(
                () -> {
                  final String subject = String.valueOf(RANDOM.nextDouble());
                  final Date date =
                      Date.from(Instant.now().plus(integer.getAndIncrement(), ChronoUnit.MINUTES));
                  final String text = String.valueOf(RANDOM.nextDouble());
                  return generateEmail(subject, date, text);
                })
            .limit(50)
            .collect(Collectors.toList());
    Collections.shuffle(emailList);
    when(mapper.readValue(new URL(this.maildevEmailPath), Email[].class))
        .thenReturn(emailList.toArray(new Email[0]));
    final Email returnedEmail = reader.getLatestEmailBySubject("No such mail");
    assertNull(returnedEmail);
  }

  @Test
  public void getLatestEmailWhenInboxHaveManyRandomItemsAndSubjectMatch() throws IOException {
    final AtomicInteger integer = new AtomicInteger();
    final Email email = generateEmail("Matching Text");
    List<Email> emailList =
        Stream.generate(
                () -> {
                  final String subject = String.valueOf(RANDOM.nextDouble());
                  final Date date =
                      Date.from(Instant.now().plus(integer.getAndIncrement(), ChronoUnit.MINUTES));
                  final String text = String.valueOf(RANDOM.nextDouble());
                  return generateEmail(subject, date, text);
                })
            .limit(50)
            .collect(Collectors.toList());
    emailList.add(email);
    Collections.shuffle(emailList);
    when(mapper.readValue(eq(new URL(this.maildevEmailPath)), eq(Email[].class)))
        .thenReturn(emailList.toArray(new Email[0]));
    final Email returnedEmail = reader.getLatestEmailBySubject("Matching Text");
    assertEquals(email, returnedEmail);
  }

  @Test
  public void testReadPasswordAndKeyFromEmail() throws Exception {
    String userName = "tester";
    String userPwd = "test123";
    String userKey = "10000";

    final Email email = generateRegistrationEmail(userName, userKey, userPwd);
    when(mapper.readValue(new URL(this.maildevEmailPath), Email[].class))
        .thenReturn(new Email[] {email});

    String[] credentials = reader.readPasswordAndKeyFromEmail(userName);
    assertEquals(userKey, credentials[0]);
    assertEquals(userPwd, credentials[1]);
  }

    @Test
    public void testReadKeyFromEmail() throws Exception {
        String userName = "tester";
        String userPwd = "test123";
        String userKey = "10000";

        final Email email = generateRegistrationEmail(userName, userKey, userPwd);
        when(mapper.readValue(new URL(this.maildevEmailPath), Email[].class))
                .thenReturn(new Email[] {email});

        String key = reader.readKeyFromEmail(userName);
        assertEquals(userKey, key);
    }

  private static Email generateEmail(String subject, Date date, String text) {
    final Email email = new Email();
    email.setSubject(subject);
    email.setTime(date);
    email.setText(text);
    return email;
  }

  private static Email generateEmail(String subject) {
    final Email email = new Email();
    email.setSubject(subject);
    email.setTime(Date.from(Instant.now()));
    email.setText(String.valueOf(RANDOM.nextDouble()));
    return email;
  }

  private static Email generateRegistrationEmail(String userName, String userKey, String password) {
    final Email email = new Email();
    email.setSubject("Account created");
    email.setTime(Date.from(Instant.now()));
    email.setText(
        "Dear user,\n"
            + "Your user ID is: "
            + userName
            + "\n"
            + "Your initial password is: "
            + password
            + "\n"
            + "User key for Web service access: "
            + userKey
            + "\n"
            + "With best regards.");
    return email;
  }
}
