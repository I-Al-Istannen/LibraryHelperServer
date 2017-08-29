package me.ialistannen.libraryhelperserver.server.endpoints;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByAuthorWildcards;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByIsbn;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByTitleRegex;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByTitleWildcards;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.server.utilities.QueryParams;
import org.elasticsearch.client.transport.TransportClient;

/**
 * The endpoint for searching.
 */
public class SearchApiEndpoint implements HttpHandler {

  private final Map<String, Function<String, List<LoanableBook>>> searchTypes = new HashMap<>();

  public SearchApiEndpoint(TransportClient client) {
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
    String query = Exchange.queryParams()
        .getTransformed(exchange, "query", QueryParams.combined(" "));

    Optional<String> searchType = Exchange.queryParams()
        .getSingle(exchange, "search_type");

    if (query == null) {
      HttpStatusSender.badRequest(exchange, "You must pass a 'query' parameter.");
      return;
    }
    if (!searchType.isPresent()) {
      HttpStatusSender.badRequest(exchange, "You must pass a 'search_type' parameter.");
      return;
    }
    if (!searchTypes.containsKey(searchType.get())) {
      HttpStatusSender.badRequest(exchange, String.format(
          "Invalid search_type: '%s'. Allowed are '%s'",
          searchType.get(), String.join(", ", searchTypes.keySet())
      ));
      return;
    }

    List<LoanableBook> books = searchTypes.get(searchType.get()).apply(query);

    String json = DatabaseUtil.getGson().toJson(books);

    Exchange.body().sendJson(exchange, json);
  }
}
