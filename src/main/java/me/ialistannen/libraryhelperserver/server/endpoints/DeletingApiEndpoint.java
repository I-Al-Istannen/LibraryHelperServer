package me.ialistannen.libraryhelperserver.server.endpoints;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Function;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.util.exceptions.DatabaseException;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.Configs;
import me.ialistannen.libraryhelperserver.util.MapBuilder;
import me.ialistannen.libraryhelperserver.util.OptionalConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The endpoint for deleting books.
 */
public class DeletingApiEndpoint implements HttpHandler {

  private static final Logger LOGGER = LogManager.getLogger(DeletingApiEndpoint.class);

  private IsbnConverter isbnConverter;
  private BookDatabaseMutator databaseMutator;

  public DeletingApiEndpoint(IsbnConverter isbnConverter, BookDatabaseMutator databaseMutator) {
    this.isbnConverter = isbnConverter;
    this.databaseMutator = databaseMutator;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    Optional<Isbn> isbn = Exchange.queryParams().getTransformed(exchange, "isbn", toIsbn());

    if (!isbn.isPresent()) {
      HttpStatusSender.badRequest(exchange, "You need to provide a valid 'isbn' parameter.");
      return;
    }

    try {
      boolean value = databaseMutator.deleteBookByIsbn(isbn.get());
      Exchange.body().sendJson(exchange,
          MapBuilder.of("deleted", value).build()
      );
      if (value) {
        deleteCover(isbn.get());
      }
    } catch (DatabaseException e) {
      LOGGER.log(
          Level.WARN,
          "An error occurred while deleting a book with the isbn '" + isbn + "'",
          e
      );
      HttpStatusSender.internalServerError(exchange);
    }
  }

  private Function<Deque<String>, Optional<Isbn>> toIsbn() {
    return strings -> {
      if (strings.isEmpty()) {
        return Optional.empty();
      }
      return OptionalConverter.toJDK(isbnConverter.fromString(strings.getFirst()));
    };
  }

  private void deleteCover(Isbn isbn) {
    Path targetPath = Configs.getCustomAsPath("assets.basepath")
        .resolve("covers")
        .resolve(isbn.getDigitsAsString());

    try {
      Files.deleteIfExists(targetPath);
    } catch (IOException e) {
      LOGGER.log(Level.WARN, "Could not delete cover '" + isbn.getDigitsAsString() + "'", e);
    }
  }
}
