package me.ialistannen.libraryhelperserver.server.utilities;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.server.HttpServerExchange;

/**
 * Sends common HTTP status codes.
 */
public class HttpStatusSender {

  private static final Gson SERIALIZER = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();

  public static void badRequest(HttpServerExchange exchange, String message) {
    HttpStatusContainer httpStatusContainer = new HttpStatusContainer(400, message);

    sendResponse(exchange, httpStatusContainer);
  }

  public static void internalServerError(HttpServerExchange exchange) {
    String message = "An internal server error occurred while processing your request :/";
    internalServerError(exchange, message);
  }

  public static void internalServerError(HttpServerExchange exchange, String message) {
    HttpStatusContainer httpStatusContainer = new HttpStatusContainer(500, message);

    sendResponse(exchange, httpStatusContainer);
  }

  public static void forbidden(HttpServerExchange exchange, String message) {
    HttpStatusContainer httpStatusContainer = new HttpStatusContainer(403, message);

    sendResponse(exchange, httpStatusContainer);
  }

  public static void unauthorized(HttpServerExchange exchange, String message) {
    HttpStatusContainer httpStatusContainer = new HttpStatusContainer(401, message);

    sendResponse(exchange, httpStatusContainer);
  }

  public static void conflict(HttpServerExchange exchange, String message) {
    HttpStatusContainer httpStatusContainer = new HttpStatusContainer(409, message);

    sendResponse(exchange, httpStatusContainer);
  }

  private static void sendResponse(HttpServerExchange exchange,
      HttpStatusContainer httpStatusContainer) {
    exchange.setStatusCode(httpStatusContainer.statusCode);
    Exchange.body().sendJson(exchange, SERIALIZER.toJson(httpStatusContainer));
  }

  private static class HttpStatusContainer {

    int statusCode;
    String message;

    private HttpStatusContainer(int statusCode, String message) {
      this.statusCode = statusCode;
      this.message = message;
    }
  }
}
