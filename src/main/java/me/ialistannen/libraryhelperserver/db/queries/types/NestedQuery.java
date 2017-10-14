package me.ialistannen.libraryhelperserver.db.queries.types;

import me.ialistannen.libraryhelperserver.db.queries.BookQuery;
import me.ialistannen.libraryhelperserver.db.queries.QueryOptions;
import me.ialistannen.libraryhelperserver.db.queries.types.NestedQuery.NestedQueryOptions;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A nested query.
 */
public class NestedQuery extends BookQuery<NestedQueryOptions> {

  @Override
  public QueryBuilder getQueryBuilder(TransportClient client, NestedQueryOptions options) {
    return QueryBuilders.nestedQuery(
        options.getNestedFieldPath(),
        options.getQueryBuilder(client),
        ScoreMode.Avg
    );
  }

  public static class NestedQueryOptions<T extends QueryOptions> implements QueryOptions {

    private String nestedFieldPath;
    private BookQuery<T> otherQuery;
    private T otherQueryOptions;

    public NestedQueryOptions(String nestedFieldPath, BookQuery<T> otherQuery,
        T otherQueryOptions) {
      this.nestedFieldPath = nestedFieldPath;
      this.otherQuery = otherQuery;
      this.otherQueryOptions = otherQueryOptions;
    }

    public String getNestedFieldPath() {
      return nestedFieldPath;
    }

    public QueryBuilder getQueryBuilder(TransportClient client) {
      return otherQuery.getQueryBuilder(client, otherQueryOptions);
    }
  }
}
