package me.ialistannen.libraryhelperserver.server.utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.undertow.server.HttpServerExchange;
import java.io.InputStreamReader;
import java.util.Optional;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;

/**
 * Reads Json.
 */
public interface JsonReader {

  /**
   * Reads the body as a Json tree.
   *
   * @param exchange The {@link HttpServerExchange} to read from
   * @return The read {@link JsonObject}
   */
  default JsonObject readTree(HttpServerExchange exchange) {
    return readObject(exchange, JsonObject.class).orElse(null);
  }

  /**
   * Reads the body as a Json tree.
   *
   * @param exchange The {@link HttpServerExchange} to read from
   * @param clazz The Class of the object to read it as.
   * @param <T> The type of the returned object
   * @return The read Object
   */
  default <T> Optional<T> readObject(HttpServerExchange exchange, Class<T> clazz) {
    try {
      T object = DatabaseUtil.getGson().fromJson(
          new InputStreamReader(exchange.getInputStream()), clazz
      );
      return Optional.ofNullable(object);
    } catch (JsonSyntaxException e) {
      return Optional.empty();
    }
  }
}
