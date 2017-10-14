package me.ialistannen.libraryhelperserver.db.queries.types;

import me.ialistannen.libraryhelperserver.db.queries.BookQuery;
import me.ialistannen.libraryhelperserver.db.queries.QueryOptions.BasicQueryOptions;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A query that does partly fuzzy matches.
 */
public class FuzzyQuery extends BookQuery<BasicQueryOptions> {

  @Override
  public QueryBuilder getQueryBuilder(TransportClient client, BasicQueryOptions options) {
    return QueryBuilders.matchQuery(options.getFieldName(), options.getQueryString());
  }
}
