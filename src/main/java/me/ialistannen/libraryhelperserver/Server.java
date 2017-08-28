package me.ialistannen.libraryhelperserver;

import io.undertow.Handlers;
import io.undertow.Undertow;
import java.net.InetAddress;
import java.net.UnknownHostException;
import me.ialistannen.libraryhelperserver.server.endpoints.SearchApiEndpoint;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 * The main file for the server.
 */
public class Server {

  public static void main(String[] args) throws UnknownHostException {
    Undertow undertow = Undertow.builder()
        .addHttpListener(8080, "localhost", Handlers.routing()
            .get("/test", exchange -> exchange.getResponseSender().send("Magic?"))
            .get("/search", new SearchApiEndpoint(getClient()))
        )
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
}
