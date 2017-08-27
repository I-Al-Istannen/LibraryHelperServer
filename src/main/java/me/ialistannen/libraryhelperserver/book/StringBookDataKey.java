package me.ialistannen.libraryhelperserver.book;

import java.util.Objects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StringBookDataKey that = (StringBookDataKey) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
