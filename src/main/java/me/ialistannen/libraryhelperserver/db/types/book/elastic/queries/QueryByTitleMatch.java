package me.ialistannen.libraryhelperserver.db.types.book.elastic.queries;

import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Queries by
 */
public class QueryByTitleMatch extends Query<List<LoanableBook>> {

  private final String query;

  private QueryByTitleMatch(String query) {
    this.query = query;
  }

  @Override
  public List<LoanableBook> makeQuery(TransportClient client) {
    return hitsToBooks(
        search(client, QueryBuilders.matchQuery(
            "title",
            query
        ))
    );
  }

  /**
   * @param query The query to execute
   * @return The {@link QueryByTitleMatch} for the passed query.
   */
  public static QueryByTitleMatch forQuery(String query) {
    return new QueryByTitleMatch(query);
  }
}
