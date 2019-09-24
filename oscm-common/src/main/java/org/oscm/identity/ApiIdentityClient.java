/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 23.09.2019
 *
 *******************************************************************************/
package org.oscm.identity;

import org.oscm.identity.exception.IdentityClientException;
import org.oscm.identity.model.Token;

/**
 * Client for accessing oscm-identity using client credentials flow. it requires from the client to
 * get access token first, validate it and then request wanted endpoint. All necessary settings are
 * stored in {@link IdentityConfiguration} object and it requires only id of the tenant.
 */
public class ApiIdentityClient extends IdentityClient {

    public ApiIdentityClient(IdentityConfiguration configuration) {
        super(configuration);
    }

    public Token getAccessToken() throws IdentityClientException {
        Token accessToken = super.getAccessToken();
        return accessToken;
    }
}
