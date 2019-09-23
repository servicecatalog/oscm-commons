/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 18.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.exception;

/** Runtime exception thrown when oscm-identity client delivered configuration is invalid */
public class IdentityConfigurationException extends RuntimeException {

  public IdentityConfigurationException(String message) {
    super(message);
  }
}
