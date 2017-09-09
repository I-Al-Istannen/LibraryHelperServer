package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.undertow.context.UndertowWebContext;
import org.pac4j.undertow.handler.SecurityHandler;

/**
 * A {@link RoutingHandler} that makes authentication easier.
 */
public class AuthenticatedRoutingHandler {

  private static final String UNPROTECTED_CLIENT_NAME = "AnonymousClient";
  private static final String PROTECTED_CLIENT_NAME = "HeaderClient";

  private final Config config;
  private final RoutingHandler routingHandler;
  private final SecurityLogic<Object, UndertowWebContext> securityLogic;

  private AuthenticatedRoutingHandler(Config config,
      SecurityLogic<Object, UndertowWebContext> securityLogic) {
    this.config = config;
    this.securityLogic = securityLogic;
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

    routingHandler.add(method, template, buildHandler(clientName, handler));
    return this;
  }

  private HttpHandler buildHandler(String clientName, HttpHandler handler) {
    return SecurityHandler
        .build(
            handler, config, clientName,
            null, null, null,
            securityLogic
        );
  }

  public RoutingHandler build() {
    return routingHandler;
  }

  public static AuthenticatedRoutingHandler builder(Config config) {
    return builder(config, null);
  }

  public static AuthenticatedRoutingHandler builder(Config config,
      SecurityLogic<Object, UndertowWebContext> securityLogic) {
    return new AuthenticatedRoutingHandler(config, securityLogic);
  }
}
