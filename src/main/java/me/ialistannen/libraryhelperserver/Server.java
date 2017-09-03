package me.ialistannen.libraryhelperserver;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.lookup.providers.amazon.AmazonIsbnLookupProvider;
import me.ialistannen.libraryhelperserver.db.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.elastic.ElasticBookDatabaseMutator;
import me.ialistannen.libraryhelperserver.server.endpoints.AddingApiEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.DeletingApiEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.SearchApiEndpoint;
import me.ialistannen.libraryhelperserver.server.wrappinghandler.CustomHandlers;
import me.ialistannen.libraryhelperserver.server.wrappinghandler.HandlerChain;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * The main file for the server.
 */
public class Server {

  private static HttpHandler wrapWithBasicHandlers(HttpHandler handler) {
    return HandlerChain.start(CustomHandlers::gzip)
        .next(BlockingHandler::new)
        .next(CustomHandlers::accessLog)
        .complete(handler);
  }

  public static void main(String[] args) throws UnknownHostException {
    PathHandler pathHandler = new PathHandler(wrapWithBasicHandlers(getRoutingHandler()))
        .addPrefixPath("/images", CustomHandlers.resource("covers"));

    Undertow undertow = Undertow.builder()
        .addHttpListener(8080, "0.0.0.0", pathHandler)
        .build();

    undertow.start();
  }

  private static RoutingHandler getRoutingHandler() throws UnknownHostException {
    TransportClient client = getClient();

    BookDatabaseMutator mutator = new ElasticBookDatabaseMutator(client);
    IsbnConverter isbnConverter = new IsbnConverter();

    return Handlers.routing()
        .get("/test", exchange -> exchange.getResponseSender().send("Magic?"))
        .get("/search", new SearchApiEndpoint(client))
        .put("/add", new AddingApiEndpoint(mutator, isbnConverter,
            new AmazonIsbnLookupProvider(Locale.GERMAN, isbnConverter)))
        .delete("/delete", new DeletingApiEndpoint(isbnConverter, mutator));
  }

  private static TransportClient getClient() throws UnknownHostException {
    return new PreBuiltTransportClient(Settings.EMPTY)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300));
  }
}
