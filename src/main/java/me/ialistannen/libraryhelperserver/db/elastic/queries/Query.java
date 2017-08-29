package me.ialistannen.libraryhelperserver.db.elastic.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * A database query.
 */
public abstract class Query<T> {

  /**
   * Executes a query given a {@link TransportClient}.
   *
   * @param client The client to use for the query
   * @return The result of running it
   */
  // Taking a TransportClient breaks the abstraction, but I will be fine with that as I
  // have no clue how to change it for good
  public abstract T makeQuery(TransportClient client);

  /**
   * @param client The {@link TransportClient} to use
   * @param query The query to execute
   * @return The returned hits
   */
  protected SearchHits search(TransportClient client, QueryBuilder query) {
    SearchResponse searchResponse = client.prepareSearch(StringConstant.INDEX_NAME.getValue())
        .setTypes(StringConstant.TYPE_NAME.getValue())
        .setQuery(query)
        .get();

    DatabaseUtil.assertIsOkay(searchResponse, "Could not search. Status: '%s'");

    return searchResponse.getHits();
  }

  /**
   * @param hits The {@link SearchHits}
   * @return The corresponding {@link LoanableBook}s
   */
  protected List<LoanableBook> hitsToBooks(SearchHits hits) {
    if (hits.getTotalHits() < 1) {
      return Collections.emptyList();
    }

    List<LoanableBook> result = new ArrayList<>();

    for (SearchHit hit : hits.getHits()) {
      result.add(DatabaseUtil.searchHitToBook(hit));
    }

    return result;
  }
}
