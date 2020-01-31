package org.oscm.email;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

public class MaildevReader {
    private String maildevAddress;
    private ObjectMapper objectMapper;

    public MaildevReader(String maildevAddress) {
        this.maildevAddress = maildevAddress;
        this.objectMapper = new ObjectMapper();
    }

    public Email getLatestEmailBySubject(String subject) throws IOException {
        final Email[] emails = objectMapper.readValue(new URL(maildevAddress + "/email"), Email[].class);
        return Arrays.stream(emails)
                .filter(email -> email.getSubject().equals(subject))
                .max(Comparator.comparing(Email::getDate))
                .orElse(null);
    }

}