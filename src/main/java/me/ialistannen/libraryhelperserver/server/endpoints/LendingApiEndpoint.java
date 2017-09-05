package me.ialistannen.libraryhelperserver.server.endpoints;

import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import java.lang.reflect.Field;
import java.util.Map;
import me.ialistannen.isbnlookuplib.book.Book;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelpercommon.book.StringBookDataKey;
import me.ialistannen.libraryhelperserver.db.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByIsbn;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Allows the user to lend books.
 */
public class LendingApiEndpoint implements HttpHandler {

  private static final Logger LOGGER = LogManager.getLogger(LendingApiEndpoint.class);
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
    Optional<Isbn> isbnOptional = isbnConverter.fromString(isbnStringOptional.get());

    if (!isbnOptional.isPresent()) {
      HttpStatusSender.badRequest(exchange, "Invalid ISBN!");
      return;
    }

    Optional<LoanableBook> bookOptional = databaseBrowser
        .getForQuery(QueryByIsbn.forIsbn(isbnOptional.get()));

    if (exchange.getRequestMethod().equals(Methods.PUT)) {
      if (!bookOptional.isPresent()) {
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

      LoanableBook loanableBook = bookOptional.get();
      loanableBook.setData(BORROWER_KEY, borrower);

      // update it in this case. Though elastic will reindex the whole document
      databaseMutator.addBook(loanableBook);

      Exchange.body().sendJson(exchange, MapBuilder.of("added", true).build());
    } else if (exchange.getRequestMethod().equals(Methods.DELETE)) {
      if (bookOptional.isPresent()) {
        LoanableBook loanableBook = bookOptional.get();
        // FIXME: 05.09.17 Add real method
        careFuckAllDelete(loanableBook, BORROWER_KEY);
        databaseMutator.addBook(loanableBook);
      }

      Exchange.body().sendJson(exchange, MapBuilder.of("deleted", true).build());
    }
  }

  private void careFuckAllDelete(LoanableBook book, BookDataKey key) {
    try {
      Field field = Book.class.getDeclaredField("dataMap");
      field.setAccessible(true);
      Map<?, ?> dataMap = (Map<?, ?>) field.get(book);
      dataMap.remove(key);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      LOGGER.log(Level.WARN, "My crap blew up", e);
    }
  }
}
