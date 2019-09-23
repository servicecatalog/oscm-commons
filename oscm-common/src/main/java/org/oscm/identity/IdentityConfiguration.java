/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 18.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import lombok.Builder;
import lombok.Data;

import javax.servlet.http.HttpSession;

/** Stores settings related to oscm-identity configuration */
@Builder(builderMethodName = "of")
@Data
public class IdentityConfiguration {

  String tenantId;
  HttpSession sessionContext;
}
