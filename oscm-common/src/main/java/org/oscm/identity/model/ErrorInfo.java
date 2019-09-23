/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 20.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.model;

import lombok.Data;

/** Simple data transfer object representing error information */
@Data
public class ErrorInfo {

  private String error;
  private String errorDescription;
}
