/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 20.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import org.oscm.identity.exception.IdentityResponseException;
import org.oscm.identity.model.Token;
import org.oscm.identity.model.UserInfo;

/**
 * Client for accessing oscm-identity using authentication flow related tokens (stored in session
 * context). In case access token used is expired, it tries to refresh it with refresh endpoint. All
 * necessary settings are stored in {@link IdentityConfiguration} object.
 */
public class WebIdentityClient extends IdentityClient {

  public WebIdentityClient(IdentityConfiguration configuration) {
    super(configuration);
  }

  /**
   * Retrieves user information based on given user id. If response is not successful (status
   * different than 2xx) it throws checked exception {@link IdentityResponseException}
   *
   * @param userId id of user
   * @return user information
   * @throws IdentityResponseException
   */
  public UserInfo getUser(String userId) throws IdentityResponseException {

    validator.validateWebContext(configuration);
    String accessToken = IdentityClientHelper.getAccessToken(configuration);
    UserInfo userInfo = null;

    try {
      userInfo = getUser(accessToken, userId);
    } catch (IdentityResponseException excp) {

      if (excp.getMessage().equals("Access token has expired.")) {
        System.out.println("Refreshing the token");
        String refreshToken = IdentityClientHelper.getRefreshToken(configuration);
        Token response = refreshToken(refreshToken);
        IdentityClientHelper.updateTokens(configuration, response);
        userInfo = getUser(response.getAccessToken(), userId);
      } else {
        throw excp;
      }
    }

    return userInfo;
  }
}
