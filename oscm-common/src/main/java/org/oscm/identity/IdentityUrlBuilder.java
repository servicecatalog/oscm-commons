package org.oscm.identity;

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
}
