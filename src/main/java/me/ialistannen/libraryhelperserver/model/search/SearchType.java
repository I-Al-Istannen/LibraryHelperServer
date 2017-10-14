package me.ialistannen.libraryhelperserver.model.search;

import me.ialistannen.libraryhelperserver.db.queries.BookQuery;
import me.ialistannen.libraryhelperserver.db.queries.QueryOptions;
import me.ialistannen.libraryhelperserver.db.queries.types.ExactMatchQuery;
import me.ialistannen.libraryhelperserver.db.queries.types.FuzzyQuery;
import me.ialistannen.libraryhelperserver.db.queries.types.RegexQuery;
import me.ialistannen.libraryhelperserver.db.queries.types.WildcardQuery;
import me.ialistannen.libraryhelperserver.util.CachingSupplier;

/**
 * The possible search types
 */
public enum SearchType {

  /**
   * A wildcarded query, case insensitive
   */
  WILDCARD(new CachingSupplier<>(WildcardQuery::new)),
  /**
   * A query using a regular expression
   */
  REGEX(new CachingSupplier<>(RegexQuery::new)),
  /**
   * A match that searches for a phrase, but does not require it to match perfectly.
   */
  FUZZY(new CachingSupplier<>(FuzzyQuery::new)),
  /**
   * Exactly what was entered
   */
  EXACT_MATCH(new CachingSupplier<>(ExactMatchQuery::new));


  private CachingSupplier<BookQuery<?>> querySupplier;

  SearchType(CachingSupplier<BookQuery<?>> querySupplier) {
    this.querySupplier = querySupplier;
  }

  public <T extends QueryOptions> BookQuery<T> getQuery() {
    @SuppressWarnings("unchecked")
    BookQuery<T> t = (BookQuery<T>) querySupplier.get();
    return t;
  }
}
