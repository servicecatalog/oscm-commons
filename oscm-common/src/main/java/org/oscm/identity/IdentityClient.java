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

import javax.validation.ValidationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Abstract client for accessing oscm-identity endpoints */
public abstract class IdentityClient {

  Client client;
  IdentityValidator validator;
  IdentityConfiguration configuration;
  private static final String OSCM_PREFIX = "OSCM_";

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
   * Updated the user on the Identity provider's side. It requires 'userId' to be present in the
   * UserInfo object.
   *
   * @param user object containing data about updated user
   * @return standard HTTP 'Response'
   * @throws IdentityClientException
   */
  public Response updateUser(UserInfo user) throws IdentityClientException {
    validate(configuration);
    validateUserObject(user);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.getUpdateUserUrl();
    String accessToken = getAccessToken(AccessType.IDP);

    Response response =
        client
            .target(url)
            .path(user.getUserId())
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .put(Entity.entity(user, MediaType.APPLICATION_JSON));

    return IdentityClientHelper.handleResponse(response, url);
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

    GroupInfo newOrExistingGroup = null;

    try {
      newOrExistingGroup = getExistingGroup(builder, client, groupName, accessToken);
    } catch (IdentityClientException e) {
      if (e.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
        newOrExistingGroup =
            createAndReturnNewGroup(builder, client, groupName, groupDescription, accessToken);
      } else throw e;
    }
    return newOrExistingGroup;
  }

  /**
   * Search the existing group with the name equal to the given group name and return detailed
   * information of this group.
   *
   * @param builder - an endpoint URL builder
   * @param client - a client instance
   * @param groupName - - the name of the group that is about to be created
   * @param accessToken - required IDP access token
   * @return representation of existing group which creation was requested
   * @throws IdentityClientException - if a group with the given name could not be found or any
   *     other problem occurred on retrieving the requested information.
   */
  private GroupInfo getExistingGroup(
      IdentityUrlBuilder builder, Client client, String groupName, String accessToken)
      throws IdentityClientException {
    String url = builder.buildCreateGroupUrl();
    String path = builder.buildGroupPath(groupName);
    Response response =
        client
            .target(url)
            .path(path)
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
   * @param groupName Group Info wrapper that contains the data about group that is about to be
   *     created
   * @param accessToken IDP access token
   * @return representation of created group
   * @throws IdentityClientException
   */
  private GroupInfo createAndReturnNewGroup(
      IdentityUrlBuilder builder,
      Client client,
      String groupName,
      String groupDescription,
      String accessToken)
      throws IdentityClientException {
    GroupInfo groupInfo = new GroupInfo();
    groupInfo.setDescription(groupDescription);
    groupInfo.setName(OSCM_PREFIX + groupName);
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

    UserInfo[] members = IdentityClientHelper.handleResponse(response, UserInfo[].class, url);
    return new HashSet<>(Arrays.asList(members));
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
   * @return id of the user
   * @throws IdentityClientException
   */
  public String validateToken(String token, TokenType tokenType) throws IdentityClientException {

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

    UserId user = IdentityClientHelper.handleResponse(response, UserId.class, url);
    return user.getUserId();
  }

  /**
   * Retrieves members of given group in related OIDC provider. If response is not successful
   * (status is different than 2xx) it throws checked exception {@link IdentityClientException}
   *
   * @return groups
   * @throws IdentityClientException
   */
  public Set<GroupInfo> getGroups() throws IdentityClientException {

    validate(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());

    String url = builder.buildGroupsUrl();
    String accessToken = getAccessToken(AccessType.IDP);

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .get();

    GroupInfo[] groups = IdentityClientHelper.handleResponse(response, GroupInfo[].class, url);
    return new HashSet<>(Arrays.asList(groups));
  }

  /**
   * Retrieves id token (based on resource owner password credentials grant) from related OIDC. If
   * response is not successful (status is different than 2xx) it throws checked exception {@link
   * IdentityClientException} provider
   *
   * @param username username for authenticating to identity provider
   * @param password password for authenticating to identity provider
   * @return id token
   */
  public String getIdToken(String username, String password) throws IdentityClientException {

    validator.validateRequiredSettings(configuration);
    ArgumentValidator.notEmptyString("username", username);
    ArgumentValidator.notEmptyString("password", password);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildIdTokenTokenUrl();

    Credentials credentials = new Credentials(username, password);

    Response response =
        client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

    IdToken token = IdentityClientHelper.handleResponse(response, IdToken.class, url);
    return token.getIdToken();
  }

  /**
   * Validated if provided user object has all mandatory fields filled up
   *
   * @param user object to be validated
   */
  private void validateUserObject(UserInfo user) {
    if (user.getUserId() == null || user.getUserId().isEmpty())
      throw new ValidationException("UserId should not be null nor empty!");
  }
}
