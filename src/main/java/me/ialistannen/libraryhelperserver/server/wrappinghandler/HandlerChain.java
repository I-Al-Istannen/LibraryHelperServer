package me.ialistannen.libraryhelperserver.server.wrappinghandler;


import io.undertow.server.HttpHandler;
import java.util.Objects;
import java.util.function.Function;

/**
 * Chains handlers.
 */
public class HandlerChain {

  private Function<HttpHandler, HttpHandler> chain;

  private HandlerChain(Function<HttpHandler, HttpHandler> function) {
    this.chain = Objects.requireNonNull(function, "function can not be null!");
  }

  public static HandlerChain start(Function<HttpHandler, HttpHandler> function) {
    return new HandlerChain(function);
  }

  public HandlerChain next(Function<HttpHandler, HttpHandler> function) {
    chain = chain.compose(function);
    return this;
  }

  public HttpHandler complete(HttpHandler handler) {
    return chain.apply(handler);
  }
}
