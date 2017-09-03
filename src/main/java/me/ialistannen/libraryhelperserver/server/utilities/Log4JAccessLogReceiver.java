package me.ialistannen.libraryhelperserver.server.utilities;

import io.undertow.server.handlers.accesslog.AccessLogReceiver;
import org.apache.logging.log4j.Logger;

/**
 * An {@link AccessLogReceiver} using Slf4J.
 */
public class Log4JAccessLogReceiver implements AccessLogReceiver {

  private Logger logger;

  public Log4JAccessLogReceiver(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void logMessage(String message) {
    logger.info(message);
  }
}
