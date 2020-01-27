/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Author: Mike J&auml;ger
 *
 * <p>Creation Date: 23.04.2009
 *
 * <p>Completion Time: 17.06.2009
 *
 * <p>*****************************************************************************
 */
package org.oscm.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
  private static Map<String, FileAppender> fileAppendersMap = new ConcurrentHashMap<>();

  private static final String systemLogAppenderName = "besSystemLogAppender";
  private static final String accessLogAppenderName = "besAccessLogAppender";
  private static final String auditLogAppenderName = "besAuditLogAppender";
  private static final String reverseProxyLogAppenderName = "reverseProxyAppender";

  private static ConsoleAppender consoleAppender;
  private static boolean switchedToFileAppender = false;

  public static Log4jLogger getLogger(Class<?> category) {
    return getLogger(category, Locale.getDefault());
  }

  public static Log4jLogger getLogger(Class<?> category, Locale locale) {
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
   * @param filePath The path to the log files.
   * @param logConfigFile The path to the log4j configuration file.
   * @param logLevel The log level to be used.
   */
  public static void activateRollingFileAppender(
      String filePath, String logConfigFile, String logLevel) {
    synchronized (managedLoggers) {
      try {
        LoggerFactory.logLevel = logLevel;
        LoggerFactory.logFilePath = filePath;
        LoggerFactory.logConfigPath = logConfigFile;

        initAppenders();

        Iterator<Class<?>> iterator = managedLoggers.keySet().iterator();
        while (iterator.hasNext()) {
          Class<?> loggerName = iterator.next();
          Log4jLogger logger = managedLoggers.get(loggerName);
          setFileAppendersForLogger(logger);
        }
        switchedToFileAppender = true;
      } catch (IOException e) {
        System.err.println("Log file could not be created!");
      }
    }
  }

  private static void initAppenders() throws IOException {
    initAppenderForLogFileOf("system.log", systemLogAppenderName);
    initAppenderForLogFileOf("audit.log", auditLogAppenderName);
    initAppenderForLogFileOf("access.log", accessLogAppenderName);
    initAppenderForLogFileOf("reverseproxy.log", reverseProxyLogAppenderName);

    // TODO: Set these props

    //        // setting the max backup index and file size
    //        systemLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
    //        systemLogAppender.setMaxFileSize(MAX_FILE_SIZE);
    //
    //        accessLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
    //        accessLogAppender.setMaxFileSize(MAX_FILE_SIZE);
    //
    //        auditLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
    //        auditLogAppender.setMaxFileSize(MAX_FILE_SIZE);
    //
    //        reverseProxyLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
    //        reverseProxyLogAppender.setMaxFileSize(MAX_FILE_SIZE);
  }

  private static void initAppenderForLogFileOf(String logFileName, String appenderName) {
    FileAppender fileAppender =
        FileAppender.newBuilder()
            .setName(appenderName)
            .withFileName(logFilePath + logFileName)
            .setLayout(getLayout())
            .build();
    fileAppender.start();
    fileAppendersMap.put(appenderName, fileAppender);
  }

  // TODO: Modify this for logging to FILE
  private static void setFileAppendersForLogger(Log4jLogger logger) {

    Level level = determineLogLevel(logLevel);


//    changeFileAppenderIfNew(logger.systemLogger, fileAppendersMap.get(systemLogAppenderName));
//
//    changeFileAppenderIfNew(logger.accessLogger, fileAppendersMap.get(accessLogAppenderName));
//
//    changeFileAppenderIfNew(logger.auditLogger, fileAppendersMap.get(auditLogAppenderName));
//
//    changeFileAppenderIfNew(logger.proxyLogger, fileAppendersMap.get(reverseProxyLogAppenderName));

    FileAppender systemLogAppender = fileAppendersMap.get(systemLogAppenderName);
    FileAppender accessLogAppender = fileAppendersMap.get(accessLogAppenderName);
    FileAppender auditLogAppender = fileAppendersMap.get(auditLogAppenderName);
    FileAppender proxyLogAppender = fileAppendersMap.get(reverseProxyLogAppenderName);

    logger.systemLogger.addAppender(systemLogAppender);
    logger.systemLogger.addAppender(accessLogAppender);
    logger.systemLogger.addAppender(auditLogAppender);
    logger.systemLogger.addAppender(proxyLogAppender);

    setLogLevel(logger, level);
  }

  private static void changeFileAppenderIfNew(Logger logger, FileAppender newFileAppender) {
    Appender existingAppender =
        logger.getAppenders().isEmpty()
            ? null
            : (Appender) logger.getAppenders().values().toArray()[0];
    if (existingAppender == null) {
      logger.removeAppender(existingAppender);
      logger.addAppender(newFileAppender);
    } else if (existingAppender != newFileAppender) {
      logger.removeAppender(existingAppender);
      logger.addAppender(newFileAppender);
    }
    return;
  }

  // TODO: Modify this for logging to CONSOLE
  private static void setConsoleAppenderForLogger(Log4jLogger logger) {
    //        if (consoleAppender == null) {
    //            consoleAppender = new ConsoleAppender(getLayout());
    //            consoleAppender.setName("OSCM console appender");
    //        }
    //        Level level = determineLogLevel(logLevel);
    //
    //        logger.systemLogger.removeAllAppenders();
    //        logger.systemLogger.addAppender(consoleAppender);
    //
    //        logger.accessLogger.removeAllAppenders();
    //        logger.accessLogger.addAppender(consoleAppender);
    //
    //        logger.auditLogger.removeAllAppenders();
    //        logger.auditLogger.addAppender(consoleAppender);
    //
    //        logger.proxyLogger.removeAllAppenders();
    //        logger.proxyLogger.addAppender(consoleAppender);
    //
    //        setLogLevel(logger, level);
  }

  /**
   * Sets the log level for the given logger. If there is no property file to be used, the log level
   * will be set to the value as given in the level parameter.
   *
   * @param logger The logger to be modified.
   * @param level The log level to be set if no property file can be found.
   */
  public static void setLogLevel(Log4jLogger logger, Level level) {
    // TODO: Fix this

    //        if (logConfigPath != null && new File(logConfigPath).exists()) {
    //            PropertyConfigurator.configureAndWatch(logConfigPath, 60000);
    //        } else {
    logger.systemLogger.setLevel(level);
    logger.auditLogger.setLevel(level);

    // used INFO log level as default for the reverse proxy logger
    logger.proxyLogger.setLevel(Level.INFO);

    // all access operations will be logged with info level
    logger.accessLogger.setLevel(Level.INFO);
    //        }
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
    Layout layout =
        PatternLayout.newBuilder()
            .withPattern("%d{MM/dd/yyyy_HH:mm:ss.SSS} FSP_INTS-BSS: %p: ThreadID %t: %c{1}: %m%n")
            .build();
    return layout;
  }
}
