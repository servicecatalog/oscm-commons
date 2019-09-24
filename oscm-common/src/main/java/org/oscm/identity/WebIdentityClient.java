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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Client for accessing oscm-identity using authentication flow related tokens (stored in session
 * context). In case access token used is expired, client tries to refresh and execute the request again. All
 * necessary settings are stored in {@link IdentityConfiguration} object.
 */
public class WebIdentityClient extends IdentityClient {


  public WebIdentityClient(IdentityConfiguration configuration) {
    super(configuration);
  }

  @Override
  void validate(IdentityConfiguration configuration) {
    validator.validateRequiredSettings(configuration);
    validator.validateWebContext(configuration);
  }

  @Override
  public String getAccessToken() {
    validator.validateWebContext(configuration);
    return IdentityClientHelper.getAccessToken(configuration);
  }

  @Override
  public UserInfo getUser(String userId) throws IdentityClientException {

    try {
      return super.getUser(userId);
    } catch (IdentityClientException exception) {

      boolean tokenRefreshed = refreshAccessToken(exception);
      if (tokenRefreshed) {
        return super.getUser(userId);
      } else {
        throw exception;
      }
    }
  }

  @Override
  public GroupInfo createGroup(String groupName, String groupDescription) throws IdentityClientException {

    try {
      return super.createGroup(groupName, groupDescription);
    } catch (IdentityClientException exception) {

      boolean tokenRefreshed = refreshAccessToken(exception);
      if (tokenRefreshed) {
        return super.createGroup(groupName, groupDescription);
      } else {
        throw exception;
      }
    }
  }

  private boolean refreshAccessToken(IdentityClientException exception) throws IdentityClientException {

    if (exception.getMessage().equals("Access token has expired.")) {

      String refreshToken = IdentityClientHelper.getRefreshToken(configuration);
      IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
      String url = builder.buildRefreshTokenUrl();

      Token token = new Token();
      token.setRefreshToken(refreshToken);

      Response response =
          client
              .target(url)
              .request(MediaType.APPLICATION_JSON)
              .post(Entity.entity(token, MediaType.APPLICATION_JSON));

      Token refreshedTokens = IdentityClientHelper.handleResponse(response, Token.class, url);
      IdentityClientHelper.updateTokens(configuration, refreshedTokens);
      return true;
    }
    return false;
  }
}
