/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 24.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.model;

import lombok.Data;

/** Simple data transfer object representing group information */
@Data
public class GroupInfo {

  private String id;
  private String name;
  private String description;
}
