/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 20.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.GroupInfo;
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
   * Retrieves user information based on given user id. If response is not successful (status is
   * different than 2xx) it throws checked exception {@link IdentityClientException}
   *
   * @param userId id of user
   * @return user information
   * @throws IdentityClientException
   */
  public UserInfo getUser(String userId) throws IdentityClientException {

    validator.validateWebContext(configuration);
    String accessToken = IdentityClientHelper.getAccessToken(configuration);
    UserInfo userInfo;

    try {
      userInfo = getUser(accessToken, userId);
    } catch (IdentityClientException excp) {

      if (excp.getMessage().equals("Access token has expired.")) {
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

  /**
   * Creates user group in related OIDC provider. If response is not successful (status is different
   * than 2xx) it throws checked exception {@link IdentityClientException}
   *
   * @param groupName name of the group
   * @param groupDescription description of the group
   * @return group information
   * @throws IdentityClientException
   */
  public GroupInfo createGroup(String groupName, String groupDescription)
      throws IdentityClientException {

    validator.validateWebContext(configuration);
    String accessToken = IdentityClientHelper.getAccessToken(configuration);
    GroupInfo groupInfo;

    try {
      groupInfo = createGroup(accessToken, groupName, groupDescription);
    } catch (IdentityClientException excp) {

      if (excp.getMessage().equals("Access token has expired.")) {
        String refreshToken = IdentityClientHelper.getRefreshToken(configuration);
        Token response = refreshToken(refreshToken);
        IdentityClientHelper.updateTokens(configuration, response);
        groupInfo = createGroup(response.getAccessToken(), groupName, groupDescription);
      } else {
        throw excp;
      }
    }
    return groupInfo;
  }
}
