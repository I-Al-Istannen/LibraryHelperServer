package me.ialistannen.libraryhelperserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import me.ialistannen.isbnlookuplib.book.Book;
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

    sleepToLetElasticCatchUpVeryBadWayToDoIt();

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
    LoanableBook book2 = new LoanableBook();
    Isbn isbn2 = new IsbnConverter().fromString("978-3791500119").get();
    book2.setData(StandardBookDataKeys.ISBN, isbn2);
    book2.setData(StandardBookDataKeys.LANGUAGE, "Deutsch");
    book2.setData(StandardBookDataKeys.TITLE, "A dragon book");
    book2.setData(StandardBookDataKeys.DESCRIPTION, "This book is about dragons");
    book2.setData(
        StandardBookDataKeys.AUTHORS,
        Arrays.asList(
            new Pair<>("Peter Müller", "Übersetzer"),
            new Pair<>("Cornelia Funke", "Autor")
        )
    );

    System.out.println();
    System.out.println("################################");
    System.out.println(centerInWidth("Original", 32));
    System.out.println("################################");
    System.out.println();
    outputBooks(Arrays.asList(book, book2));

    ElasticBookDatabase database = new ElasticBookDatabase(transportClient);

    database.storeBook(book);
    database.storeBook(book2);

    sleepToLetElasticCatchUpVeryBadWayToDoIt();

    System.out.println();
    System.out.println("################################");
    System.out.println(centerInWidth("By ISBN", 32));
    System.out.println("################################");
    System.out.println();

    outputBook(database.getBookByIsbn(isbn));
    outputBook(database.getBookByIsbn(isbn2));

    System.out.println();
    System.out.println("################################");
    System.out.println(centerInWidth("Title wildcards", 32));
    System.out.println("################################");
    System.out.println();

    outputBooks(database.getBooksByFullTitleWithWildcards("A book"));
    outputBooks(database.getBooksByFullTitleWithWildcards("*book"));
    outputBooks(database.getBooksByFullTitleWithWildcards("A*ok"));

    System.out.println();
    System.out.println("################################");
    System.out.println(centerInWidth("Title regex", 32));
    System.out.println("################################");
    System.out.println();

    outputBooks(database.getBooksByTitleRegex("a book"));
    outputBooks(database.getBooksByTitleRegex(".*book"));
    outputBooks(database.getBooksByTitleRegex("a.*ok"));

    System.out.println();
    System.out.println("################################");
    System.out.println(centerInWidth("Author", 32));
    System.out.println("################################");
    System.out.println();

    outputBooks(database.getBookByAuthor("Peter*"));
    outputBooks(database.getBookByAuthor("Max*"));
    outputBooks(database.getBookByAuthor("*Muster*"));
    outputBooks(database.getBookByAuthor("*Mül*"));
    outputBooks(database.getBookByAuthor("Max MUSTERMANN"));
    outputBooks(database.getBookByAuthor("CORNELIA*"));

    // DELETE

    database.deleteBook(isbn);

    sleepToLetElasticCatchUpVeryBadWayToDoIt();

    System.out.println();
    System.out.println("################################");
    System.out.println(centerInWidth("After deletion", 32));
    System.out.println("################################");
    System.out.println();

    outputBook(database.getBookByIsbn(isbn));
    outputBook(database.getBookByIsbn(isbn2));
  }

  private static void sleepToLetElasticCatchUpVeryBadWayToDoIt() throws InterruptedException {
    Thread.sleep(2000);
  }

  private static void outputBooks(List<? extends Book> books) {
    books.stream().map(Optional::ofNullable).forEach(Server::outputBook);
    System.out.println("--------------------------------");
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static void outputBook(Optional<? extends Book> book) {
    outputBook(book, false);
  }

  @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue"})
  private static void outputBook(Optional<? extends Book> book, boolean full) {
    if (!book.isPresent()) {
      System.out.println("Empty :(");
      return;
    }
    if (full) {
      System.out.println(book.get().nicerToString());
    } else {
      try {
        byte[] md5s = MessageDigest.getInstance("md5").digest(book.get().toString().getBytes());
        System.out.println(Base64.getMimeEncoder().encodeToString(md5s));
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }
  }

  @SuppressWarnings("SameParameterValue")
  private static String centerInWidth(String string, int width) {
    int length = string.length();

    int paddingAmount = (width - length) / 2;

    StringBuilder stringBuilder = new StringBuilder();

    for (int i = 0; i < paddingAmount; i++) {
      stringBuilder.append(" ");
    }
    stringBuilder.append(string);

    return stringBuilder.toString();
  }
}
