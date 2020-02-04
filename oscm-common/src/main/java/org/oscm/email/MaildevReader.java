/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2020
 *******************************************************************************/

package org.oscm.email;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

public class MaildevReader {
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

}