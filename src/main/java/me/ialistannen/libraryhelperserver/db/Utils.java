package me.ialistannen.libraryhelperserver.db;

import me.ialistannen.isbnlookuplib.book.BookDataKey;

/**
 * Some utility methods
 */
class Utils {

  /**
   * @param key The {@link BookDataKey}
   * @return The name of the field for the {@link BookDataKey}
   */
  static String getBookDataKeyAsFieldName(BookDataKey key) {
    return key.toString().toLowerCase();
  }


}
