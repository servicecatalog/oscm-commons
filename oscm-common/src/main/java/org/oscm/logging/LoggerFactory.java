/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Author: Mike J&auml;ger
 *
 *  Creation Date: 23.04.2009
 *
 *  Completion Time: 17.06.2009
 *
 *******************************************************************************/
package org.oscm.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Provides functionality to obtain logger objects and to modify their settings, e.g. the log level.
 *
 * @author Mike J&auml;ger
 */
public class LoggerFactory {

  public static final String INFO_LOG_LEVEL = "INFO";
  public static final String DEBUG_LOG_LEVEL = "DEBUG";
  public static final String WARN_LOG_LEVEL = "WARN";
  public static final String ERROR_LOG_LEVEL = "ERROR";

  private static HashMap<Class<?>, Log4jLogger> managedLoggers =
      new HashMap<Class<?>, Log4jLogger>();

  private static final int MAX_BACKUP_INDEX = 5;
  private static final String MAX_FILE_SIZE = "10MB";

  private static String logLevel;
  private static String logFilePath;
  private static String logConfigPath;

  private static FileAppender systemLogAppender;
  private static FileAppender accessLogAppender;
  private static FileAppender auditLogAppender;
  private static FileAppender reverseProxyLogAppender;

  private static final String systemLogAppenderName = "SystemLogAppender";
  private static final String accessLogAppenderName = "AccessLogAppender";
  private static final String auditLogAppenderName = "AuditLogAppender";
  private static final String reverseProxyLogAppenderName = "ReverseProxyLogAppender";

  private static boolean switchedToFileAppender = false;

  public static Log4jLogger getLogger(Class<?> category) {
    return getLogger(category, Locale.getDefault());
  }

  public static Log4jLogger getLogger(Class<?> category, Locale locale) {

    synchronized (managedLoggers) {
      Log4jLogger logger = new Log4jLogger(category, locale);
      if (switchedToFileAppender) {
        setFileAppendersForLogger(logger);
      }
      if (!managedLoggers.containsKey(category)) {
        managedLoggers.put(category, logger);
      }
      return logger;
    }
  }

  /**
   * Changes the initial ConsoleAppender to a RollingFileAppender using the current configuration
   * settings.
   *
   * @param logFilePath The path to the log files.
   * @param logConfigFile The path to the log4j configuration file.
   * @param logLevel The log level to be used.
   */
  public static void activateRollingFileAppender(
      String logFilePath, String logConfigFile, String logLevel) {

    synchronized (managedLoggers) {
      LoggerFactory.logLevel = logLevel;
      LoggerFactory.logFilePath = logFilePath;
      LoggerFactory.logConfigPath = logConfigFile;

      initAppenders();

      Iterator<Class<?>> iterator = managedLoggers.keySet().iterator();
      while (iterator.hasNext()) {
        Class<?> loggerName = iterator.next();
        Log4jLogger logger = managedLoggers.get(loggerName);
        setFileAppendersForLogger(logger);
      }
      switchedToFileAppender = true;
    }
  }

  private static void initAppenders() {

    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    systemLogAppender = initFileAppender(systemLogAppenderName, "system.log");
    accessLogAppender = initFileAppender(accessLogAppenderName, "access.log");
    auditLogAppender = initFileAppender(auditLogAppenderName, "audit.log");
    reverseProxyLogAppender = initFileAppender(reverseProxyLogAppenderName, "reverseproxy.log");

    config.addAppender(systemLogAppender);
    config.addAppender(accessLogAppender);
    config.addAppender(auditLogAppender);
    config.addAppender(reverseProxyLogAppender);
  }

  private static FileAppender initFileAppender(String appenderName, String filename) {
    FileAppender appender =
        FileAppender.newBuilder()
            .withName(appenderName)
            .withFileName(logFilePath + File.separatorChar + filename)
            .withLayout(getLayout())
            .build();
    appender.start();
    return appender;
  }

  private static void setFileAppendersForLogger(Log4jLogger logger) {
    Level level = determineLogLevel(logLevel);

    String systemLoggerName = logger.systemLogger.getName();
    String accessLoggerName = logger.accessLogger.getName();
    String auditLoggerName = logger.auditLogger.getName();
    String proxyLoggerName = logger.proxyLogger.getName();

    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    LoggerConfig systemLoggerConfig = initLogger(systemLoggerName, systemLogAppender, level);
    config.addLogger(systemLoggerName, systemLoggerConfig);

    LoggerConfig accessLoggerConfig = initLogger(accessLoggerName, accessLogAppender, level);
    config.addLogger(accessLoggerName, accessLoggerConfig);

    LoggerConfig auditLoggerConfig = initLogger(auditLoggerName, auditLogAppender, level);
    config.addLogger(auditLoggerName, auditLoggerConfig);

    LoggerConfig proxyLoggerConfig = initLogger(proxyLoggerName, reverseProxyLogAppender, level);
    config.addLogger(proxyLoggerName, proxyLoggerConfig);

    ctx.updateLoggers();
  }

  private static LoggerConfig initLogger(String loggerName, Appender appender, Level level) {
    LoggerConfig loggerConfig = new LoggerConfig(loggerName, level, false);
    loggerConfig.addAppender(appender, level, null);
    return loggerConfig;
  }

  /**
   * Determines the log level represented by the configuration setting. If the stored value does not
   * match any of the supported log levels, the default log level INFO will be used.
   *
   * @param logLevel The log level information read from the configuration settings.
   * @return The log4j compliant log level to be used.
   */
  private static Level determineLogLevel(String logLevel) {
    Level level = Level.INFO;
    if (DEBUG_LOG_LEVEL.equals(logLevel)) {
      level = Level.DEBUG;
    } else if (WARN_LOG_LEVEL.equals(logLevel)) {
      level = Level.WARN;
    } else if (ERROR_LOG_LEVEL.equals(logLevel)) {
      level = Level.ERROR;
    } else if (INFO_LOG_LEVEL.equals(logLevel)) {
      level = Level.INFO;
    }
    return level;
  }

  /**
   * Returns the log message layout according to the product requirements.
   *
   * @return The message layout.
   */
  private static Layout getLayout() {
    return PatternLayout.newBuilder()
        .withPattern("%d{MM/dd/yyyy_HH:mm:ss.SSS} FSP_INTS-BSS: %p: ThreadID %t: %c{1}: %m%n")
        .build();
  }
}
