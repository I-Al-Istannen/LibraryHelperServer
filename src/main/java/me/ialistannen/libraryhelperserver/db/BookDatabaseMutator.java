package me.ialistannen.libraryhelperserver.db;

import java.util.Collection;
import java.util.Collections;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.exceptions.DatabaseException;

/**
 * The interface to allow you mutating the database.
 */
public interface BookDatabaseMutator {

  /**
   * Adds a book to the database.
   *
   * @param book The book to add
   * @throws DatabaseException If an error occurs while adding it
   */
  default void addBook(LoanableBook book) {
    addBooks(Collections.singletonList(book));
  }

  /**
   * Adds multiple books to the database.
   *
   * @param books The books to add
   * @throws DatabaseException If an error occurs while adding them
   */
  void addBooks(Collection<LoanableBook> books);

  /**
   * Deletes the given book from the database, if it exists.
   *
   * @param book The book to delete.
   * @throws DatabaseException If an error occurs while deleting it
   */
  void deleteBook(LoanableBook book);

  /**
   * Deletes the given book from the database, if it exists.
   *
   * @param isbn The {@link Isbn} of the book to delete.
   * @throws DatabaseException If an error occurs while deleting it
   */
  void deleteBookByIsbn(Isbn isbn);

  /**
   * Deletes everything from the database.
   */
  void deleteAll();
}
