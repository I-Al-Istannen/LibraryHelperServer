package me.ialistannen.libraryhelperserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticBookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticDatabaseCreator;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * The main file for the server.
 */
public class Server {

  public static void main(String[] args) throws UnknownHostException {
    TransportClient client = getClient();

    new ElasticDatabaseCreator().create(client.admin().indices());

    new ElasticBookDatabaseMutator(client).addBook(getLoanableBook());
  }

  private static TransportClient getClient() throws UnknownHostException {
    return new PreBuiltTransportClient(Settings.EMPTY)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300));
  }

  private static LoanableBook getLoanableBook() {
    LoanableBook book = new LoanableBook();
    book.setData(StandardBookDataKeys.TITLE, "Test");
    book.setData(
        StandardBookDataKeys.ISBN,
        new IsbnConverter().fromString("978-3791504544").get()
    );
    book.setData(
        StandardBookDataKeys.AUTHORS,
        Arrays.asList(
            new Pair<>("Cornelia Funke", "Author"),
            new Pair<>("Max Mustermann", "Übersetzer")
        )
    );
    return book;
  }
}
