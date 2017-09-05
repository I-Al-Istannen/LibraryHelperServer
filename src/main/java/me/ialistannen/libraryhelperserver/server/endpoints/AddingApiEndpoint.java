package me.ialistannen.libraryhelperserver.server.endpoints;

import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import me.ialistannen.isbnlookuplib.book.Book;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.lookup.IsbnLookupProvider;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.exceptions.DatabaseException;
import me.ialistannen.libraryhelperserver.server.utilities.Exchange;
import me.ialistannen.libraryhelperserver.server.utilities.HttpStatusSender;
import me.ialistannen.libraryhelperserver.util.Configs;
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
  private IsbnConverter isbnConverter;
  private IsbnLookupProvider isbnLookupProvider;

  public AddingApiEndpoint(BookDatabaseMutator databaseMutator, IsbnConverter isbnConverter,
      IsbnLookupProvider isbnLookupProvider) {
    this.databaseMutator = databaseMutator;
    this.isbnConverter = isbnConverter;
    this.isbnLookupProvider = isbnLookupProvider;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    JsonObject jsonObject = Exchange.body().readTree(exchange);

    if (jsonObject == null || !jsonObject.has("isbn")) {
      HttpStatusSender.badRequest(exchange, "Invalid json received!");
      return;
    }

    String isbnString = jsonObject.getAsJsonPrimitive("isbn").getAsString();

    Optional<Isbn> isbnOptional = isbnConverter.fromString(isbnString);

    if (!isbnOptional.isPresent()) {
      HttpStatusSender.badRequest(exchange, "Invalid ISBN!");
      return;
    }

    Isbn isbn = isbnOptional.get();

    Optional<Book> bookOptional = isbnLookupProvider.lookup(isbn);

    if (!bookOptional.isPresent()) {
      Exchange.body().sendJson(exchange, MapBuilder.of("message", "Isbn lookup failed.").build());
      return;
    }

    LoanableBook book = new LoanableBook(bookOptional.get());

    try {
      databaseMutator.addBook(book);
      LOGGER.info("Added book with ISBN " + book.getAllData().get(StandardBookDataKeys.ISBN));

      if (book.getData(StandardBookDataKeys.COVER_IMAGE_URL) != null) {
        downloadImageToCovers(
            book.getData(StandardBookDataKeys.COVER_IMAGE_URL), isbn.getDigitsAsString()
        );
      }

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

  private void downloadImageToCovers(String urlString, String isbn) {
    try {
      Path targetPath = Configs.getCustomAsPath("assets.basepath")
          .resolve("covers")
          .resolve(isbn + ".jpg");

      // Unlikely it has changed
      if (Files.exists(targetPath)) {
        return;
      }

      URL url = new URL(urlString);
      HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.addRequestProperty(Headers.USER_AGENT_STRING, "Mozilla/5.0");

      try (InputStream inputStream = httpURLConnection.getInputStream()) {
        Files.copy(inputStream, targetPath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARN, "Error downloading from '" + urlString + "'", e);
    }
  }
}
