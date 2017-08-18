package me.ialistannen.libraryhelperserver.db;

import java.util.List;
import java.util.Optional;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelperserver.book.LoanableBook;

/**
 * A book database.
 */
public interface BookDatabase {

  /**
   * Returns the book matching the isbn from the database.
   *
   * @param isbn The {@link Isbn} of the book
   * @return The book, if any
   */
  Optional<LoanableBook> getBookByIsbn(Isbn isbn);

  /**
   * Searches the database for books with the given title.
   *
   * @param titleWithWildcards The title to search. Can contain wildcards (? and *).
   * @return The books that match the search
   */
  List<LoanableBook> getBooksByFullTitleWithWildcards(String titleWithWildcards);

  /**
   * Searches the database for books with the given title.
   *
   * @param regex The regular expression to use
   * @return The books that match the criteria
   */
  List<LoanableBook> getBooksByTitleRegex(String regex);

  /**
   * Searches the database for books by the given author.
   *
   * @param authorWithWildcards The author name with wildcards
   * @return The books matching the criteria
   */
  List<LoanableBook> getBookByAuthor(String authorWithWildcards);

  /**
   * Stores a book in the database.
   *
   * @param book The {@link LoanableBook} to store.
   */
  void storeBook(LoanableBook book);

  /**
   * Deletes the given book.
   *
   * @param isbn The {@link Isbn} of the book to delete
   */
  void deleteBook(Isbn isbn);

  /**
   * Lends the book to a person.
   *
   * @param isbn The {@link Isbn} of the book
   * @param lender The person you lend it to
   */
  void lendBook(Isbn isbn, String lender);

  /**
   * Marks the book as back, after it was lend to somebody.
   *
   * @param isbn The {@link Isbn} of the book
   */
  // TODO: 16.08.17 Better name...
  void gotLendBookBack(Isbn isbn);
}
