package me.ialistannen.libraryhelperserver.db.elastic.queries;

import java.util.List;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A {@link Query} that searches the title using wildcards.
 */
public class QueryByTitleWildcards extends Query<List<LoanableBook>> {

  private final String query;

  private QueryByTitleWildcards(String query) {
    this.query = query.toLowerCase();
  }

  @Override
  public List<LoanableBook> makeQuery(TransportClient client) {
    return hitsToBooks(
        search(client, QueryBuilders.wildcardQuery(
            "title.raw",
            query
        ))
    );
  }

  /**
   * @param titleQuery The wildcarded query to use
   * @return A query to search that
   */
  public static QueryByTitleWildcards forQuery(String titleQuery) {
    return new QueryByTitleWildcards(titleQuery);
  }
}
