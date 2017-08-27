package me.ialistannen.libraryhelperserver.db;

import java.io.IOException;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 * Creates the elastic book database.
 */
public class ElasticBookDatabaseCreator {

  private static final Logger LOGGER = LogManager.getLogger(ElasticBookDatabaseCreator.class);

  static final String INDEX_NAME = "books";
  static final String TYPE_BOOK = "book";
  static final String RAW_MULTIFIELD_NAME = "raw";

  private TransportClient client;

  public ElasticBookDatabaseCreator(TransportClient client) {
    this.client = client;
  }

  /**
   * Creates the index and the type
   */
  public void create() {
    try {
      createWithExceptions();
    } catch (IOException e) {
      LOGGER.log(Level.ERROR, "Error creating the index.", e);
    }
  }

  private void createWithExceptions() throws IOException {
    boolean exists = client.admin()
        .indices()
        .prepareExists(INDEX_NAME)
        .get()
        .isExists();

    if (exists) {
//      client.admin().indices().prepareDelete(INDEX_NAME).get();
      return;
    }

    CreateIndexResponse createIndexResponse = client.admin()
        .indices()
        .prepareCreate(INDEX_NAME)
        .setSettings(
            //@formatter:off
            XContentFactory.jsonBuilder()
                .startObject()
                  .startObject("analysis")
                    .startObject("analyzer")
                      .startObject("raw_lowercase")
                        .field("type","custom")
                        .field("tokenizer", "keyword")
                        .startArray("filter")
                          .value("lowercase")
                        .endArray()
                      .endObject()
                    .endObject()
                  .endObject()
                .endObject()
            //@formatter:on
        )
        .addMapping(
            TYPE_BOOK,
            //@formatter:off
            XContentFactory.jsonBuilder()
                .startObject()
                  .startObject(TYPE_BOOK)
                    .startObject("properties")

                      .startObject(StandardBookDataKeys.TITLE.name())
                        .field("type", "text")
                        .startObject("fields")
                          .startObject("raw")
                            .field("type", "text")
                            .field("analyzer", "raw_lowercase")
                          .endObject()
                        .endObject()
                      .endObject()

                      .startObject(StandardBookDataKeys.AUTHORS.name())
                        .field("type", "nested")
                        .startObject("properties")
                          .startObject("key")
                            .field("type", "text")
                              .startObject("fields")
                                .startObject("raw")
                                  .field("type", "text")
                                  .field("analyzer", "raw_lowercase")
                                .endObject()
                              .endObject()
                          .endObject()
                        .endObject()
                      .endObject()

                    .endObject()
                  .endObject()
                .endObject()
            //@formatter:on
        )
        .get();

    if (!createIndexResponse.isAcknowledged()) {
      LOGGER.warn("Create index not acknowledged!");
    }
  }

}
