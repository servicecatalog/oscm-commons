/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2020
 *
 *  Creation Date: 27.02.2020
 *
 *******************************************************************************/

package org.oscm.logging;

public enum AppenderConfiguration {
  SYSTEM_LOG_APPENDER("SystemLogAppender", "system.log", "system-%i.log"),
  ACCESS_LOG_APPENDER("AccessLogAppender", "access.log", "access-%i.log"),
  AUDIT_LOG_APPENDER("AuditLogAppender", "audit.log", "audit-%i.log"),
  PROXY_LOG_APPENDER("ProxyLogAppender", "reverseproxy.log", "reverseproxy-%i.log");

  AppenderConfiguration(String name, String fileName, String filePattern) {
    this.name = name;
    this.fileName = fileName;
    this.filePattern = filePattern;
  }

  private String name;
  private String fileName;
  private String filePattern;

  private static final String MAX_BACKUP_INDEX = "5";
  private static final String MAX_FILE_SIZE = "10MB";
  private static final String PATTERN_LAYOUT =
      "%d{MM/dd/yyyy_HH:mm:ss.SSS} FSP_INTS-BSS: %p: ThreadID %t: %c{1}: %m%n";

  public String getFileName() {
    return fileName;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getName() {
    return name;
  }

  public static String getMaxBackupIndex() {
    return MAX_BACKUP_INDEX;
  }

  public static String getMaxFileSize() {
    return MAX_FILE_SIZE;
  }

  public static String getPatternLayout() {
    return PATTERN_LAYOUT;
  }
}
