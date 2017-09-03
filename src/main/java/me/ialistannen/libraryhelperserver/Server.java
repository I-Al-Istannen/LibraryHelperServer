package me.ialistannen.libraryhelperserver;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import me.ialistannen.libraryhelperserver.db.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticBookDatabaseMutator;
import me.ialistannen.libraryhelperserver.server.endpoints.AddingApiEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.DeletingApiEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.SearchApiEndpoint;
import me.ialistannen.libraryhelperserver.server.utilities.Log4JAccessLogReceiver;
import org.apache.logging.log4j.LogManager;
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

    BookDatabaseMutator mutator = new ElasticBookDatabaseMutator(client);

    RoutingHandler routes = Handlers.routing()
        .get("/test", exchange -> exchange.getResponseSender().send("Magic?"))
        .get("/search", new SearchApiEndpoint(client))
        .put("/add", new AddingApiEndpoint(mutator))
        .delete("/delete", new DeletingApiEndpoint(new IsbnConverter(), mutator));

    AccessLogHandler rootHandler = new AccessLogHandler(
        routes,
        new Log4JAccessLogReceiver(LogManager.getLogger("me.ialistannen.accesslog")),
        "%h %l \"%r\" %s %b | Ref: \"%{i,Referer}\" Agent: \"%{i,User-Agent}",
        ClassLoader.getSystemClassLoader()
    );

    Undertow undertow = Undertow.builder()
        .addHttpListener(8080, "0.0.0.0", rootHandler)
        .build();

    new Thread(undertow::start).start();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println(undertow.getListenerInfo());
  }

  private static TransportClient getClient() throws UnknownHostException {
    return new PreBuiltTransportClient(Settings.EMPTY)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300));
  }

  private static LoanableBook getLoanableBook() {
    LoanableBook book = new LoanableBook();
    book.setData(StandardBookDataKeys.TITLE, "Geisterritter");
    book.setData(
        StandardBookDataKeys.ISBN,
        new IsbnConverter().fromString("978-3791504797").get()
    );
    book.setData(
        StandardBookDataKeys.AUTHORS,
        Arrays.asList(
            new Pair<>("Cornelia Funke", "Author"),
            new Pair<>("Friedrich Hechelmann ", "Illustrator")
        )
    );
    return book;
  }
}
