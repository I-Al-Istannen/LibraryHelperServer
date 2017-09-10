package me.ialistannen.libraryhelperserver.server.authentication;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import me.ialistannen.libraryhelperserver.db.types.users.UserDatabaseMutator;
import me.ialistannen.libraryhelperserver.model.User;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;
import me.ialistannen.libraryhelperserver.model.hashing.bcrypt.BcryptHasher;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.undertow.account.Pac4jAccount;

/**
 * An endpoint to delete a user.
 */
public class UserDeletionEndpoint implements HttpHandler {

  private UserDatabaseMutator mutator;

  public UserDeletionEndpoint(UserDatabaseMutator mutator) {
    this.mutator = mutator;

    Hash hash = BcryptHasher.getInstance().hash("password");
    Set<String> roles = new HashSet<>(Arrays.asList("admin", "that guy"));
    User user = new User(hash, "I-Al-Istannen", roles, Collections.emptyMap());
    mutator.storeOrUpdateUser(user);
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

    Optional<Request> requestOptional = Exchange.body().readObject(exchange, Request.class);

    if (!requestOptional.isPresent()) {
      HttpStatusSender.badRequest(
          exchange, "Malformed JSON. Did you provide the 'username' field?"
      );
      return;
    }

    Request request = requestOptional.get();

    mutator.deleteUser(request.username);

    Exchange.body().sendJson(exchange, MapBuilder.of("deleted", true).build());
  }

  private static class Request {

    @SuppressWarnings("unused")
    private String username;
  }
}
