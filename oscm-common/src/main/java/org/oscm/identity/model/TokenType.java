/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 20.09.2019
 *
 *******************************************************************************/
package org.oscm.identity.model;

/** Simple enum representing the type of oauth token */
public enum TokenType {
  ID_TOKEN,
  IDP_ACCESS_TOKEN,
  APPLICATION_ACCESS_TOKEN,
  REFRESH_TOKEN
}
