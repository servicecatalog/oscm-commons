/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 20.09.2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.identity;

import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.ErrorInfo;
import org.oscm.identity.model.Token;
import org.oscm.identity.model.TokenType;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import javax.ws.rs.core.Response;

/** Utility class for handling issues related to oscm-identity client */
public class IdentityClientHelper {

  private static final Log4jLogger LOGGER = LoggerFactory.getLogger(IdentityClientHelper.class);

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
   * Handles http response. In case it is successful, it provides client with response body objects,
   * it provides client with {@link IdentityClientException} otherwise
   *
   * @param <T>
   * @param response http response
   * @param type type of requested response body
   * @param url
   * @return response body
   * @throws IdentityClientException
   */
  public static <T> T handleResponse(Response response, Class<T> type, String url)
      throws IdentityClientException {

    int status = response.getStatus();
    LOGGER.logInfo(
        Log4jLogger.SYSTEM_LOG,
        LogMessageIdentifier.INFO_IDENTITY_CLIENT_RESPONSE,
        url,
        Integer.toString(status));

    if (isResponseSuccessful(response)) {
      return response.readEntity(type);
    } else {
      ErrorInfo errorInfo = response.readEntity(ErrorInfo.class);
      String error = errorInfo.getError();
      String errorDescription = errorInfo.getErrorDescription();

      IdentityClientException clientException =
          getClientException(response.getStatus(), errorDescription, error);
      throw clientException;
    }
  }

  /**
   * Handles http response with no body returned. In case it is successful, it provides client with
   * response body objects, it provides client with {@link IdentityClientException} otherwise
   *
   * @param response http response
   * @param url
   * @return response body
   * @throws IdentityClientException
   */
  public static Response handleResponse(Response response, String url)
      throws IdentityClientException {

    int status = response.getStatus();
    LOGGER.logInfo(
        Log4jLogger.SYSTEM_LOG,
        LogMessageIdentifier.INFO_IDENTITY_CLIENT_RESPONSE,
        url,
        Integer.toString(status));

    if (isResponseSuccessful(response)) {
      return response;
    } else {
      ErrorInfo errorInfo = response.readEntity(ErrorInfo.class);
      String error = errorInfo.getError();
      String errorDescription = errorInfo.getErrorDescription();

      IdentityClientException clientException =
          getClientException(response.getStatus(), errorDescription, error);
      throw clientException;
    }
  }

  private static IdentityClientException getClientException(
      int status, String errorDescription, String error) throws IdentityClientException {

    IdentityClientException cex;

    if (status == Response.Status.BAD_REQUEST.getStatusCode()) {
      cex =
          new IdentityClientException(errorDescription, IdentityClientException.Reason.BAD_REQUEST);
    } else if (status == Response.Status.NOT_FOUND.getStatusCode()) {
      cex = new IdentityClientException(errorDescription, IdentityClientException.Reason.NOT_FOUND);
    } else {
      cex =
          new IdentityClientException(errorDescription, IdentityClientException.Reason.OIDC_ERROR);
    }

    cex.setError(error);
    cex.setStatus(status);
    LOGGER.logError(LogMessageIdentifier.ERROR_IDENTITY_CLIENT_DETAILS, error, errorDescription);
    return cex;
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
