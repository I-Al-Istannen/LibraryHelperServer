package me.ialistannen.libraryhelperserver.server.endpoints;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByAuthorWildcards;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByIsbn;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByTitleRegex;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByTitleWildcards;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import org.elasticsearch.client.transport.TransportClient;

/**
 * The endpoint for searching.
 */
public class SearchApiEndpoint implements HttpHandler {

  private final Map<String, Function<String, List<LoanableBook>>> searchTypes = new HashMap<>();

  private final TransportClient client;

  public SearchApiEndpoint(TransportClient client) {
    this.client = client;

    searchTypes.put("title_regex", s -> QueryByTitleRegex.forRegex(s).makeQuery(client));
    searchTypes.put("title_wildcard", s -> QueryByTitleWildcards.forQuery(s).makeQuery(client));
    searchTypes.put("author_wildcard", s -> QueryByAuthorWildcards.forQuery(s).makeQuery(client));
    searchTypes.put("isbn", s -> {
      Optional<LoanableBook> bookOptional = QueryByIsbn.forIsbn(s).makeQuery(client);

      return bookOptional.isPresent()
          ? Collections.singletonList(bookOptional.get())
          : Collections.emptyList();
    });
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();

    String searchType = queryParameters.containsKey("search_type")
        ? queryParameters.get("search_type").getFirst()
        : null;
    Deque<String> queryList = queryParameters.get("query");

    if (searchType == null || queryList == null || !searchTypes.containsKey(searchType)) {
      exchange.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
      exchange.getResponseSender().send("Bad request.");
      return;
    }

    String query = String.join(" ", queryList);

    List<LoanableBook> books = searchTypes.get(searchType).apply(query);

    String json = DatabaseUtil.getGson().toJson(books);

    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/json");
    exchange.getResponseSender().send(json);
  }
}
