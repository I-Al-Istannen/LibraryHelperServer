package me.ialistannen.libraryhelperserver.book;

import me.ialistannen.isbnlookuplib.book.Book;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;

/**
 * A book that can be lend to others.
 */
public class LoanableBook extends Book implements Identifiable<String> {

  private static final BookDataKey IDENTIFIER_KEY = StandardBookDataKeys.ISBN_STRING;

  @Override
  public void setData(BookDataKey key, Object value) {
    super.setData(key, value);

    if (key == StandardBookDataKeys.ISBN && value instanceof Isbn) {
      Isbn isbn = getData(StandardBookDataKeys.ISBN);
      setData(IDENTIFIER_KEY, isbn.getDigitsAsString());
    }
  }

  @Override
  public String getKey() {
    return getData(IDENTIFIER_KEY);
  }
}
