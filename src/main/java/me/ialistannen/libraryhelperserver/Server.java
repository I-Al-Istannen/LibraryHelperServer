package me.ialistannen.libraryhelperserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.isbn.IsbnType;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelperserver.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticBookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticBookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticDatabaseCreator;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByAuthorWildcards;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByIsbn;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByTitleRegex;
import me.ialistannen.libraryhelperserver.db.elastic.queries.QueryByTitleWildcards;
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

//    new ElasticBookDatabaseMutator(client).addBook(getLoanableBook2());
//    new ElasticBookDatabaseMutator(client).addBook(getLoanableBook());
//
//    sleepToAllowElasticToCatchUpBadWay();

    BookDatabaseBrowser browser = new ElasticBookDatabaseBrowser(client);
    System.out.println(browser.getForQuery(
        QueryByIsbn.forIsbn(getLoanableBook2().getData(StandardBookDataKeys.ISBN))
    ));
    System.out.println(browser.getForQuery(
        QueryByIsbn.forIsbn(getLoanableBook().getData(StandardBookDataKeys.ISBN))
    ));
    System.out.println(browser.getForQuery(
        QueryByIsbn.forIsbn(new Isbn(new short[]{1}, IsbnType.ISBN_10))
    ));

    System.out.println();
    System.out.println(browser.getForQuery(
        QueryByTitleWildcards.forQuery("*Test*")
    ));
    System.out.println(browser.getForQuery(
        QueryByTitleWildcards.forQuery("T*2")
    ));
    System.out.println(browser.getForQuery(
        QueryByTitleWildcards.forQuery("*LOL*")
    ));

    System.out.println();
    System.out.println(browser.getForQuery(
        QueryByTitleRegex.forRegex(".*test.*")
    ));
    System.out.println(browser.getForQuery(
        QueryByTitleRegex.forRegex("t.*2")
    ));
    System.out.println(browser.getForQuery(
        QueryByTitleRegex.forRegex(".*lol.*")
    ));

    System.out.println();
    System.out.println(browser.getForQuery(
        QueryByAuthorWildcards.forQuery("*Funke*")
    ));
    System.out.println(browser.getForQuery(
        QueryByAuthorWildcards.forQuery("Jo*n*")
    ));
    System.out.println(browser.getForQuery(
        QueryByAuthorWildcards.forQuery("P*")
    ));

    if ("".isEmpty()) {
      return;
    }

    new ElasticDatabaseCreator().create(client.admin().indices());

    ElasticBookDatabaseMutator elasticBookDatabaseMutator = new ElasticBookDatabaseMutator(client);
    elasticBookDatabaseMutator.addBook(getLoanableBook());

    sleepToAllowElasticToCatchUpBadWay();

    elasticBookDatabaseMutator.deleteAll();
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

  private static LoanableBook getLoanableBook2() {
    LoanableBook book = new LoanableBook();
    book.setData(StandardBookDataKeys.TITLE, "Test 2");
    book.setData(
        StandardBookDataKeys.ISBN,
        new IsbnConverter().fromString("978-3791500119").get()
    );
    book.setData(
        StandardBookDataKeys.AUTHORS,
        Arrays.asList(
            new Pair<>("R.A. Salvator", "Author"),
            new Pair<>("John Doe", "Übersetzer")
        )
    );
    return book;
  }

  private static void sleepToAllowElasticToCatchUpBadWay() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
