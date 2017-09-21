package me.ialistannen.libraryhelperserver.db.types.book.elastic.queries;

import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A wildcarded query on a given field.
 */
public class QueryByWildcards extends Query<List<LoanableBook>> {

  private final String query;
  private final String field;

  private QueryByWildcards(String query, String field) {
    this.query = query.toLowerCase();
    this.field = field;
  }

  @Override
  public List<LoanableBook> makeQuery(TransportClient client) {
    return hitsToBooks(
        search(client, QueryBuilders.wildcardQuery(
            field,
            query
        ))
    );
  }

  /**
   * @param query The wildcarded query to use
   * @param field The field to search in
   * @return A query to search that
   */
  public static QueryByWildcards forQuery(String query, String field) {
    return new QueryByWildcards(query, field);
  }
}