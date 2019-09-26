/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 18.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identity.model.TokenDetails;
import org.oscm.identity.model.TokenType;
import org.oscm.identity.model.UserInfo;
import org.oscm.identity.validator.IdentityValidator;
import org.oscm.validation.ArgumentValidator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Abstract client for accessing oscm-identity endpoints */
public abstract class IdentityClient {

  Client client = ClientBuilder.newClient();
  IdentityValidator validator = new IdentityValidator();
  IdentityConfiguration configuration;

  IdentityClient(IdentityConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Validates configuration settings necessary for specific client. In case of failure it throws
   * runtime exception {@link org.oscm.identity.exception.IdentityConfigurationException}
   *
   * @param configuration
   */
  abstract void validate(IdentityConfiguration configuration);

  /**
   * Retrieves access token used by specific client for requesting endpoints
   *
   * @return access token
   * @throws IdentityClientException
   */
  public abstract String getAccessToken() throws IdentityClientException;

  /**
   * Retrieves user information based on given user's id. If response is not successful (status is
   * different than 2xx) it throws checked exception {@link IdentityClientException}
   *
   * @param userId id of user
   * @return user information
   * @throws IdentityClientException
   */
  public UserInfo getUser(String userId) throws IdentityClientException {

    ArgumentValidator.notEmptyString("userId", userId);
    validate(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildGetUserUrl();
    String accessToken = getAccessToken();

    Response response =
        client
            .target(url)
            .path(userId)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .get();

    UserInfo userInfoResponse = IdentityClientHelper.handleResponse(response, UserInfo.class, url);
    return userInfoResponse;
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

    ArgumentValidator.notEmptyString("groupName", groupName);
    validate(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildCreateGroupUrl();
    String accessToken = getAccessToken();

    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setName(groupName);
    groupInfo.setDescription(groupDescription);

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .post(Entity.entity(groupInfo, MediaType.APPLICATION_JSON));

    GroupInfo groupInfoResponse = IdentityClientHelper.handleResponse(response, GroupInfo.class, url);
    return groupInfoResponse;
  }

  /**
   * Validates the given token. If validation fails it provides client with {@link
   * IdentityClientException}
   *
   * @param token
   * @param tokenType type of the token
   * @throws IdentityClientException
   */
  public void validateToken(String token, TokenType tokenType) throws IdentityClientException {

    validator.validateRequiredSettings(configuration);
    ArgumentValidator.notEmptyString("token", token);
    ArgumentValidator.notNull("tokenType", tokenType);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildValidateTokenUrl();
    TokenDetails tokenDetails = new TokenDetails();
    tokenDetails.setToken(token);
    tokenDetails.setTokenType(tokenType.name());

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(tokenDetails, MediaType.APPLICATION_JSON));

    IdentityClientHelper.handleResponse(response, String.class, url);
  }
}
