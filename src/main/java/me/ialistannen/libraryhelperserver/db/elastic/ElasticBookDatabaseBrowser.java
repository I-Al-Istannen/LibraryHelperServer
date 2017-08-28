package me.ialistannen.libraryhelperserver.db.elastic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.exceptions.DatabaseException;
import me.ialistannen.libraryhelperserver.db.queries.Query;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * An implementation of the {@link BookDatabaseBrowser} using Elasticsearch.
 */
public class ElasticBookDatabaseBrowser implements BookDatabaseBrowser {

  private final TransportClient client;
  private final Gson gson;

  public ElasticBookDatabaseBrowser(TransportClient client) {
    this.client = client;

    this.gson = IntermediaryBook.configureGson(new GsonBuilder())
        .create();
  }

  @Override
  public List<LoanableBook> getAllBooksLimited() {
    SearchResponse searchResponse = client.prepareSearch(StringConstant.INDEX_NAME.getValue())
        .setTypes(StringConstant.TYPE_NAME.getValue())
        .setQuery(QueryBuilders.matchAllQuery())
        .setSize(100)
        .get();

    assertIsOkay(searchResponse, "Was not able to return all books. Status: '%s'.");

    SearchHits hits = searchResponse.getHits();

    return Arrays.stream(hits.getHits())
        .map(this::searchHitToBook)
        .collect(Collectors.toList());
  }

  private LoanableBook searchHitToBook(SearchHit searchHit) {
    String json = searchHit.getSourceAsString();

    return gson.fromJson(json, IntermediaryBook.class).toLoanableBook();
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
      assertIsOkay(searchResponse, "Search failed. Status: '%s'");

      for (SearchHit hit : searchResponse.getHits().getHits()) {
        results.add(searchHitToBook(hit));
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

  private void assertIsOkay(StatusToXContentObject response, String message) {
    if (response.status() != RestStatus.OK && response.status() != RestStatus.CREATED) {
      throw new DatabaseException(String.format(message, response.status()));
    }
  }
}
