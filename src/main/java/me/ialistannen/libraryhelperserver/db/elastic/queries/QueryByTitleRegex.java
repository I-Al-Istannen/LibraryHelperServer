package me.ialistannen.libraryhelperserver.db.elastic.queries;

import java.util.List;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * A {@link Query} that searches the title using regex.
 */
public class QueryByTitleRegex extends Query<List<LoanableBook>> {

  private final String query;

  private QueryByTitleRegex(String query) {
    this.query = query;
  }

  @Override
  public List<LoanableBook> makeQuery(TransportClient client) {
    return hitsToBooks(
        search(client, QueryBuilders.regexpQuery(
            "title.raw",
            query
        ))
    );
  }

  /**
   * Seaches by a regular expression.
   *
   * <p><b>Note that the regex is <u>case sensitive</u> and all data is stored in
   * <u>lowercase</u>.</b>
   *
   * @param regex The regular expression to use
   * @return The created query
   */
  public static QueryByTitleRegex forRegex(String regex) {
    return new QueryByTitleRegex(regex);
  }
}
