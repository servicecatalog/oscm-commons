package org.oscm.validator;

import org.oscm.identity.IdentityConfiguration;
import org.oscm.string.Strings;
import org.oscm.types.exceptions.IdentityConfigurationException;

public class IdentityValidator {

  public void validateRequiredSettings(IdentityConfiguration configuration) {

    if (Strings.isEmpty(configuration.getTenantId())) {
      throw new IdentityConfigurationException("Missing tenant id information");
    }
  }

  public void validateRequiredArgument(String argumentName, String argument) {

    if (Strings.isEmpty(argument)) {
      throw new IllegalArgumentException(argumentName + "must not be null or empty");
    }
  }
}
