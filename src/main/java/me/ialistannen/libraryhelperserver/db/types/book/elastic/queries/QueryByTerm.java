package me.ialistannen.libraryhelperserver.db.types.book.elastic.queries;

import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A query to search by term
 */
public class QueryByTerm extends Query<List<LoanableBook>> {

  private final String query;
  private final String field;

  private QueryByTerm(String query, String field) {
    this.query = query;
    this.field = field;
  }

  @Override
  public List<LoanableBook> makeQuery(TransportClient client) {
    return hitsToBooks(
        search(client, QueryBuilders.termQuery(
            field,
            query
        ))
    );
  }

  /**
   * @param query The term query to use
   * @param field The field to search in
   * @return A query to search that
   */
  public static QueryByTerm forQuery(String query, String field) {
    return new QueryByTerm(query, field);
  }
}
