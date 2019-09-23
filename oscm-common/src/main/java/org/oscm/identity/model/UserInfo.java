/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 20.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.model;

import lombok.Data;

/** Simple data transfer object representing user information */
@Data
public class UserInfo {

    String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String country;
    private String city;
    private String address;
    private String postalCode;
}
