/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 20.09.2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.identity.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** Simple data transfer object representing user information */
@Data
public class UserInfo {

  @NotNull private String userId;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String country;
  private String city;
  private String address;
  private String postalCode;
  private String locale;
  private String gender;
}
