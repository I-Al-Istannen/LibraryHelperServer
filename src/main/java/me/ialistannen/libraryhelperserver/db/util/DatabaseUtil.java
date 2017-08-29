package me.ialistannen.libraryhelperserver.db.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.InputStream;
import java.io.InputStreamReader;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.elastic.IntermediaryBook;
import me.ialistannen.libraryhelperserver.db.exceptions.DatabaseException;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;

/**
 * Contains common helper functions.
 */
public class DatabaseUtil {

  private static final Gson gson = IntermediaryBook.configureGson(new GsonBuilder()).create();

  /**
   * Asserts that the status is okay.
   *
   * @param response The response
   * @param message The message to print. Can contain `%s`, which will be replaced with the status
   * @throws DatabaseException if the status is not OK or CREATED
   */
  public static void assertIsOkay(StatusToXContentObject response, String message) {
    if (response.status() != RestStatus.OK && response.status() != RestStatus.CREATED) {
      throw new DatabaseException(String.format(message, response.status()));
    }
  }

  /**
   * Converts a {@link SearchHit} to a {@link LoanableBook}.
   *
   * @param searchHit The {@link SearchHit} to convert
   * @return The resulting {@link LoanableBook}
   */
  public static LoanableBook searchHitToBook(SearchHit searchHit) {
    String json = searchHit.getSourceAsString();

    return gson.fromJson(json, IntermediaryBook.class).toLoanableBook();
  }

  /**
   * @return The JSON representation of the book.
   */
  public static String toJson(LoanableBook book) {
    return gson.toJson(IntermediaryBook.fromLoanableBook(book));
  }

  /**
   * @return The parsed book
   */
  public static Optional<LoanableBook> fromJson(String json) {
    try {
      return Optional.of(
          gson.fromJson(json, IntermediaryBook.class).toLoanableBook()
      );
    } catch (JsonSyntaxException e) {
      return Optional.empty();
    }
  }

  /**
   * @return The parsed book
   */
  public static Optional<LoanableBook> fromJson(InputStream inputStream) {
    try {
      return Optional.of(
          gson.fromJson(new InputStreamReader(inputStream), IntermediaryBook.class)
              .toLoanableBook()
      );
    } catch (JsonSyntaxException e) {
      return Optional.empty();
    }
  }

  /**
   * @return The {@link Gson} instance able to serialize books
   */
  public static Gson getGson() {
    return gson;
  }
}