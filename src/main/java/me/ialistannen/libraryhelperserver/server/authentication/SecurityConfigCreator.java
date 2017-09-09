package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.util.Headers;
import java.util.Collections;
import me.ialistannen.libraryhelperserver.util.Configs;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;

/**
 * A creator for the Pac4j config.
 */
public class SecurityConfigCreator {

  /**
   * @return The default config
   */
  public static Config create() {
    Config config = new Config();

    String secret = Configs.getCustom().getString("authentication.signing_secret");

    HeaderClient jwtClient = new HeaderClient(
        Headers.AUTHORIZATION_STRING,
        "Bearer ",
        new JwtAuthenticator(
            Collections.singletonList(new SecretSignatureConfiguration(secret)),
            Collections.emptyList()
        )
    );
    Clients clients = new Clients(jwtClient, new AnonymousClient());

    config.setClients(clients);

    return config;
  }
}
