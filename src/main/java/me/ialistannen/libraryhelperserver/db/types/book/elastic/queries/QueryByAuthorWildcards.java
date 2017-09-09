package me.ialistannen.libraryhelperserver.db.types.book.elastic.queries;

import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;

/**
 * A {@link Query} searching by the author's name.
 */
public class QueryByAuthorWildcards extends Query<List<LoanableBook>> {

  private final String query;

  private QueryByAuthorWildcards(String query) {
    this.query = query;
  }

  @Override
  public List<LoanableBook> makeQuery(TransportClient client) {
    WildcardQueryBuilder query = QueryBuilders.wildcardQuery(
        "authors.key",
        this.query.toLowerCase()
    );
    return hitsToBooks(
        search(client, QueryBuilders.nestedQuery(
            "authors",
            query,
            ScoreMode.Avg
        ))
    );
  }

  /**
   * @param titleQuery The wildcarded query to use
   * @return A query to search
   */
  public static QueryByAuthorWildcards forQuery(String titleQuery) {
    return new QueryByAuthorWildcards(titleQuery);
  }
}
