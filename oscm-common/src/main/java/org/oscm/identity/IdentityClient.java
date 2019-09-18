package org.oscm.identity;

import org.oscm.validator.IdentityValidator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class IdentityClient {

  private IdentityConfiguration configuration;
  private Client client = ClientBuilder.newClient();
  private IdentityValidator validator = new IdentityValidator();

  public IdentityClient(IdentityConfiguration configuration) {
    this.configuration = configuration;
  }

  public Response getUser(String accessToken, String userId) {

    validator.validateRequiredSettings(configuration);
    validator.validateRequiredArgument("Access token", accessToken);
    validator.validateRequiredArgument("User id", userId);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildGetUserUrl();
    System.out.println(url);

    Response response =
        client
            .target(url)
            .path(userId)
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .get();

    return response;
  }

  public Response getAccessToken() {

    validator.validateRequiredSettings(configuration);

    IdentityUrlBuilder builder = new IdentityUrlBuilder(configuration.getTenantId());
    String url = builder.buildGetAccessTokenUrl();
    System.out.println(url);

    Response response =
        client.target(url).request(MediaType.APPLICATION_JSON).post(Entity.json(null));

    return response;
  }
}
