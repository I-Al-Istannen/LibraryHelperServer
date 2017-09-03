package me.ialistannen.libraryhelperserver.server.wrappinghandler;

import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import me.ialistannen.libraryhelperserver.server.utilities.Log4JAccessLogReceiver;
import me.ialistannen.libraryhelperserver.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A collection of useful handlers.
 */
public class CustomHandlers {

  private static final Logger LOGGER = LogManager.getLogger(CustomHandlers.class);

  public static HttpHandler gzip(HttpHandler next) {
    return new EncodingHandler(
        new ContentEncodingRepository().addEncodingHandler(
            "gzip",
            new GzipEncodingProvider(), 1000,
            Predicates.maxContentSize(20)
        )
    ).setNext(next);
  }

  public static HttpHandler accessLog(HttpHandler next) {
    return new AccessLogHandler(
        next,
        new Log4JAccessLogReceiver(LogManager.getLogger("me.ialistannen.accesslog")),
        "%h %l \"%r\" %s %b | Ref: \"%{i,Referer}\" Agent: \"%{i,User-Agent}",
        ClassLoader.getSystemClassLoader()
    );
  }

  public static HttpHandler resource(String prefix) {
    Path path = Paths.get(
        Configs.getCustom().getString("assets.basepath")
            .replace("~", System.getProperty("user.home"))
    )
        .toAbsolutePath()
        .resolve(prefix);

    LOGGER.info("Base path is '{}'", path.toString());

    ResourceManager resourceManager = new FileResourceManager(path.toFile(), 1024 * 1024);

    ResourceHandler resourceHandler = new ResourceHandler(resourceManager);
    resourceHandler.setCacheTime((int) TimeUnit.HOURS.toSeconds(4));

    return resourceHandler;
  }
}
