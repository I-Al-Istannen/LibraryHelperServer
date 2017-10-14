package me.ialistannen.libraryhelperserver.db.queries;

import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.queries.QueryOptions.BasicQueryOptions;
import me.ialistannen.libraryhelperserver.db.queries.types.NestedQuery;
import me.ialistannen.libraryhelperserver.db.queries.types.NestedQuery.NestedQueryOptions;
import me.ialistannen.libraryhelperserver.model.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;

/**
 * The different query fields that exist.
 */
public enum QueryField {
  AUTHOR("authors", "authors.key", true),
  TITLE("title", true),
  ISBN("isbn", true);

  private String fieldName;
  private String nestedFieldPath;
  private boolean nested;
  private boolean hasRawSubField;

  QueryField(String fieldName, boolean hasRawSubField) {
    this.fieldName = fieldName;
    this.hasRawSubField = hasRawSubField;
  }

  QueryField(String fieldName, String nestedFieldPath, boolean hasRawSubField) {
    this(fieldName, hasRawSubField);
    this.nestedFieldPath = nestedFieldPath;
    this.nested = true;
  }

  /**
   * @return True if this query field has a raw field that can be used for Regex/Wildcard queries
   */
  public boolean hasRawSubField() {
    return hasRawSubField;
  }


  /**
   * @param query The {@link Query} to perform
   * @param searchType The {@link SearchType}
   * @param client The {@link TransportClient} to use
   * @return The result of the query
   */
  public List<LoanableBook> performQuery(String query, SearchType searchType,
      TransportClient client) {
    BookQuery<QueryOptions> bookQuery = searchType.getQuery();
    QueryOptions queryOptions = getQueryOptions(query, bookQuery);
    bookQuery = getAdjustedQuery(bookQuery);

    return bookQuery.performQuery(client, queryOptions);
  }

  /**
   * <em><strong>Must be called before {@link #getAdjustedQuery(BookQuery)}</strong></em>
   *
   * @param query The query string
   * @param bookQuery The query to use
   * @param <T> The type it returns. I want specialized enum entries :(
   * @return The query options to use with this field
   */
  private <T extends QueryOptions> T getQueryOptions(String query, BookQuery<T> bookQuery) {
    if (nested) {
      return getNestedQueryOptions(query, bookQuery);
    }

    @SuppressWarnings("unchecked")
    T queryOptions = (T) new BasicQueryOptions(query, fieldName);
    return queryOptions;
  }

  private <T extends QueryOptions> BookQuery<T> getAdjustedQuery(BookQuery<T> base) {
    if (nested) {
      return getNestedAdjustedQuery();
    }
    return base;
  }

  @SuppressWarnings("unchecked")
  private <T extends QueryOptions> T getNestedQueryOptions(String query, BookQuery<T> bookQuery) {
    T otherOptions = (T) new BasicQueryOptions(query, nestedFieldPath);

    return (T) new NestedQueryOptions<>(fieldName, bookQuery, otherOptions);
  }

  private <T extends QueryOptions> BookQuery<T> getNestedAdjustedQuery() {
    @SuppressWarnings("unchecked")
    BookQuery<T> tBookQuery = (BookQuery<T>) new NestedQuery();
    return tBookQuery;
  }
}
