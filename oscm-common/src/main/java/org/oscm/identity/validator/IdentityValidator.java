/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 18.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.validator;

import org.oscm.identity.IdentityConfiguration;
import org.oscm.identity.model.TokenType;
import org.oscm.string.Strings;
import org.oscm.identity.exception.IdentityConfigurationException;

import javax.servlet.http.HttpSession;

/** Validator used along with oscm-identity client */
public class IdentityValidator {

  /**
   * Validates all required setting of oscm-identity related configuration
   *
   * @param configuration
   */
  public void validateRequiredSettings(IdentityConfiguration configuration) {

    if (Strings.isEmpty(configuration.getTenantId())) {
      throw new IdentityConfigurationException("Missing required setting: tenantId");
    }
  }

  /**
   * Validates web session context and its elements necessary for requesting oscm-identity secured
   * endpoints and for refreshing the access token
   *
   * @param configuration
   */
  public void validateWebContext(IdentityConfiguration configuration) {

    HttpSession sessionContext = configuration.getSessionContext();

    if (sessionContext == null) {
      throw new IdentityConfigurationException("Missing required setting: sessionContext");
    }

    String accessToken = (String) sessionContext.getAttribute(TokenType.ACCESS_TOKEN.name());

    if (Strings.isEmpty(accessToken)) {
      throw new IdentityConfigurationException(
          "Missing required setting: sessionContext[ACCESS_TOKEN]");
    }

    String refreshToken = (String) sessionContext.getAttribute(TokenType.REFRESH_TOKEN.name());

    if (Strings.isEmpty(refreshToken)) {
      throw new IdentityConfigurationException(
          "Missing required setting: sessionContext[REFRESH_TOKEN]");
    }
  }
}
