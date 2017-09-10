package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Collections;
import java.util.Optional;
import me.ialistannen.libraryhelperserver.db.types.users.UserDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.types.users.UserDatabaseMutator;
import me.ialistannen.libraryhelperserver.model.User;
import me.ialistannen.libraryhelperserver.model.hashing.bcrypt.BcryptHash;
import me.ialistannen.libraryhelperserver.model.hashing.bcrypt.BcryptHasher;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.undertow.account.Pac4jAccount;

/**
 * Allows the addition of users.
 */
public class UserCreationEndpoint implements HttpHandler {

  private UserDatabaseMutator mutator;
  private UserDatabaseBrowser browser;

  public UserCreationEndpoint(UserDatabaseMutator mutator, UserDatabaseBrowser browser) {
    this.mutator = mutator;
    this.browser = browser;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Pac4jAccount account = AuthenticationUtil.getAccount(exchange);

    if (account == null) {
      HttpStatusSender.unauthorized(exchange, "You are not allowed to do that.");
      return;
    }

    CommonProfile profile = account.getProfile();

    if (!profile.getRoles().contains("admin")) {
      HttpStatusSender.forbidden(exchange, "You are not allowed to do that");
      return;
    }

    Optional<UserData> userDataOptional = Exchange.body().readObject(exchange, UserData.class);

    if (!userDataOptional.isPresent()) {
      HttpStatusSender.badRequest(
          exchange, "Json malformed. Did you provide the 'username' and 'password' fields?"
      );
      return;
    }

    UserData userData = userDataOptional.get();

    if (browser.getUser(userData.username).isPresent()) {
      HttpStatusSender.conflict(exchange, "This user already exists in the database");
      return;
    }

    BcryptHash hash = BcryptHasher.getInstance().hash(userData.password);
    User user = new User(hash, userData.username, Collections.emptySet(), Collections.emptyMap());

    mutator.storeOrUpdateUser(user);

    Exchange.body().sendJson(exchange, MapBuilder.of("created", true).build());
  }

  private static class UserData {

    @SuppressWarnings("unused")
    private String username;
    @SuppressWarnings("unused")
    private String password;
  }
}
