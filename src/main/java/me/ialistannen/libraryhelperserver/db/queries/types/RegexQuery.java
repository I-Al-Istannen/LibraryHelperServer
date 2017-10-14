package me.ialistannen.libraryhelperserver.db.queries.types;

import me.ialistannen.libraryhelperserver.db.queries.BookQuery;
import me.ialistannen.libraryhelperserver.db.queries.QueryOptions.BasicQueryOptions;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A query using regular expressions.
 */
public class RegexQuery extends BookQuery<BasicQueryOptions> {

  @Override
  public QueryBuilder getQueryBuilder(TransportClient client, BasicQueryOptions options) {
    return QueryBuilders.regexpQuery(
        options.getRawFieldName(), options.getQueryString().toLowerCase()
    );
  }
}
