/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 16-10-2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.identity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.ErrorInfo;
import org.oscm.identity.model.GroupInfo;
import org.oscm.identity.model.UserInfo;
import org.oscm.identity.validator.IdentityValidator;
import org.oscm.internal.types.exception.IllegalArgumentException;

import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class IdentityClientTest {

  @Mock private Client client;
  @Mock private IdentityValidator validator;
  @Mock private HttpSession session;
  @Mock private IdentityConfiguration configuration;
  @Mock private Invocation.Builder builder;
  @Mock private WebTarget webTarget;
  @Mock private Response response;
  @InjectMocks private WebIdentityClient identityClient;

  @Before
  public void setUp() {
    identityClient = new WebIdentityClient(configuration);
    MockitoAnnotations.initMocks(this);

    when(configuration.getSessionContext()).thenReturn(session);
    mockHttpRequestCreation();
  }

  @Test
  public void shouldCreateGroup_whenThereIsNoGroupExisting() throws IdentityClientException {
    GroupInfo expectedGroup = new GroupInfo();
    expectedGroup.setName("groupName");
    expectedGroup.setDescription("description");
    ErrorInfo errorEntity = new ErrorInfo();
    errorEntity.setError("Group not found");

    when(response.getStatus()).thenReturn(404, 404, 404, 201);
    when(response.readEntity(any(Class.class))).thenReturn(errorEntity, expectedGroup);

    GroupInfo createdGroup =
        identityClient.createGroup(expectedGroup.getName(), expectedGroup.getDescription());

    assertThat(createdGroup).isNotNull();
    assertThat(createdGroup)
        .isNotNull()
        .extracting(GroupInfo::getName)
        .isEqualTo(expectedGroup.getName());
    assertThat(createdGroup)
        .isNotNull()
        .extracting(GroupInfo::getDescription)
        .isEqualTo(expectedGroup.getDescription());
  }

  @Test
  public void shouldReturnExistingGroup_whenGroupOfNameExists() throws IdentityClientException {
    GroupInfo expectedGroup = new GroupInfo();
    expectedGroup.setName("groupName");
    expectedGroup.setDescription("description");

    when(response.getStatus()).thenReturn(200);
    when(response.readEntity(any(Class.class))).thenReturn(expectedGroup);

    GroupInfo createdGroup =
        identityClient.createGroup(expectedGroup.getName(), expectedGroup.getDescription());

    assertThat(createdGroup).isNotNull();
    assertThat(createdGroup)
        .isNotNull()
        .extracting(GroupInfo::getName)
        .isEqualTo(expectedGroup.getName());
    assertThat(createdGroup)
        .isNotNull()
        .extracting(GroupInfo::getDescription)
        .isEqualTo(expectedGroup.getDescription());
  }

  @Test
  public void shouldUpdateTheUser() {
    when(response.getStatus()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());

    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("some@user.com");

    assertThatCode(() -> identityClient.updateUser(userInfo)).doesNotThrowAnyException();
  }

  @Test
  public void shouldNotUpdateTheUser_whenRequestSendingFailed() {
    ErrorInfo errorEntity = new ErrorInfo();
    errorEntity.setError("Something went wrong during sending the request");

    when(response.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    when(response.readEntity(any(Class.class))).thenReturn(errorEntity);

    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("some@user.com");

    assertThatExceptionOfType(IdentityClientException.class)
        .isThrownBy(() -> identityClient.updateUser(userInfo));
  }

  @Test
  public void shouldNotUpdateTheUser_whenUserIdIsNotPresent() {
    when(response.getStatus()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());

    UserInfo userInfo = new UserInfo();
    userInfo.setUserId("some@user.com");

    assertThatCode(() -> identityClient.updateUser(userInfo)).doesNotThrowAnyException();
  }

  @Test
  public void deleteGroupSuccessTest() throws IdentityClientException {
    final String groupId = "groupId";
    when(response.getStatus()).thenReturn(Response.Status.NO_CONTENT.getStatusCode());

    identityClient.deleteGroup(groupId);

    assertThatCode(() -> identityClient.deleteGroup(groupId)).doesNotThrowAnyException();
  }

  @Test(expected = IllegalArgumentException.class)
  public void deleteGroupEmptyGroupIdTest() throws IdentityClientException {
    final String groupId = "";
    identityClient.deleteGroup(groupId);
  }

  private void mockHttpRequestCreation() {
    when(client.target(anyString())).thenReturn(webTarget);
    when(webTarget.path(anyString())).thenReturn(webTarget);
    when(webTarget.request(anyString())).thenReturn(builder);
    when(webTarget.request()).thenReturn(builder);
    when(builder.header(any(), any())).thenReturn(builder);
    when(builder.get()).thenReturn(response);
    when(builder.post(any())).thenReturn(response);
    when(builder.put(any())).thenReturn(response);
    when(builder.delete()).thenReturn(response);
  }
}
