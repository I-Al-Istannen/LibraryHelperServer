package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import org.pac4j.undertow.account.Pac4jAccount;

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

}
