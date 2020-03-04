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
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.util.HashMap;
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

  private static final HashMap<Class<?>, Log4jLogger> managedLoggers = new HashMap<>();

  private static Logger FACTORY_LOGGER = LogManager.getLogger(LoggerFactory.class);

  private static String logLevel;
  private static String logFilePath;
  private static String logConfigPath;

  private static Appender systemLogAppender;
  private static Appender accessLogAppender;
  private static Appender auditLogAppender;
  private static Appender proxyLogAppender;

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

      FACTORY_LOGGER.info("LOG: New loggers for class: " + category + "created");
      System.out.println("STDOUT: New loggers for class: " + category + "created");
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
      FACTORY_LOGGER.info("LOG: Initiating appenders,  file" + logFilePath);

      initAppenders();

      for (Class<?> loggerName : managedLoggers.keySet()) {
        Log4jLogger logger = managedLoggers.get(loggerName);
        FACTORY_LOGGER.info("LOG: Setting appenders for logger:" + loggerName);
        System.out.println("STDOUT: Setting appenders for logger:" + loggerName);
        setFileAppendersForLogger(logger);
      }
      switchedToFileAppender = true;
    }
  }

  private static void initAppenders() {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    systemLogAppender = initRollingFileAppender(config, AppenderConfiguration.SYSTEM_LOG_APPENDER);
    accessLogAppender = initRollingFileAppender(config, AppenderConfiguration.ACCESS_LOG_APPENDER);
    auditLogAppender = initRollingFileAppender(config, AppenderConfiguration.AUDIT_LOG_APPENDER);
    proxyLogAppender = initRollingFileAppender(config, AppenderConfiguration.PROXY_LOG_APPENDER);
  }

  private static Appender initRollingFileAppender(
      Configuration configuration, AppenderConfiguration appenderConfiguration) {
    System.out.println("STDOUT: Initiating appenders");
    System.out.println(logFilePath + File.separatorChar + appenderConfiguration.getFileName());
    RollingFileAppender appender =
        RollingFileAppender.newBuilder()
            .withName(appenderConfiguration.getName())
            .withFileName(logFilePath + File.separatorChar + appenderConfiguration.getFileName())
            .withFilePattern(
                logFilePath + File.separatorChar + appenderConfiguration.getFilePattern())
            .withLayout(getLayout())
            .withPolicy(
                SizeBasedTriggeringPolicy.createPolicy(AppenderConfiguration.getMaxFileSize()))
            .withStrategy(getRolloverStrategy(configuration))
            .build();
    appender.start();

    configuration.addAppender(appender);
    return appender;
  }

  private static void setFileAppendersForLogger(Log4jLogger logger) {
    Level level = determineLogLevel(logLevel);

    System.out.println("LEVEL:" + level.toString());
    String systemLoggerName = logger.systemLogger.getName();
    String accessLoggerName = logger.accessLogger.getName();
    String auditLoggerName = logger.auditLogger.getName();
    String proxyLoggerName = logger.proxyLogger.getName();

    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    LoggerConfig systemLoggerConfig = initLogger(systemLoggerName, systemLogAppender, level);
    config.addLogger(systemLoggerName, systemLoggerConfig);

    System.out.println("Adding " + accessLogAppender.getName() + " to "+ accessLoggerName);
    LoggerConfig accessLoggerConfig = initLogger(accessLoggerName, accessLogAppender, level);
    config.addLogger(accessLoggerName, accessLoggerConfig);

    LoggerConfig auditLoggerConfig = initLogger(auditLoggerName, auditLogAppender, level);
    config.addLogger(auditLoggerName, auditLoggerConfig);

    LoggerConfig proxyLoggerConfig = initLogger(proxyLoggerName, proxyLogAppender, level);
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
    }
    return level;
  }

  /**
   * Returns the log message layout according to the product requirements.
   *
   * @return The message layout.
   */
  private static Layout getLayout() {
    return PatternLayout.newBuilder().withPattern(AppenderConfiguration.getPatternLayout()).build();
  }

  /**
   * Retrieves default rollover strategy for rolling file appenders.
   *
   * @param configuration log4j2 configuration context
   * @return The rollover strategy
   */
  private static RolloverStrategy getRolloverStrategy(Configuration configuration) {
    return DefaultRolloverStrategy.newBuilder()
        .withMax(AppenderConfiguration.getMaxBackupIndex())
        .withConfig(configuration)
        .build();
  }
}
