package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import org.pac4j.core.config.Config;
import org.pac4j.undertow.handler.SecurityHandler;

/**
 * A {@link RoutingHandler} that makes authentication easier.
 */
public class AuthenticatedRoutingHandler {

  private static final String UNPROTECTED_CLIENT_NAME = "AnonymousClient";
  private static final String PROTECTED_CLIENT_NAME = "HeaderClient";

  private final Config config;
  private final RoutingHandler routingHandler;

  private AuthenticatedRoutingHandler(Config config) {
    this.config = config;
    this.routingHandler = new RoutingHandler();
  }

  public AuthenticatedRoutingHandler authenticated(String method, String template,
      HttpHandler handler) {

    return addHandler(method, template, PROTECTED_CLIENT_NAME, handler);
  }

  public AuthenticatedRoutingHandler unauthenticated(String method, String template,
      HttpHandler handler) {
    return addHandler(method, template, UNPROTECTED_CLIENT_NAME, handler);
  }

  private AuthenticatedRoutingHandler addHandler(String method, String template, String clientName,
      HttpHandler handler) {

    routingHandler.add(method, template, SecurityHandler.build(handler, config, clientName));
    return this;
  }

  public RoutingHandler build() {
    return routingHandler;
  }

  public static AuthenticatedRoutingHandler builder(Config config) {
    return new AuthenticatedRoutingHandler(config);
  }
}
