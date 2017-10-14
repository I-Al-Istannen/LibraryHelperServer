package me.ialistannen.libraryhelperserver.db.queries;

import org.elasticsearch.client.transport.TransportClient;

/**
 * A query for the database.
 *
 * @param <R> the return type
 */
public interface Query<R, T extends QueryOptions> {

  /**
   * Performs the query.
   *
   * @param client The client to use for searching
   * @param options The {@link QueryOptions} to use
   * @return The result of the query
   */
  R performQuery(TransportClient client, T options);
}
