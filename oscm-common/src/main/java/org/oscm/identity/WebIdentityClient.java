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

import java.util.Optional;

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

      Optional<String> refreshedToken = refreshAccessTokenIfNecessary(excp);
      if (refreshedToken.isPresent()) {
        userInfo = getUser(refreshedToken.get(), userId);
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

      Optional<String> refreshedToken = refreshAccessTokenIfNecessary(excp);
      if (refreshedToken.isPresent()) {
        groupInfo = createGroup(refreshedToken.get(), groupName, groupDescription);
      } else {
        throw excp;
      }
    }
    return groupInfo;
  }

  private Optional<String> refreshAccessTokenIfNecessary(IdentityClientException error)
      throws IdentityClientException {

    if (error.getMessage().equals("Access token has expired.")) {

      String refreshToken = IdentityClientHelper.getRefreshToken(configuration);
      Token response = refreshToken(refreshToken);
      IdentityClientHelper.updateTokens(configuration, response);
      return Optional.of(response.getAccessToken());
    }
    return Optional.empty();
  }
}
