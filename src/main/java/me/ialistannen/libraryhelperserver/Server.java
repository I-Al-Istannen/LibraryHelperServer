package me.ialistannen.libraryhelperserver;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.lookup.providers.amazon.AmazonIsbnLookupProvider;
import me.ialistannen.libraryhelperserver.db.creation.elastic.ElasticDatabaseCreator;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.types.book.BookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.types.book.elastic.ElasticBookDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.types.book.elastic.ElasticBookDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.types.users.UserDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.types.users.UserDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.types.users.elastic.ElasticUserDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.types.users.elastic.ElasticUserDatabaseMutator;
import me.ialistannen.libraryhelperserver.server.authentication.AuthenticatedRoutingHandler;
import me.ialistannen.libraryhelperserver.server.authentication.AuthenticationEndpoint;
import me.ialistannen.libraryhelperserver.server.authentication.AuthenticationUtil;
import me.ialistannen.libraryhelperserver.server.authentication.JsonSecurityLogic;
import me.ialistannen.libraryhelperserver.server.authentication.SecurityConfigCreator;
import me.ialistannen.libraryhelperserver.server.authentication.UserCreationEndpoint;
import me.ialistannen.libraryhelperserver.server.authentication.UserDeletionEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.AddingApiEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.DeletingApiEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.LendingApiEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.ModificationEndpoint;
import me.ialistannen.libraryhelperserver.server.endpoints.SearchApiEndpoint;
import me.ialistannen.libraryhelperserver.server.wrappinghandler.CustomHandlers;
import me.ialistannen.libraryhelperserver.server.wrappinghandler.HandlerChain;
import me.ialistannen.libraryhelperserver.util.Configs;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.pac4j.core.config.Config;

/**
 * The main file for the server.
 */
public class Server {

  private static HttpHandler wrapWithBasicHandlers(HttpHandler handler) {
    return HandlerChain.start(CustomHandlers::gzip)
        .next(BlockingHandler::new)
        .next(CustomHandlers::accessLog)
        .next(e -> new SessionAttachmentHandler(
            e,
            new InMemorySessionManager("SessionManager"),
            new SessionCookieConfig()
        ))
        .complete(handler);
  }

  public static void main(String[] args) throws UnknownHostException {
    JsonSecurityLogic jsonSecurityLogic = new JsonSecurityLogic();
    Config config = SecurityConfigCreator.create();

    HttpHandler coverHandler = buildCoverHandler(jsonSecurityLogic, config);
    PathHandler pathHandler = new PathHandler(wrapWithBasicHandlers(
        getRoutingHandler(config, jsonSecurityLogic)
    ))
        .addPrefixPath("/cover", coverHandler);

    Undertow undertow = Undertow.builder()
        .addHttpListener(8080, "0.0.0.0", pathHandler)
        .build();

    undertow.start();
  }

  private static HttpHandler buildCoverHandler(JsonSecurityLogic jsonSecurityLogic, Config config) {
    return AuthenticationUtil.requireAuthentication(
        CustomHandlers.resource("covers"), config, jsonSecurityLogic
    );
  }

  private static RoutingHandler getRoutingHandler(Config config, JsonSecurityLogic securityLogic)
      throws UnknownHostException {
    TransportClient client = getClient();

    ElasticDatabaseCreator databaseCreator = new ElasticDatabaseCreator();
    databaseCreator.create(client.admin().indices());

    BookDatabaseMutator mutator = new ElasticBookDatabaseMutator(client);
    BookDatabaseBrowser browser = new ElasticBookDatabaseBrowser(client);
    UserDatabaseBrowser userDatabaseBrowser = new ElasticUserDatabaseBrowser(client);
    UserDatabaseMutator userDatabaseMutator = new ElasticUserDatabaseMutator(client);
    IsbnConverter isbnConverter = new IsbnConverter();

    LendingApiEndpoint lendingApiEndpoint = new LendingApiEndpoint(isbnConverter, mutator, browser);
    String secret = Configs.getCustom().getString("authentication.signing_secret");

    return AuthenticatedRoutingHandler.builder(config, securityLogic)
        .authenticated(
            "get", "/test", exchange -> exchange.getResponseSender().send("Magic?")
        )
        .unauthenticated(
            "post", "/login", new AuthenticationEndpoint(
                secret, userDatabaseBrowser, userDatabaseMutator
            )
        )
        .authenticated(
            "delete", "/delete-user", new UserDeletionEndpoint(userDatabaseMutator)
        )
        .authenticated(
            "post", "/add-user", new UserCreationEndpoint(
                userDatabaseMutator, userDatabaseBrowser
            )
        )
        .authenticated("get", "/search", new SearchApiEndpoint(client))
        .authenticated(
            "put", "/add", new AddingApiEndpoint(
                mutator, isbnConverter, new AmazonIsbnLookupProvider(Locale.GERMAN, isbnConverter)
            ))
        .authenticated("post", "/modify", new ModificationEndpoint(mutator, browser))
        .authenticated(
            "delete", "/delete", new DeletingApiEndpoint(isbnConverter, mutator)
        )
        .authenticated("delete", "/lending", lendingApiEndpoint)
        .authenticated("put", "/lending", lendingApiEndpoint)
        .build();
  }

  private static TransportClient getClient() throws UnknownHostException {
    return new PreBuiltTransportClient(Settings.EMPTY)
        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300));
  }
}
