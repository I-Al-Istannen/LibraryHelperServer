package me.ialistannen.libraryhelperserver.server.utilities;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Sends JSON.
 */
public interface JsonSender {

  /**
   * Sends JSON.
   *
   * @param exchange The {@link HttpServerExchange}
   * @param json The JSON to send
   */
  default void sendJson(HttpServerExchange exchange, String json) {
    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/json");
    exchange.getResponseSender().send(ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8)));
  }
}
