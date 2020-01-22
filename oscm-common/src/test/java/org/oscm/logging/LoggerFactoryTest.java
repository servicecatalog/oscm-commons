/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 15.01.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.logging;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.types.enumtypes.LogMessageIdentifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.*;

public class LoggerFactoryTest {

  private static PrintStream mockConsoleOut;
  private static Log4jLogger oscmLogger;
  private static ByteArrayOutputStream outputStream;
  private URI temporaryLogFilesDirectoryURI;

  private static final String TEST_EXCEPTION_MESSAGE = "Some unique exception message";

  @BeforeClass
  public static void before(){
    outputStream = new ByteArrayOutputStream();
    mockConsoleOut = new PrintStream(outputStream);
    System.setOut(mockConsoleOut);
    oscmLogger = LoggerFactory.getLogger(LoggerFactoryTest.class, Locale.ENGLISH);
  }

  @Before
  public void setUp() {
    try {
      temporaryLogFilesDirectoryURI = Files.createTempDirectory("temporaryLogFiles").toUri();
    } catch (IOException e) {
      fail(e.getMessage(), e);
    }
  }

  @After
  public void tearDown() {
    reinitializeLoggerFactory();
  }

  @Test
  public void shouldGetLogger() {
    // WHEN
    oscmLogger = LoggerFactory.getLogger(this.getClass());

    // THEN
    assertNotNull(oscmLogger);
  }

    @Test
    public void shouldGetLoggerWithLocale() {
      // WHEN
      oscmLogger = LoggerFactory.getLogger(this.getClass(), Locale.ENGLISH);

      // THEN
      assertNotNull(oscmLogger);
    }

  @Test
  public void when_logERROR_then_shouldLogToConsole() {
    //GIVEN
    LoggerFactory.setLogLevel(oscmLogger, Level.ERROR);

    // WHEN
    oscmLogger.logError(
        Log4jLogger.SYSTEM_LOG,
        new ValidationException(TEST_EXCEPTION_MESSAGE),
        LogMessageIdentifier.ERROR_READING_PROPERTIES);

    // THEN
    assertTrue(outputStream.toString().contains("[main] ERROR"));
    assertTrue(outputStream.toString().contains(TEST_EXCEPTION_MESSAGE));
    assertTrue(
        outputStream.toString().contains(LogMessageIdentifier.ERROR_READING_PROPERTIES.getMsgId()));
  }

  @Test
  public void when_logWARN_then_shouldLogToConsole() {
    //GIVEN
    LoggerFactory.setLogLevel(oscmLogger, Level.WARN);

    // WHEN
    oscmLogger.logWarn(
            Log4jLogger.SYSTEM_LOG,
            new ValidationException(TEST_EXCEPTION_MESSAGE),
            LogMessageIdentifier.ERROR_READING_PROPERTIES);

    // THEN
    assertTrue(outputStream.toString().contains("[main] WARN"));
    assertTrue(outputStream.toString().contains(TEST_EXCEPTION_MESSAGE));
    assertTrue(
            outputStream.toString().contains(LogMessageIdentifier.ERROR_READING_PROPERTIES.getMsgId()));
  }

  @Test
  public void when_logINFO_then_shouldLogToConsole() {
    //GIVEN
    LoggerFactory.setLogLevel(oscmLogger, Level.INFO);

    // WHEN
    oscmLogger.logInfo(
            Log4jLogger.SYSTEM_LOG,
            LogMessageIdentifier.ERROR_READING_PROPERTIES);

    // THEN
    assertTrue(outputStream.toString().contains("[main] INFO"));
    assertTrue(
            outputStream.toString().contains(LogMessageIdentifier.ERROR_READING_PROPERTIES.getMsgId()));
  }

  @Test
  public void when_logDEBUG_then_shouldLogToConsole() {
    //GIVEN
    LoggerFactory.setLogLevel(oscmLogger, Level.DEBUG);

    // WHEN
    oscmLogger.logDebug(TEST_EXCEPTION_MESSAGE);

    // THEN
    assertTrue(outputStream.toString().contains("[main] DEBUG"));
    assertTrue(
            outputStream.toString().contains(TEST_EXCEPTION_MESSAGE));
  }

  @Test
  public void given_standardLogger_then_shouldLogToFiles() throws IOException {
    // GIVEN
    LoggerFactory.activateRollingFileAppender(
        temporaryLogFilesDirectoryURI.getPath(), null, "ERROR");
    oscmLogger = LoggerFactory.getLogger(this.getClass());

    // WHEN
    invokeFileLoggerFor(Log4jLogger.SYSTEM_LOG);
    invokeFileLoggerFor(Log4jLogger.ACCESS_LOG);
    invokeFileLoggerFor(Log4jLogger.AUDIT_LOG);
    invokeFileLoggerFor(Log4jLogger.PROXY_LOG);

    // THEN
    assertEquals(4, Files.list(Paths.get(temporaryLogFilesDirectoryURI)).count());
    assertTrue(
        Files.list(Paths.get(temporaryLogFilesDirectoryURI))
            .map(Path::getFileName)
            .map(Path::toString)
            .collect(Collectors.toSet())
            .containsAll(
                Lists.newArrayList("audit.log", "system.log", "access.log", "reverseproxy.log")));
    assertTrue(
        Files.list(Paths.get(temporaryLogFilesDirectoryURI)).collect(Collectors.toList()).stream()
            .allMatch(p -> logFilesContentValidationPredicate().test(p)));
  }

//  @Test
//  public void given_loggerWithLocale_then_shouldLogToFiles() throws IOException {
//    // GIVEN
//    LoggerFactory.activateRollingFileAppender(
//        temporaryLogFilesDirectoryURI.getPath(), null, "DEBUG");
//    oscmLogger = LoggerFactory.getLogger(this.getClass(), Locale.ENGLISH);
//
//    // WHEN
//    invokeFileLoggerFor(Log4jLogger.SYSTEM_LOG);
//    invokeFileLoggerFor(Log4jLogger.ACCESS_LOG);
//    invokeFileLoggerFor(Log4jLogger.AUDIT_LOG);
//    invokeFileLoggerFor(Log4jLogger.PROXY_LOG);
//
//    // THEN
//    assertEquals(4, Files.list(Paths.get(temporaryLogFilesDirectoryURI)).count());
//    assertTrue(
//        Files.list(Paths.get(temporaryLogFilesDirectoryURI))
//            .map(Path::getFileName)
//            .map(Path::toString)
//            .collect(Collectors.toSet())
//            .containsAll(
//                Lists.newArrayList("audit.log", "system.log", "access.log", "reverseproxy.log")));
//    assertTrue(
//        Files.list(Paths.get(temporaryLogFilesDirectoryURI)).collect(Collectors.toList()).stream()
//            .allMatch(p -> logFilesContentValidationPredicate().test(p)));
//  }

  private void invokeFileLoggerFor(int logFileIdentifier) {
    oscmLogger.logError(
        logFileIdentifier,
        new ValidationException(TEST_EXCEPTION_MESSAGE),
        LogMessageIdentifier.ERROR_READING_PROPERTIES);
  }

  private static Predicate<Path> logFilesContentValidationPredicate() {
    return p -> {
      try {
        return Files.lines(p).collect(Collectors.toList()).stream()
                .anyMatch(line1 -> line1.contains(TEST_EXCEPTION_MESSAGE))
            && Files.lines(p).collect(Collectors.toList()).stream()
                .anyMatch(
                    line2 ->
                        line2.contains(LogMessageIdentifier.ERROR_READING_PROPERTIES.getMsgId()));
      } catch (IOException e) {
        return false;
      }
    };
  }

  private void reinitializeLoggerFactory() {
    try {
      reinitializeField("logLevel");
      reinitializeField("logFilePath");
      reinitializeField("logConfigPath");
      reinitializeField("systemLogAppender");
      reinitializeField("accessLogAppender");
      reinitializeField("auditLogAppender");
      reinitializeField("reverseProxyLogAppender");
      reinitializeField("consoleAppender");
      reinitializeField("switchedToFileAppender", false);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail(e.getMessage(), e);
    }
  }

  private void reinitializeField(String fieldName)
      throws NoSuchFieldException, IllegalAccessException {
    Field fieldToReinitialize = LoggerFactory.class.getDeclaredField(fieldName);
    boolean isFieldAccessible = fieldToReinitialize.isAccessible();

    if (!isFieldAccessible) fieldToReinitialize.setAccessible(true);
    fieldToReinitialize.set(null, null);
    if (!isFieldAccessible) fieldToReinitialize.setAccessible(false);
  }

  private void reinitializeField(String fieldName, Object initialFieldValue)
      throws NoSuchFieldException, IllegalAccessException {
    Field fieldToReinitialize = LoggerFactory.class.getDeclaredField(fieldName);
    boolean isFieldAccessible = fieldToReinitialize.isAccessible();

    if (!isFieldAccessible) fieldToReinitialize.setAccessible(true);
    fieldToReinitialize.set(null, initialFieldValue);
    if (!isFieldAccessible) fieldToReinitialize.setAccessible(false);
  }
}
