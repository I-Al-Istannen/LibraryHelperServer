package me.ialistannen.libraryhelperserver.db.elastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.elastic.queries.Query;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * An implementation of the {@link BookDatabaseBrowser} using Elasticsearch.
 */
public class ElasticBookDatabaseBrowser implements BookDatabaseBrowser {

  private final TransportClient client;

  public ElasticBookDatabaseBrowser(TransportClient client) {
    this.client = client;
  }

  @Override
  public List<LoanableBook> getAllBooksLimited() {
    SearchResponse searchResponse = client.prepareSearch(StringConstant.INDEX_NAME.getValue())
        .setTypes(StringConstant.TYPE_NAME.getValue())
        .setQuery(QueryBuilders.matchAllQuery())
        .setSize(100)
        .get();

    DatabaseUtil.assertIsOkay(searchResponse, "Was not able to return all books. Status: '%s'.");

    SearchHits hits = searchResponse.getHits();

    return Arrays.stream(hits.getHits())
        .map(DatabaseUtil::searchHitToBook)
        .collect(Collectors.toList());
  }


  @Override
  public List<LoanableBook> getAllBooksFully() {
    SearchResponse searchResponse = client.prepareSearch(StringConstant.INDEX_NAME.getValue())
        .setTypes(StringConstant.TYPE_NAME.getValue())
        .setQuery(QueryBuilders.matchAllQuery())
        .setScroll(TimeValue.timeValueMinutes(1))
        .setSize(100)
        .get();

    List<LoanableBook> results = new ArrayList<>();
    do {
      DatabaseUtil.assertIsOkay(searchResponse, "Search failed. Status: '%s'");

      for (SearchHit hit : searchResponse.getHits().getHits()) {
        results.add(DatabaseUtil.searchHitToBook(hit));
      }

      searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
          .setScroll(TimeValue.timeValueMinutes(1))
          .get();

    } while (searchResponse.getHits().getHits().length > 0);

    return results;
  }

  @Override
  public <T> T getForQuery(Query<T> query) {
    return query.makeQuery(client);
  }
}
