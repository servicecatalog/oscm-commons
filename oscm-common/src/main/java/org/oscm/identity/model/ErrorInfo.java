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

import lombok.Builder;
import lombok.Data;

/** Simple data transfer object representing error information */
@Data
public class ErrorInfo {

  private String error;
  private String errorDescription;
}
