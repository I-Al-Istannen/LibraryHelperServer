package me.ialistannen.libraryhelperserver.db.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.creation.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * A query for books.
 */
public abstract class BookQuery<T extends QueryOptions> implements Query<List<LoanableBook>, T> {

  @Override
  public List<LoanableBook> performQuery(TransportClient client, T options) {
    return hitsToBooks(search(
        client,
        getQueryBuilder(client, options)
    ));
  }

  /**
   * @param client The {@link TransportClient} to use
   * @param query The query to execute
   * @return The returned hits
   */
  protected SearchHits search(TransportClient client, QueryBuilder query) {
    SearchResponse searchResponse = client.prepareSearch(StringConstant.BOOK_INDEX_NAME.getValue())
        .setTypes(StringConstant.BOOK_TYPE_NAME.getValue())
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

  /**
   * Returns the {@link QueryBuilder} this query would use.
   *
   * @param client The {@link TransportClient} to use
   * @param options The query options
   * @return The {@link QueryBuilder} it would use
   */
  public abstract QueryBuilder getQueryBuilder(TransportClient client, T options);
}
