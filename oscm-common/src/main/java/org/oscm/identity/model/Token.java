/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 20.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.model;

import lombok.Data;

/** Simple data transfer object representing oauth2 related tokens */
@Data
public class Token {

  private String accessToken;
  private String refreshToken;
}
