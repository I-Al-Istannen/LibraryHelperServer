package me.ialistannen.libraryhelperserver.db.types.book;

import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.queries.QueryField;
import me.ialistannen.libraryhelperserver.model.search.SearchType;

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
   * @param query The query string
   * @param searchType The {@link SearchType}
   * @param field The {@link QueryField} to query
   * @return Everything that matched the query
   */
  List<LoanableBook> getForQuery(SearchType searchType, QueryField field, String query);
}
