package me.ialistannen.libraryhelperserver.server.endpoints;

import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import java.util.List;
import java.util.Optional;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelpercommon.book.StringBookDataKey;
import me.ialistannen.libraryhelperserver.db.queries.QueryField;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.model.search.SearchType;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import me.ialistannen.libraryhelperserver.util.OptionalConverter;

/**
 * Allows the user to lend books.
 */
public class LendingApiEndpoint implements HttpHandler {

  private static final BookDataKey BORROWER_KEY = new StringBookDataKey("BORROWER");

  private IsbnConverter isbnConverter;
  private BookDatabaseMutator databaseMutator;
  private BookDatabaseBrowser databaseBrowser;

  public LendingApiEndpoint(IsbnConverter isbnConverter, BookDatabaseMutator databaseMutator,
      BookDatabaseBrowser databaseBrowser) {
    this.isbnConverter = isbnConverter;
    this.databaseMutator = databaseMutator;
    this.databaseBrowser = databaseBrowser;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Optional<String> isbnStringOptional = Exchange.queryParams().getSingle(exchange, "isbn");

    if (!isbnStringOptional.isPresent()) {
      HttpStatusSender.badRequest(exchange, "No 'isbn' parameter given");
      return;
    }
    Optional<Isbn> isbnOptional = OptionalConverter.toJDK(
        isbnConverter.fromString(isbnStringOptional.get())
    );

    if (!isbnOptional.isPresent()) {
      HttpStatusSender.badRequest(exchange, "Invalid ISBN!");
      return;
    }

    List<LoanableBook> bookOptional = databaseBrowser
        .getForQuery(
            SearchType.EXACT_MATCH, QueryField.ISBN, isbnOptional.get().getDigitsAsString()
        );

    if (exchange.getRequestMethod().equals(Methods.PUT)) {
      if (bookOptional.isEmpty()) {
        HttpStatusSender.badRequest(exchange, "Book not known");
        return;
      }

      JsonObject jsonObject = Exchange.body().readTree(exchange);

      if (jsonObject == null) {
        HttpStatusSender.badRequest(exchange, "Malformed json!");
        return;
      }
      if (!jsonObject.has("borrower")) {
        HttpStatusSender.badRequest(exchange, "You need to provide a 'borrower' param.");
        return;
      }
      String borrower = jsonObject.getAsJsonPrimitive("borrower").getAsString();

      LoanableBook loanableBook = bookOptional.get(0);
      loanableBook.setData(BORROWER_KEY, borrower);

      // update it in this case. Though elastic will reindex the whole document
      databaseMutator.addBook(loanableBook);

      Exchange.body().sendJson(exchange, MapBuilder.of("added", true).build());
    } else if (exchange.getRequestMethod().equals(Methods.DELETE)) {
      if (!bookOptional.isEmpty()) {
        LoanableBook loanableBook = bookOptional.get(0);
        loanableBook.removeData(BORROWER_KEY);
        databaseMutator.addBook(loanableBook);
      }

      Exchange.body().sendJson(exchange, MapBuilder.of("deleted", true).build());
    }
  }
}
