/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 23.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.exception;

/**
 * Checked exception thrown in case http response requested by oscm-identity client is not
 * successful. contains additional information with http response status and error description
 */
public class IdentityResponseException extends Exception {

  private int status;
  private String error;

  public IdentityResponseException(String message) {
    super(message);
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
