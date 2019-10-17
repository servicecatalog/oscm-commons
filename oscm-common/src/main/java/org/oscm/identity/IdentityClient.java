/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 18.09.2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.identity;

import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.*;
import org.oscm.identity.validator.IdentityValidator;
import org.oscm.validation.ArgumentValidator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/** Abstract client for accessing oscm-identity endpoints */
public abstract class IdentityClient {

  Client client;
  IdentityValidator validator;
  IdentityConfiguration configuration;

  IdentityClient(IdentityConfiguration configuration) {
    this.client = ClientBuilder.newClient();
    this.validator = new IdentityValidator();
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
   * @param accessType type of access for requested access token
   * @return access token
   * @throws IdentityClientException
   */
  public abstract String getAccessToken(AccessType accessType) throws IdentityClientException;

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
    String accessToken = getAccessToken(AccessType.IDP);

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
    String accessToken = getAccessToken(AccessType.IDP);
    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());

    GroupInfo groupInfo = GroupInfo.of().name(groupName).description(groupDescription).build();

    try {
      return getExistingGroup(builder, client, groupInfo, accessToken);
    } catch (IdentityClientException ignored) {
    }
    return createAndReturnNewGroup(builder, client, groupInfo, accessToken);
  }

  /**
   * Searches for calls the API endpoint that will search for existing group with the name equal to
   * the name of the one passed in GroupInfo object and returns it if possible
   *
   * @param builder endpoint url builder
   * @param client client instance
   * @param groupInfo Group Info wrapper that contains the data about group that is about to be
   *     created
   * @param accessToken IDP access token
   * @return representation of existing group which creation was requested
   * @throws IdentityClientException
   */
  private GroupInfo getExistingGroup(
      IdentityUrlBuilder builder, Client client, GroupInfo groupInfo, String accessToken)
      throws IdentityClientException {
    String url = builder.buildCreateGroupUrl();
    Response response =
        client
            .target(url + groupInfo.getName())
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .get();
    return IdentityClientHelper.handleResponse(response, GroupInfo.class, url);
  }

  /**
   * Calls the endpoint that will create new group in remote directory and returns representaion of
   * this newly created object
   *
   * @param builder endpoint url builder
   * @param client client instance
   * @param groupInfo Group Info wrapper that contains the data about group that is about to be
   *     created
   * @param accessToken IDP access token
   * @return representation of created group
   * @throws IdentityClientException
   */
  private GroupInfo createAndReturnNewGroup(
      IdentityUrlBuilder builder, Client client, GroupInfo groupInfo, String accessToken)
      throws IdentityClientException {
    String url = builder.buildCreateGroupUrl();
    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .post(Entity.entity(groupInfo, MediaType.APPLICATION_JSON));

    return IdentityClientHelper.handleResponse(response, GroupInfo.class, url);
  }

  /**
   * Retrieves members of given group in related OIDC provider. If response is not successful
   * (status is different than 2xx) it throws checked exception {@link IdentityClientException}
   *
   * @param groupId id of the group
   * @return users
   * @throws IdentityClientException
   */
  public Set<UserInfo> getGroupMembers(String groupId) throws IdentityClientException {

    validate(configuration);
    ArgumentValidator.notEmptyString("groupId", groupId);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildGroupMembersUrl(groupId);

    String accessToken = getAccessToken(AccessType.IDP);

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .get();

    Set<UserInfo> members = IdentityClientHelper.handleResponse(response, Set.class, url);
    return members;
  }

  /**
   * Adds given user to group with given id in related OIDC provider. If response is not successful
   * (status is different than 2xx) it throws checked exception {@link IdentityClientException}
   *
   * @param userId id of user
   * @param groupId if of group
   * @throws IdentityClientException
   */
  public void addGroupMember(String userId, String groupId) throws IdentityClientException {

    validate(configuration);
    ArgumentValidator.notEmptyString("userId", userId);
    ArgumentValidator.notEmptyString("groupId", groupId);

    String accessToken = getAccessToken(AccessType.IDP);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildGroupMembersUrl(groupId);

    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(userId);

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .post(Entity.entity(userInfo, MediaType.APPLICATION_JSON));

    IdentityClientHelper.handleResponse(response, String.class, url);
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
