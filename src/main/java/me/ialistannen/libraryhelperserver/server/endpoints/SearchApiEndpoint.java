package me.ialistannen.libraryhelperserver.server.endpoints;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import me.ialistannen.libraryhelpercommon.book.IntermediaryBook;
import me.ialistannen.libraryhelperserver.db.queries.QueryField;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import me.ialistannen.libraryhelperserver.model.search.SearchType;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.server.utilities.QueryParams;
import me.ialistannen.libraryhelperserver.util.EnumUtil;

/**
 * The endpoint for searching.
 */
public class SearchApiEndpoint implements HttpHandler {

  private final Map<String, QueryField> queryFieldMap = EnumUtil
      .getReverseMapping(QueryField.class, this::formatEnumName);
  private final Map<String, SearchType> searchTypeMap = EnumUtil
      .getReverseMapping(SearchType.class, this::formatEnumName);

  private BookDatabaseBrowser bookDatabaseBrowser;

  public SearchApiEndpoint(BookDatabaseBrowser bookDatabaseBrowser) {
    this.bookDatabaseBrowser = bookDatabaseBrowser;
  }

  private String formatEnumName(Enum<?> enumEntry) {
    return enumEntry.name().toLowerCase();
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    String query = Exchange.queryParams()
        .getTransformed(exchange, "query", QueryParams.combined(" "));

    if (query == null) {
      HttpStatusSender.badRequest(exchange, "You must pass a 'query' parameter.");
      return;
    }

    Optional<String> searchTypeName = getParameterIfValid(
        exchange, "search_type", searchTypeMap.keySet()
    );
    if (!searchTypeName.isPresent()) {
      return;
    }

    Optional<String> fieldName = getParameterIfValid(
        exchange, "field", queryFieldMap.keySet()
    );
    if (!fieldName.isPresent()) {
      return;
    }

    QueryField queryField = queryFieldMap.get(fieldName.get());
    SearchType searchType = searchTypeMap.get(searchTypeName.get());

    if (searchType != SearchType.FUZZY && !queryField.hasRawSubField()) {
      HttpStatusSender.badRequest(
          exchange,
          String.format("The field '%s' can only be searched fuzzily", fieldName.get())
      );
      return;
    }

    List<IntermediaryBook> books = bookDatabaseBrowser.getForQuery(searchType, queryField, query)
        .stream()
        .map(IntermediaryBook::fromLoanableBook)
        .collect(Collectors.toList());
    String json = DatabaseUtil.getGson().toJson(books);

    Exchange.body().sendJson(exchange, json);
  }

  private Optional<String> getParameterIfValid(HttpServerExchange exchange, String name,
      Collection<String> allowedChoices) {

    Optional<String> single = Exchange.queryParams().getSingle(exchange, name);

    if (!single.isPresent()) {
      HttpStatusSender.badRequest(exchange, String.format("You must pass a '%s' parameter.", name));
      return Optional.empty();
    }

    if (!allowedChoices.contains(single.get())) {
      HttpStatusSender.badRequest(exchange, String.format(
          "Invalid search_type: '%s'. Allowed are '%s'",
          single.get(), String.join(", ", allowedChoices)
      ));
      return Optional.empty();
    }

    return single;
  }
}
