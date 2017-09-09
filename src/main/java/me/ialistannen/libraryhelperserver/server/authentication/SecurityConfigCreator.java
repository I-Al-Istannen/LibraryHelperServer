package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.util.Headers;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;

/**
 *
 */
public class SecurityConfigCreator {

  /**
   * @return The default config
   */
  public static Config create() {
    Config config = new Config();

    HeaderClient jwtClient = new HeaderClient(
        Headers.AUTHORIZATION_STRING,
        "Bearer ",
        new JwtAuthenticator(
            new SecretSignatureConfiguration(getSecret()),
            new SecretEncryptionConfiguration(getSecret())
        )
    );
    Clients clients = new Clients(jwtClient, new AnonymousClient());

    config.setClients(clients);

    return config;
  }

  public static String getSecret() {
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < Math.ceil(260 / "My secret".length()); i++) {
      result.append("My secret");
    }

    return result.toString();
  }
}
