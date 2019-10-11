package org.oscm.identity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Simple data transfer object representing credentials */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {

  private String username;
  private String password;
}
