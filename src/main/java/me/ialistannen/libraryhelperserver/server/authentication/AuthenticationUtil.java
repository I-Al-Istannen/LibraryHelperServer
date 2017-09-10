package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.pac4j.core.config.Config;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.undertow.account.Pac4jAccount;
import org.pac4j.undertow.handler.SecurityHandler;

/**
 * Utilities for dealing with authentication.
 */
public class AuthenticationUtil {

  /**
   * @param exchange The {@link HttpServerExchange} to get it for
   * @return The {@link Pac4jAccount} or null if not present
   */
  public static Pac4jAccount getAccount(HttpServerExchange exchange) {
    SecurityContext securityContext = exchange.getSecurityContext();
    if (securityContext == null) {
      return null;
    }

    Account authenticatedAccount = securityContext.getAuthenticatedAccount();
    if (authenticatedAccount instanceof Pac4jAccount) {
      return (Pac4jAccount) authenticatedAccount;
    }

    return null;
  }

  /**
   * Wraps a handler to require authentication.
   *
   * @param toWrap The {@link HttpHandler} to wrap
   * @param config The {@link Config} to use
   * @param securityLogic The {@link SecurityLogic} to use
   * @return The wrapped handler
   */
  public static HttpHandler requireAuthentication(HttpHandler toWrap, Config config,
      JsonSecurityLogic securityLogic) {
    return SecurityHandler.build(
        toWrap, config, "HeaderClient",
        null, null, null,
        securityLogic
    );
  }
}
