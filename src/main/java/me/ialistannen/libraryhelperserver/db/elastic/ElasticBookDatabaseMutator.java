package me.ialistannen.libraryhelperserver.db.elastic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.book.StringBookDataKey;
import me.ialistannen.libraryhelperserver.db.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.exceptions.DatabaseException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

/**
 * A {@link BookDatabaseMutator} for the Elasticsearch database
 */
public class ElasticBookDatabaseMutator implements BookDatabaseMutator {

  private TransportClient client;
  private Gson gson;

  public ElasticBookDatabaseMutator(TransportClient client) {
    this.client = client;

    this.gson = IntermediaryBook.configureGson(new GsonBuilder())

        .create();
  }

  @Override
  public void addBooks(Collection<LoanableBook> books) {
    for (LoanableBook book : books) {
      book.setData(new StringBookDataKey("A_- Test"), "HOPEFULLY");
      book.setData(new StringBookDataKey("A_- Test-Double"), 2.09);

      System.out.printf("%-30s | %-30s | %s\n", "Original: ", md5(book.toString()), book);
      System.out.println();

      System.out.printf("%-30s | %-30s | %s\n", "Serialized original: ", md5(gson.toJson(book)),
          gson.toJson(book));
      String json = gson.toJson(IntermediaryBook.fromLoanableBook(book));
      System.out.printf("%-30s | %-30s | %s\n", "Serialized intermediary: ", md5(json), json);

      System.out.println();
      IntermediaryBook intermediaryBook = gson.fromJson(json, IntermediaryBook.class);
      System.out.printf("%-30s | %-30s | %s\n", "Deserialized intermediary: ",
          md5(intermediaryBook.toString()), intermediaryBook);
      System.out.printf("%-30s | %-30s | %s\n", "Deserialized original: ",
          md5(intermediaryBook.toLoanableBook().toString()), intermediaryBook.toLoanableBook());

      IndexResponse indexResponse = client.prepareIndex(
          StringConstant.INDEX_NAME.getValue(),
          StringConstant.TYPE_NAME.getValue(),
          intermediaryBook.isbn.getDigitsAsString()
      )
          .setSource(json, XContentType.JSON)
          .get();

      assertIsOkay(indexResponse, "Could not index a book: " + book);
    }
  }

  private void assertIsOkay(IndexResponse indexResponse, String message) {
    if (indexResponse.status() != RestStatus.OK && indexResponse.status() != RestStatus.CREATED) {
      throw new DatabaseException(message);
    }
  }

  private String md5(String input) {
    try {
      return Base64.getMimeEncoder()
          .encodeToString(MessageDigest.getInstance("md5").digest(input.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return "ERROR";
    }
  }

  @Override
  public void deleteBook(LoanableBook book) {

  }

  @Override
  public void deleteBookByIsbn(Isbn isbn) {

  }

  @Override
  public void deleteAll() {

  }

}
