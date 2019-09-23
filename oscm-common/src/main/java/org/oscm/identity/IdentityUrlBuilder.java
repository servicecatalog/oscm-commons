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

  private static String HOSTNAME = "http://localhost:9090/oscm-identity";
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
}
