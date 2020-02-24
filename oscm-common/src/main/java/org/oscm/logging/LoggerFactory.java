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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

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

  private static ConsoleAppender consoleAppender;
  private static boolean switchedToFileAppender = false;

  private static Logger LOGGER = LogManager.getLogger(LoggerFactory.class);

  public static Log4jLogger getLogger(Class<?> category) {
    return getLogger(category, Locale.getDefault());
  }

  public static Log4jLogger getLogger(Class<?> category, Locale locale) {

    LOGGER.info("Initializing Logger:" + category.getName());
    LOGGER.info("File appender:" + switchedToFileAppender);

    synchronized (managedLoggers) {
      Log4jLogger logger = new Log4jLogger(category, locale);
      if (switchedToFileAppender) {
        setFileAppendersForLogger(logger);
      } else {
        setConsoleAppenderForLogger(logger);
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

    LOGGER.info("Activating file appenders........");

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

    LOGGER.info("Initializing file appenders........");
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    systemLogAppender = config.getAppender(systemLogAppenderName);
    accessLogAppender = config.getAppender(accessLogAppenderName);
    auditLogAppender = config.getAppender(auditLogAppenderName);
    reverseProxyLogAppender = config.getAppender(reverseProxyLogAppenderName);
  }

  private static void setFileAppendersForLogger(Log4jLogger logger) {
    LOGGER.info("Setting file appenders");
    Level level = determineLogLevel(logLevel);

    String systemLoggerName = logger.systemLogger.getName();
    String accessLoggerName = logger.accessLogger.getName();
    String auditLoggerName = logger.auditLogger.getName();
    String proxyLoggerName = logger.proxyLogger.getName();

    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    LoggerConfig systemLoggerConfig = new LoggerConfig(systemLoggerName, level, false);
    systemLoggerConfig.addAppender(systemLogAppender, level, null);
    config.addLogger(systemLoggerName, systemLoggerConfig);
    LOGGER.info("Setting file  for " + systemLoggerName);

    LoggerConfig accessLoggerConfig = new LoggerConfig(accessLoggerName, level, false);
    accessLoggerConfig.addAppender(accessLogAppender, level, null);
    config.addLogger(accessLoggerName, accessLoggerConfig);
    LOGGER.info("Setting file appender for " + accessLoggerName);

    LoggerConfig auditLoggerConfig = new LoggerConfig(auditLoggerName, level, false);
    auditLoggerConfig.addAppender(auditLogAppender, level, null);
    config.addLogger(auditLoggerName, auditLoggerConfig);
    LOGGER.info("Setting file appender for " + auditLoggerName);

    LoggerConfig proxyLoggerConfig = new LoggerConfig(proxyLoggerName, level, false);
    proxyLoggerConfig.addAppender(reverseProxyLogAppender, level, null);
    config.addLogger(proxyLoggerName, proxyLoggerConfig);
    LOGGER.info("Setting file appender for " + proxyLoggerName);

    ctx.updateLoggers();
  }

  private static void setConsoleAppenderForLogger(Log4jLogger logger) {
    // TODO: set console appender for loggers
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
}
