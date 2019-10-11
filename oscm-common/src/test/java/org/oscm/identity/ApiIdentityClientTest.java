/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 11.10.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.ErrorInfo;
import org.oscm.identity.model.IdToken;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApiIdentityClientTest {

  @Mock private Client client;
  @Mock private WebTarget webTarget;
  @Mock private Invocation.Builder builder;
  @Mock private Response response;

  @InjectMocks
  ApiIdentityClient identityClient =
      new ApiIdentityClient(IdentityConfiguration.of().tenantId("default").build());

  @Before
  public void init() {
    when(client.target(anyString())).thenReturn(webTarget);
    when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
  }

  @Test
  public void testGetIdToken_givenHttpResponseIsOK_thenIdTokenIsReturned() throws Exception {

    // given
    IdToken token = new IdToken();
    token.setIdToken("someToken");

    when(response.getStatus()).thenReturn(200);
    when(response.readEntity(IdToken.class)).thenReturn(token);
    when(builder.post(any())).thenReturn(response);

    // when
    String idToken = identityClient.getIdToken("test", "test");

    // then
    assertEquals(token.getIdToken(), idToken);
  }

  @Test(expected = IdentityClientException.class)
  public void testGetIdToken_givenHttpResponseIsNotFound_thenExceptionIsThrown() throws Exception {

    // given
    ErrorInfo errorInfo = new ErrorInfo();
    errorInfo.setError("Not found");
    errorInfo.setErrorDescription("Resource not found");

    when(response.getStatus()).thenReturn(404);
    when(response.readEntity(ErrorInfo.class)).thenReturn(errorInfo);
    when(builder.post(any())).thenReturn(response);

    // when
    String idToken = identityClient.getIdToken("test", "test");
  }
}
