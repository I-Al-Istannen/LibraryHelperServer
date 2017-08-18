package me.ialistannen.libraryhelperserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.ElasticBookDatabase;
import me.ialistannen.libraryhelperserver.db.ElasticBookDatabaseCreator;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * The main class for the server.
 */
public class Server {

  public static void main(String[] args) throws UnknownHostException, InterruptedException {
    TransportClient transportClient = new PreBuiltTransportClient(Settings.EMPTY)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300));

    new ElasticBookDatabaseCreator(transportClient).create();

    LoanableBook book = new LoanableBook();
    Isbn isbn = new IsbnConverter().fromString("978-3791504544").get();
    book.setData(StandardBookDataKeys.ISBN, isbn);
    book.setData(StandardBookDataKeys.LANGUAGE, "English");
    book.setData(StandardBookDataKeys.TITLE, "A book");
    book.setData(StandardBookDataKeys.DESCRIPTION, "A cool book");
    book.setData(
        StandardBookDataKeys.AUTHORS,
        Arrays.asList(
            new Pair<>("Peter Müller", "Autor"),
            new Pair<>("Max Mustermann", "Übersetzer")
        )
    );

    System.out.println(book);

    ElasticBookDatabase database = new ElasticBookDatabase(transportClient);

    database.storeBook(book);

    sleepToLetElasticCatchUpVeryBadWayToDoIt();

    System.out.println();
    System.out.println();

    Optional<LoanableBook> bookByIsbn = database.getBookByIsbn(isbn);

    System.out.println(bookByIsbn);

    System.out.println();
    System.out.println();

    System.out.println(database.getBooksByFullTitleWithWildcards("A book"));
    System.out.println(database.getBooksByFullTitleWithWildcards("*book"));
    System.out.println(database.getBooksByFullTitleWithWildcards("A*ok"));

    System.out.println();
    System.out.println();

    System.out.println(database.getBooksByTitleRegex("a book"));
    System.out.println(database.getBooksByTitleRegex(".*book"));
    System.out.println(database.getBooksByTitleRegex("a.*ok"));

    System.out.println();
    System.out.println();

    System.out.println(database.getBookByAuthor("Peter*"));
    System.out.println(database.getBookByAuthor("Max*"));
    System.out.println(database.getBookByAuthor("*Muster*"));
    System.out.println(database.getBookByAuthor("*Mül*"));
    System.out.println(database.getBookByAuthor("Max MUSTERMANN"));

    System.out.println();
    System.out.println();

    database.deleteBook(isbn);

    sleepToLetElasticCatchUpVeryBadWayToDoIt();
    System.out.println(database.getBookByIsbn(isbn));
  }

  private static void sleepToLetElasticCatchUpVeryBadWayToDoIt() throws InterruptedException {
    Thread.sleep(2000);
  }
}
