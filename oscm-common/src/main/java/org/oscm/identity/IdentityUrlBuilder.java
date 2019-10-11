/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 18.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

/** Class responsible for building oscm-identity related endpoints */
public class IdentityUrlBuilder {

  public IdentityUrlBuilder(String tenantId) {
    this.tenantId = tenantId;
  }

  private static String HOSTNAME = "http://oscm-identity:9090/oscm-identity";
  private static String RESOURCE_TENANTS = "tenants";
  private static String RESOURCE_USERS = "users";
  private static String RESOURCE_GROUPS = "groups";
  private static String RESOURCE_TOKEN = "token";

  private String tenantId;

  /**
   * Builds url for retrieving user information from oscm-identity
   *
   * @return url
   */
  public String buildGetUserUrl() {

    String url =
        new StringBuilder(HOSTNAME)
            .append("/")
            .append(RESOURCE_TENANTS)
            .append("/")
            .append(tenantId)
            .append("/")
            .append(RESOURCE_USERS)
            .toString();

    return url;
  }

  /**
   * Builds url for retrieving access token (based on client credentials flow) from oscm-identity
   *
   * @return url
   */
  public String buildGetAccessTokenUrl() {

    String url =
        new StringBuilder(HOSTNAME)
            .append("/")
            .append(RESOURCE_TENANTS)
            .append("/")
            .append(tenantId)
            .append("/")
            .append(RESOURCE_TOKEN)
            .toString();

    return url;
  }

  /**
   * Builds url for refreshing access token through oscm-identity
   *
   * @return url
   */
  public String buildRefreshTokenUrl() {

    String url =
        new StringBuilder(HOSTNAME)
            .append("/")
            .append(RESOURCE_TENANTS)
            .append("/")
            .append(tenantId)
            .append("/")
            .append(RESOURCE_TOKEN)
            .append("/")
            .append("refresh")
            .toString();

    return url;
  }

  /**
   * Builds url for validating tokens through oscm-identity
   *
   * @return url
   */
  public String buildValidateTokenUrl() {

    String url = new StringBuilder(buildTokenUrl()).append("/").append("verify").toString();

    return url;
  }

  /**
   * Builds url for creating new group through oscm-identity
   *
   * @return url
   */
  public String buildCreateGroupUrl() {

    String url =
        new StringBuilder(HOSTNAME)
            .append("/")
            .append(RESOURCE_TENANTS)
            .append("/")
            .append(tenantId)
            .append("/")
            .append(RESOURCE_GROUPS)
            .toString();

    return url;
  }

  /**
   * Builds url for retrieving group members through oscm-identity
   *
   * @return url
   */
  public String buildGroupMembersUrl(String groupId) {

    String url =
        new StringBuilder(HOSTNAME)
            .append("/")
            .append(RESOURCE_TENANTS)
            .append("/")
            .append(tenantId)
            .append("/")
            .append(RESOURCE_GROUPS)
            .append("/")
            .append(groupId)
            .append("/")
            .append("members")
            .toString();

    return url;
  }

  /**
   * Builds base url for oscm-identity token operations
   *
   * @return url
   */
  private String buildTokenUrl() {

    String url =
        new StringBuilder(HOSTNAME)
            .append("/")
            .append(RESOURCE_TENANTS)
            .append("/")
            .append(tenantId)
            .append("/")
            .append(RESOURCE_TOKEN)
            .toString();
    return url;
  }
  
  /**
   * Builds url for retrieving groups through oscm-identity
   *
   * @return url
   */
  public String buildGroupsUrl() {
  
      String url =
        new StringBuilder(HOSTNAME)
            .append("/")
            .append(RESOURCE_TENANTS)
            .append("/")
            .append(tenantId)
            .append("/")
            .append(RESOURCE_GROUPS)
            .toString();
    return url;
  }

  /**
   * Builds url for retrieving id token through oscm-identity
   *
   * @return url
   */
  public String buildIdTokenTokenUrl() {

    String url = new StringBuilder(buildTokenUrl()).append("/").append("identify").toString();

    return url;
  }
}
