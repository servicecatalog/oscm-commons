/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 18.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import org.oscm.identity.exception.IdentityResponseException;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identity.model.Token;
import org.oscm.identity.model.UserInfo;
import org.oscm.identity.validator.IdentityValidator;
import org.oscm.validation.ArgumentValidator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Common class for accessing oscm-identity endpoints
 */
public abstract class IdentityClient {

  private Client client = ClientBuilder.newClient();

  IdentityValidator validator = new IdentityValidator();
  IdentityConfiguration configuration;

  IdentityClient(IdentityConfiguration configuration) {
    this.configuration = configuration;
  }

  UserInfo getUser(String accessToken, String userId) throws IdentityResponseException {

    ArgumentValidator.notEmptyString("accessToken", accessToken);
    ArgumentValidator.notEmptyString("userId", userId);
    validator.validateRequiredSettings(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildGetUserUrl();

    Response response =
        client
            .target(url)
            .path(userId)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .get();

    IdentityClientHelper.handlePossibleErrorResponse(response);

    UserInfo userInfo = response.readEntity(UserInfo.class);
    return userInfo;
  }

  Token refreshToken(String refreshToken) throws IdentityResponseException {

    ArgumentValidator.notEmptyString("refreshToken", refreshToken);
    validator.validateRequiredSettings(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildRefreshTokenUrl();

    Token token = new Token();
    token.setRefreshToken(refreshToken);

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(token, MediaType.APPLICATION_JSON));

    IdentityClientHelper.handlePossibleErrorResponse(response);

    Token refreshedToken = response.readEntity(Token.class);
    return refreshedToken;
  }

  Token getAccessToken() throws IdentityResponseException {

    validator.validateRequiredSettings(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildGetAccessTokenUrl();

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity("", MediaType.APPLICATION_JSON));

    IdentityClientHelper.handlePossibleErrorResponse(response);

    Token token = response.readEntity(Token.class);
    return token;
  }

  GroupInfo createGroup(String accessToken, String groupName, String groupDescription)
      throws IdentityResponseException {

    ArgumentValidator.notEmptyString("accessToken", accessToken);
    ArgumentValidator.notEmptyString("groupName", groupName);
    validator.validateRequiredSettings(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildCreateGroupUrl();

    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setName(groupName);
    groupInfo.setDescription(groupDescription);

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .post(Entity.entity(groupInfo, MediaType.APPLICATION_JSON));

    IdentityClientHelper.handlePossibleErrorResponse(response);

    GroupInfo group = response.readEntity(GroupInfo.class);
    return group;
  }
}
