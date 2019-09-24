/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 20.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.ErrorInfo;
import org.oscm.identity.model.Token;
import org.oscm.identity.model.TokenType;

import javax.ws.rs.core.Response;

/** Utility class for handling issues related to oscm-identity client */
public class IdentityClientHelper {

  /**
   * Checks if http response status is successful
   *
   * @param response http response
   * @return true in case response status is 2xx, false otherwise
   */
  public static boolean isResponseSuccessful(Response response) {
    return Integer.toString(response.getStatus()).startsWith("2");
  }

  /**
   * Provide client with {@link IdentityClientException} based on http response containing error
   * information in case response is not successful
   *
   * @param response http response
   * @throws IdentityClientException
   */
  public static void handlePossibleErrorResponse(Response response) throws IdentityClientException {

    if (!isResponseSuccessful(response)) {
      ErrorInfo errorInfo = response.readEntity(ErrorInfo.class);

      IdentityClientException clientException =
          new IdentityClientException(errorInfo.getErrorDescription());

      clientException.setError(errorInfo.getError());
      clientException.setStatus(response.getStatus());

      throw clientException;
    }
  }

  /**
   * Retrieves access token value form the session context
   *
   * @param configuration oscm-identity client configuration
   * @return access token
   */
  public static String getAccessToken(IdentityConfiguration configuration) {

    String accessToken =
        (String) configuration.getSessionContext().getAttribute(TokenType.ACCESS_TOKEN.name());

    return accessToken;
  }

  /**
   * Retrieves refresh token value form the session context
   *
   * @param configuration oscm-identity client configuration
   * @return refresh token
   */
  public static String getRefreshToken(IdentityConfiguration configuration) {

    String refreshToken =
        (String) configuration.getSessionContext().getAttribute(TokenType.REFRESH_TOKEN.name());

    return refreshToken;
  }

  /**
   * Sets new values for access and refresh token in session context
   *
   * @param configuration oscm-identity client configuration
   * @param token wrapper containing access and refresh token
   */
  public static void updateTokens(IdentityConfiguration configuration, Token token) {

    configuration
        .getSessionContext()
        .setAttribute(TokenType.ACCESS_TOKEN.name(), token.getAccessToken());

    configuration
        .getSessionContext()
        .setAttribute(TokenType.REFRESH_TOKEN.name(), token.getRefreshToken());
  }
}
