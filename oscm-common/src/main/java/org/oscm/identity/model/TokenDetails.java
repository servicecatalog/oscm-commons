/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 25.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.model;

import lombok.Data;

@Data
public class TokenDetails {

  private String token;
  private String tokenType;
}
