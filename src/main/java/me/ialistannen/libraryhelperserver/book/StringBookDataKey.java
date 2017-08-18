package me.ialistannen.libraryhelperserver.book;

import me.ialistannen.isbnlookuplib.book.AbstractBookDataKey;
import me.ialistannen.isbnlookuplib.book.BookDataKey;

/**
 * A simple {@link BookDataKey} that is just a String.
 */
public class StringBookDataKey extends AbstractBookDataKey {

  private final String name;

  public StringBookDataKey(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }
}
