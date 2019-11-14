/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 23.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.exception;

import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Checked exception thrown in case http response requested by oscm-identity client is not
 * successful. contains additional information with http response status and error description
 */
public class IdentityClientException extends SaaSApplicationException {

  private static final long serialVersionUID = 6120364217852763091L;
    
  private int status;
  private String error;
  private Reason reason;

  public IdentityClientException(String message) {
    super(message);
  }
  
  public IdentityClientException(Throwable cause) {
      super(cause);
    }
  
  public IdentityClientException(String message, ApplicationExceptionBean bean) {
      super(message, bean);
  }
  
  public IdentityClientException(String message, Reason reason) {
      super(message);
      this.reason = reason;
      setMessageKey(getMessageKey() + "." + reason.toString());
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
  
  public Reason getReason() {
      return this.reason;
  }
  

  public enum Reason {

      /**
       * A http statuscode 400 is the result of the IdentityClient request 
       */
      BAD_REQUEST,

      /**
       * A http statuscode 404 is the result of the IdentityClient request 
       */
      NOT_FOUND,
      
      /**
       * A communication problem with the OIDC server occurs 
       */
      OIDC_ERROR

  }
}
