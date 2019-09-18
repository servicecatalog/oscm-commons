/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 18.09.2019
 *
 *******************************************************************************/
package org.oscm.validator;

import org.oscm.identity.IdentityConfiguration;
import org.oscm.string.Strings;
import org.oscm.types.exceptions.IdentityConfigurationException;

/** Validator used along with oscm-identity client */
public class IdentityValidator {

  /**
   * Validates all required setting of oscm-identity related configuration
   *
   * @param configuration
   */
  public void validateRequiredSettings(IdentityConfiguration configuration) {

    if (Strings.isEmpty(configuration.getTenantId())) {
      throw new IdentityConfigurationException("Missing tenant id information");
    }
  }
}
