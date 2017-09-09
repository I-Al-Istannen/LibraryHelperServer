package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Optional;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.JwtClaims;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.undertow.account.Pac4jAccount;

/**
 * The endpoint to authenticate yourself and to generate a json token.
 */
public class AuthenticationEndpoint implements HttpHandler {

  private static final Logger LOGGER = LogManager.getLogger(AuthenticationEndpoint.class);

  private JwtGenerator<CommonProfile> generator;
  private TemporalAmount expirationTime = Duration.ofMinutes(5);

  public AuthenticationEndpoint(String secret) {
    generator = new JwtGenerator<>(new SecretSignatureConfiguration(secret));
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Pac4jAccount account = getAccount(exchange);

    if (account == null || account.getProfile() == null) {
      HttpStatusSender.forbidden(exchange, "No profile found!");
      LOGGER.info(
          "Denied request for {} as the account ({}) or profile ({}) was null.",
          exchange.getSourceAddress(), account == null,
          account == null ? "unknown" : account.getProfile() == null
      );
      return;
    }

    Optional<RequestPojo> requestPojo = Exchange.body().readObject(exchange, RequestPojo.class);

    if (!requestPojo.isPresent()) {
      HttpStatusSender.badRequest(
          exchange,
          "JSON is malformed! Missing 'username' or 'password' or they are not strings."
      );
      return;
    }

    RequestPojo request = requestPojo.get();

    LOGGER.info("Created JWT for user '{}'", request.username);

    CommonProfile profile = account.getProfile();
    profile.addAttribute(JwtClaims.EXPIRATION_TIME, Date.from(Instant.now().plus(expirationTime)));
    profile.setId(request.username);

    String jwt = generator.generate(profile);

    Exchange.body().sendJson(exchange, MapBuilder.of("token", jwt).build());
  }

  private Pac4jAccount getAccount(HttpServerExchange exchange) {
    SecurityContext securityContext = exchange.getSecurityContext();
    if (securityContext == null) {
      return null;
    }

    Account authenticatedAccount = securityContext.getAuthenticatedAccount();
    System.out.println(authenticatedAccount == null);
    if (authenticatedAccount instanceof Pac4jAccount) {
      return (Pac4jAccount) authenticatedAccount;
    }

    return null;
  }

  private static class RequestPojo {

    @SuppressWarnings("unused")
    private String username;
    @SuppressWarnings("unused")
    private String password;
  }
}
