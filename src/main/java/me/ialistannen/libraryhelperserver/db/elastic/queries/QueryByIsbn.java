package me.ialistannen.libraryhelperserver.db.elastic.queries;

import java.util.List;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

/**
 * A {@link Query} that returns a book with the given ISBN.
 */
public class QueryByIsbn extends Query<Optional<LoanableBook>> {

  private Isbn isbn;

  private QueryByIsbn(Isbn isbn) {
    this.isbn = isbn;
  }

  @Override
  public Optional<LoanableBook> makeQuery(TransportClient client) {
    SearchHits searchResponse = search(client, QueryBuilders.termQuery(
        "isbn",
        this.isbn.getDigitsAsString()
    ));

    List<LoanableBook> books = hitsToBooks(searchResponse);

    if (books.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(books.get(0));
  }

  /**
   * @param isbn The ISBN to search for
   * @return An instance of the query
   */
  public static QueryByIsbn forIsbn(Isbn isbn) {
    return new QueryByIsbn(isbn);
  }
}
