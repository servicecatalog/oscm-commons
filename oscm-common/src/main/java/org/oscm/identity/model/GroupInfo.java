/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 24.09.2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.identity.model;

import lombok.Builder;
import lombok.Data;

/** Simple data transfer object representing group information */
@Data
@Builder(builderMethodName = "of")
public class GroupInfo {

  private String id;
  private String name;
  private String description;
}
