package me.ialistannen.libraryhelperserver.db.types.book.elastic.queries;

import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A query that peforms a MATCH query on a given field.
 */
public class QueryByMatch extends Query<List<LoanableBook>> {

  private final String query;
  private final String field;

  private QueryByMatch(String query, String field) {
    this.query = query;
    this.field = field;
  }

  @Override
  public List<LoanableBook> makeQuery(TransportClient client) {
    return hitsToBooks(
        search(client, QueryBuilders.matchQuery(
            field,
            query
        ))
    );
  }

  /**
   * @param query The query to execute
   * @param field The field to search in
   * @return The {@link QueryByMatch} for the passed query.
   */
  public static QueryByMatch forQuery(String query, String field) {
    return new QueryByMatch(query, field);
  }
}
