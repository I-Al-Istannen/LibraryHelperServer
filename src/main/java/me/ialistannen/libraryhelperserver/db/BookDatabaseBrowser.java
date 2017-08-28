package me.ialistannen.libraryhelperserver.db;

import java.util.List;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.elastic.queries.Query;

/**
 * The interface to allow you browsing the books.
 */
public interface BookDatabaseBrowser {

  /**
   * Returns all books in the database.
   *
   * <p><b>The amount may be limited by the database.</b>
   *
   * @return All the books in the database
   */
  List<LoanableBook> getAllBooksLimited();

  /**
   * Returns all books in the database.
   *
   * <p><b>The amount will <u>not</u> be limited by the database. If needed multiple queries are
   * sent.</b>
   *
   * @return All the books in the database
   */
  List<LoanableBook> getAllBooksFully();

  /**
   * Executes a query and returns the result.
   *
   * @param query The {@link Query} to execute
   * @param <T> The return type of the query
   * @return Everything that matched the query
   */
  <T> T getForQuery(Query<T> query);
}
