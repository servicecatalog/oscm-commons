/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.email;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

public class MaildevReader {

  private static final String MAIL_SUBJECT_USER_ACCOUNT_CREATED_EN = "Account created";
  private static final String MAIL_BODY_PASSWORD_PATTERN_EN = "password is:";
  private static final String MAIL_BODY_KEY_PATTERN_EN = "Web service access:";
  private static final String MAIL_BODY_USERNAME_PATTERN_EN = "Your user ID is: ";

  private String maildevEmailPath;
  private ObjectMapper objectMapper;

  public MaildevReader(String maildevAddress) {
    this.maildevEmailPath = maildevAddress + "/email";
    this.objectMapper = new ObjectMapper();
  }

  public Email getLatestEmailBySubject(String subject) throws IOException {
    final Email[] emails = objectMapper.readValue(new URL(maildevEmailPath), Email[].class);
    return Arrays.stream(emails)
        .filter(email -> email.getSubject().equals(subject))
        .max(Comparator.comparing(Email::getTime))
        .orElse(null);
  }

  public String[] readPasswordAndKeyFromEmail(String userName) throws IOException {
    String userPwd = null;
    String userKey = null;

    Email email = getLatestEmailBySubject(MAIL_SUBJECT_USER_ACCOUNT_CREATED_EN);
    String message = email.getText();

    String userNameFromEmail = readInformationFromGivenMail(MAIL_BODY_USERNAME_PATTERN_EN, message);
    if (userName.equals(userNameFromEmail)) {
      userKey = readInformationFromGivenMail(MAIL_BODY_KEY_PATTERN_EN, message);
      userPwd = readInformationFromGivenMail(MAIL_BODY_PASSWORD_PATTERN_EN, message);
    }

    return new String[] {userKey, userPwd};
  }

  private String readInformationFromGivenMail(String pattern, String mailContent) {
    String information = mailContent;
    int idx = information.indexOf(pattern);
    if (idx >= 0 && idx + pattern.length() < information.length()) {
      idx += pattern.length();
      information = information.substring(idx).trim();
      idx = 0;
      while (idx < information.length() && !Character.isWhitespace(information.charAt(idx))) {
        idx++;
      }
      if (idx < information.length()) {
        information = information.substring(0, idx);
      }
    }
    return information;
  }
}
