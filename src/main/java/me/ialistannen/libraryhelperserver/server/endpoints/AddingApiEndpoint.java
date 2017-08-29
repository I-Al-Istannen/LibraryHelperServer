package me.ialistannen.libraryhelperserver.server.endpoints;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.exceptions.DatabaseException;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The endpoint for adding stuff.
 */
public class AddingApiEndpoint implements HttpHandler {

  private static final Logger LOGGER = LogManager.getLogger(AddingApiEndpoint.class);

  private BookDatabaseMutator databaseMutator;

  public AddingApiEndpoint(BookDatabaseMutator databaseMutator) {
    this.databaseMutator = databaseMutator;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    if (exchange.isInIoThread()) {
      exchange.dispatch(this);
      return;
    }
    exchange.startBlocking();
    Optional<LoanableBook> bookOptional = DatabaseUtil.fromJson(exchange.getInputStream());

    if (!bookOptional.isPresent()) {
      HttpStatusSender.badRequest(exchange, "Invalid json received!");
      return;
    }

    LoanableBook book = bookOptional.get();

    try {
      databaseMutator.addBook(book);

      Exchange.body().sendJson(exchange, MapBuilder.of("acknowledged", true).build());
    } catch (DatabaseException e) {
      LOGGER.log(Level.WARN, "A database error occurred trying to store this book: " + book, e);

      HttpStatusSender.internalServerError(exchange);
    } catch (IllegalArgumentException ignored) {
      HttpStatusSender.badRequest(
          exchange,
          "You need to provide an ISBN."
      );
    }
  }
}
