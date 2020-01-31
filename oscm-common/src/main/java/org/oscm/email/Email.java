/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2020
 *******************************************************************************/

package org.oscm.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Email {
    private String subject;
    private Date date;
    private String text;
}
