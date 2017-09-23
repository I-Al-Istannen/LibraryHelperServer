package me.ialistannen.libraryhelperserver.server.endpoints;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Map.Entry;
import java.util.Optional;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.libraryhelpercommon.book.IntermediaryBook;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.types.book.elastic.queries.QueryByIsbn;
import me.ialistannen.libraryhelperserver.db.util.exceptions.DatabaseException;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The endpoint modifying a given book.
 */
public class ModificationEndpoint implements HttpHandler {

  private static final Logger LOGGER = LogManager.getLogger(ModificationEndpoint.class);

  private BookDatabaseMutator databaseMutator;
  private BookDatabaseBrowser databaseBrowser;

  public ModificationEndpoint(BookDatabaseMutator databaseMutator,
      BookDatabaseBrowser databaseBrowser) {

    this.databaseMutator = databaseMutator;
    this.databaseBrowser = databaseBrowser;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Optional<String> isbnStringOptional = Exchange.queryParams().getSingle(exchange, "isbn");

    if (!isbnStringOptional.isPresent()) {
      HttpStatusSender.badRequest(exchange, "Request is missing the 'isbn' parameter.");
      return;
    }

    Optional<IntermediaryBook> intermediaryBookOptional = Exchange.body()
        .readObject(exchange, IntermediaryBook.class);

    if (!intermediaryBookOptional.isPresent()) {
      HttpStatusSender.badRequest(exchange, "The body must be a valid IntermediaryBook.");
      return;
    }

    String isbnString = isbnStringOptional.get();

    Optional<LoanableBook> storedBookOptional = databaseBrowser.getForQuery(
        QueryByIsbn.forIsbn(isbnString)
    );

    if (!storedBookOptional.isPresent()) {
      HttpStatusSender.notFound(exchange, "A book with that ISBN was not found");
      return;
    }

    LoanableBook changedBook = intermediaryBookOptional.get().toLoanableBook();
    LoanableBook storedBook = storedBookOptional.get();

    if (!areIsbnsEqual(storedBook, changedBook)) {
      HttpStatusSender.badRequest(exchange, "The Isbn does not match the stored book.");
      return;
    }

    for (Entry<BookDataKey, Object> entry : changedBook.getAllData().entrySet()) {
      if (entry.getValue() == null) {
        storedBook.removeData(entry.getKey());
      } else {
        storedBook.setData(entry.getKey(), entry.getValue());
      }
    }

    try {
      databaseMutator.addBook(storedBook);
    } catch (DatabaseException e) {
      LOGGER.log(
          Level.WARN,
          "A database error occurred trying to store this book: " + storedBook,
          e
      );

      HttpStatusSender.internalServerError(exchange);
    }

    Exchange.body().sendJson(exchange, MapBuilder.of("modified", true).build());
  }

  private boolean areIsbnsEqual(LoanableBook one, LoanableBook two) {
    StandardBookDataKeys isbnKey = StandardBookDataKeys.ISBN;
    return one.getData(isbnKey) != null && one.getData(isbnKey).equals(two.getData(isbnKey));
  }
}
