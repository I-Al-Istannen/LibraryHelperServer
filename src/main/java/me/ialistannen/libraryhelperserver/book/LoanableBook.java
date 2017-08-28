package me.ialistannen.libraryhelperserver.book;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import me.ialistannen.isbnlookuplib.book.Book;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;

/**
 * A {@link Book} that can be loaned.
 *
 * <p>All data differing from the {@link StandardBookDataKeys} must be a trivial type (including
 * strings)
 */
public class LoanableBook extends Book {

  private static final Set<BookDataKey> whitelistedKeys = new HashSet<BookDataKey>() {{
    Collections.addAll(this, StandardBookDataKeys.values());
    add(BorrowerKey.INSTANCE);
  }};

  @Override
  public void setData(BookDataKey key, Object value) {
    if (!whitelistedKeys.contains(key)) {
      assertIsTrivialValue(value);
    }

    super.setData(key, value);
  }

  private void assertIsTrivialValue(Object object) {
    if (object instanceof String || object instanceof Number) {
      return;
    }

    throw new IllegalArgumentException(
        "The type '" + object.getClass() + "' is not a trivial type!"
            + " Only those are allowed for custom keys."
    );
  }
}
