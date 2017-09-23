package me.ialistannen.libraryhelperserver.server.utilities;

import io.undertow.server.HttpServerExchange;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Function;

/**
 * Some methods to help deal with query parameters.
 */
public interface QueryParams {

  /**
   * @param exchange The {@link HttpServerExchange}
   * @param name The name of the parameter
   * @return The value for it
   */
  default Optional<String> getSingle(HttpServerExchange exchange, String name) {
    return Optional.ofNullable(
        getTransformed(exchange, name, Deque::getFirst)
    );
  }

  /**
   * @param exchange The {@link HttpServerExchange}
   * @param name The name of the parameter
   * @param transformation The transformation to apply
   * @param <T> The return type
   * @return The value or NULL if not found
   */
  default <T> T getTransformed(HttpServerExchange exchange, String name,
      Function<Deque<String>, T> transformation) {
    Deque<String> strings = exchange.getQueryParameters().get(name);

    if (strings == null) {
      return null;
    }

    return transformation.apply(strings);
  }

  /**
   * Returns a function for {@link #getTransformed(HttpServerExchange, String, Function)} that
   * combines it in a single string.
   *
   * @param delimiter The delimiter to use
   * @return A function ready to be passed to {@link #getTransformed(HttpServerExchange, String,
   * Function)}
   */
  static Function<Deque<String>, String> combined(String delimiter) {
    return strings -> String.join(delimiter, strings);
  }
}
