package me.ialistannen.libraryhelperserver.db.queries;

import org.elasticsearch.client.transport.TransportClient;

/**
 * A database query.
 */
public interface Query<T> {

  /**
   * Executes a query given a {@link TransportClient}.
   *
   * @param client The client to use for the query
   * @return The result of running it
   */
  T makeQuery(TransportClient client);
}
