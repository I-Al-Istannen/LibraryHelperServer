package me.ialistannen.libraryhelperserver.db;

import static me.ialistannen.libraryhelperserver.db.ElasticBookDatabaseCreator.INDEX_NAME;
import static me.ialistannen.libraryhelperserver.db.ElasticBookDatabaseCreator.RAW_MULTIFIELD_NAME;
import static me.ialistannen.libraryhelperserver.db.ElasticBookDatabaseCreator.TYPE_BOOK;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.book.StringBookDataKey;
import me.ialistannen.libraryhelperserver.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * A {@link BookDatabase} using Elasticsearch
 */
public class ElasticBookDatabase implements BookDatabase {

  private static final Logger LOGGER = LogManager.getLogger(ElasticBookDatabase.class);

  private static final String FIELD_ID_NAME = "ISBN_STRING";

  private Map<String, StandardBookDataKeys> keyLookupTable = Util
      .getEnumLookupTable(StandardBookDataKeys.class, Enum::name);

  private TransportClient client;
  private Gson gson;

  public ElasticBookDatabase(TransportClient transportClient) {
    this.client = transportClient;
    this.gson = new GsonBuilder().create();
  }

  @Override
  public Optional<LoanableBook> getBookByIsbn(Isbn isbn) {
    SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
        .setTypes(TYPE_BOOK)
        .setQuery(
            QueryBuilders.termQuery(
                FIELD_ID_NAME,
                isbn.getDigitsAsString()
            )
        )
        .get(); // blocks

    if (!isResponseOkay(searchResponse)) {
      return Optional.empty();
    }

    SearchHits hits = searchResponse.getHits();

    if (hits.getTotalHits() == 0) {
      return Optional.empty();
    } else if (hits.getTotalHits() > 1) {
      LOGGER
          .warn("Multiple books with ISBN " + isbn + " found: " + Arrays.toString(hits.getHits()));
      return Optional.empty();
    }

    SearchHit searchHit = hits.getHits()[0];

    return Optional.of(getLoanableBookFromSearchHit(searchHit));
  }

  private boolean isResponseOkay(StatusToXContentObject response) {
    if (response.status() != RestStatus.OK) {
      LOGGER.warn("Result not okay. Status: " + response.status());
      return false;
    }
    return true;
  }

  private LoanableBook getLoanableBookFromSearchHit(SearchHit searchHit) {
    LoanableBook book = new LoanableBook();

    @SuppressWarnings("unchecked")
    Map<String, Object> value = gson.fromJson(searchHit.getSourceAsString(), Map.class);

    for (Entry<String, Object> entry : value.entrySet()) {
      BookDataKey key = keyLookupTable.get(entry.getKey());

      if (key == null) {
        // This destroys the original class. I do not care...
        key = new StringBookDataKey(entry.getKey());
      }

      book.setData(key, entry.getValue());
    }
    return book;
  }

  @Override
  public List<LoanableBook> getBooksByFullTitleWithWildcards(String titleWithWildcards) {
    SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
        .setTypes(TYPE_BOOK)
        .setQuery(
            QueryBuilders.wildcardQuery(
                StandardBookDataKeys.TITLE.name() + "." + RAW_MULTIFIELD_NAME,
                titleWithWildcards.toLowerCase()
            )
        )
        .setSize(5_000) // TODO: 18.08.17 Better paging...
        .get();

    if (!isResponseOkay(searchResponse)) {
      return Collections.emptyList();
    }

    return getResponseAsBookList(searchResponse);
  }

  private List<LoanableBook> getResponseAsBookList(SearchResponse searchResponse) {
    List<LoanableBook> books = new ArrayList<>();

    for (SearchHit searchHitFields : searchResponse.getHits().getHits()) {
      books.add(getLoanableBookFromSearchHit(searchHitFields));
    }

    return books;
  }

  @Override
  public List<LoanableBook> getBooksByTitleRegex(String regex) {
    SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
        .setTypes(TYPE_BOOK)
        .setQuery(
            QueryBuilders.regexpQuery(
                StandardBookDataKeys.TITLE.name() + "." + RAW_MULTIFIELD_NAME,
                regex
            )
        )
        .setSize(5_000) // TODO: 18.08.17 Better paging...
        .get();

    if (!isResponseOkay(searchResponse)) {
      return Collections.emptyList();
    }

    return getResponseAsBookList(searchResponse);
  }


  @Override
  public List<LoanableBook> getBookByAuthor(String authorWithWildcards) {
    SearchResponse searchResponse = client.prepareSearch(INDEX_NAME)
        .setTypes(TYPE_BOOK)
        .setQuery(
            QueryBuilders.nestedQuery(
                StandardBookDataKeys.AUTHORS.name(),
                QueryBuilders.wildcardQuery(
                    StandardBookDataKeys.AUTHORS.name() + ".key." + RAW_MULTIFIELD_NAME,
                    authorWithWildcards.toLowerCase()
                ),
                ScoreMode.Max
            )
        )
        .setSize(5_000) // TODO: Better pagination
        .get();

    if (!isResponseOkay(searchResponse)) {
      return Collections.emptyList();
    }

    return getResponseAsBookList(searchResponse);
  }

  @Override
  public void storeBook(LoanableBook book) {
    String json = gson.toJson(book.getAllData());

    IndexResponse indexResponse = client
        .prepareIndex(INDEX_NAME, TYPE_BOOK, book.getKey())
        .setSource(json, XContentType.JSON)
        .get();

    if (indexResponse.status() != RestStatus.CREATED && indexResponse.status() != RestStatus.OK) {
      LOGGER.warn(
          "Error storing a book with key " + book.getKey()
              + " (status " + indexResponse.status() + ")");
    }
  }

  @Override
  public void deleteBook(Isbn isbn) {
    DeleteResponse deleteResponse = client
        .prepareDelete(INDEX_NAME, TYPE_BOOK, isbn.getDigitsAsString())
        .get();

    isResponseOkay(deleteResponse);
  }

  @Override
  public void lendBook(Isbn isbn, String lender) {

  }

  @Override
  public void gotLendBookBack(Isbn isbn) {

  }
}
