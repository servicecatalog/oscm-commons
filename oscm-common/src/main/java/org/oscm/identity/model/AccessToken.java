/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 27.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.model;

import lombok.Data;

@Data
public class AccessToken {

  private String accessToken;
  private AccessType accessType;
}
