package me.ialistannen.libraryhelperserver.db.types.book.elastic;

import java.util.Collection;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.creation.elastic.ElasticDatabaseCreator;
import me.ialistannen.libraryhelperserver.db.creation.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import me.ialistannen.libraryhelperserver.db.util.exceptions.DatabaseException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * A {@link BookDatabaseMutator} for the Elasticsearch database.
 */
public class ElasticBookDatabaseMutator implements BookDatabaseMutator {

  private TransportClient client;

  public ElasticBookDatabaseMutator(TransportClient client) {
    this.client = client;
  }

  @Override
  public void addBooks(Collection<LoanableBook> books) {
    for (LoanableBook book : books) {
      Isbn isbn = ensureGetIsbnFromBook(book);

      String json = DatabaseUtil.toJson(book);
      IndexResponse indexResponse = client.prepareIndex(
          StringConstant.INDEX_NAME.getValue(),
          StringConstant.TYPE_NAME.getValue(),
          isbn.getDigitsAsString()
      )
          .setSource(json, XContentType.JSON)
          .get();

      DatabaseUtil
          .assertIsOkay(indexResponse, "Could not index a book: " + book + ", response was '%s.'");
    }
  }

  private Isbn ensureGetIsbnFromBook(LoanableBook book) {
    Isbn isbn = book.getData(StandardBookDataKeys.ISBN);

    if (isbn == null) {
      throw new IllegalArgumentException("The book needs to have an ISBN: " + book);
    }

    return isbn;
  }

  @Override
  public boolean deleteBook(LoanableBook book) {
    return deleteBookByIsbn(ensureGetIsbnFromBook(book));
  }

  @Override
  public boolean deleteBookByIsbn(Isbn isbn) {
    DeleteResponse deleteResponse = client.prepareDelete(
        StringConstant.INDEX_NAME.getValue(),
        StringConstant.TYPE_NAME.getValue(),
        isbn.getDigitsAsString()
    ).get();

    if (deleteResponse.getResult() == Result.NOT_FOUND) {
      return false;
    }

    DatabaseUtil.assertIsOkay(deleteResponse,
        "Could not delete the book with ISBN: " + isbn + ". Status: '%s'.");

    return deleteResponse.getResult() == Result.DELETED;
  }

  @Override
  public void deleteAll() {
    DeleteIndexResponse deleteResponse = client.admin().indices()
        .prepareDelete(StringConstant.INDEX_NAME.getValue())
        .get();

    if (!deleteResponse.isAcknowledged()) {
      throw new DatabaseException("Could not clear index: Not acknowledged");
    }

    new ElasticDatabaseCreator().create(client.admin().indices());
  }
}
