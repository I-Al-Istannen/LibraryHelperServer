package me.ialistannen.libraryhelperserver.db.creation.elastic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import me.ialistannen.libraryhelperserver.db.util.exceptions.DatabaseCreationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * Creates the database, if needed.
 */
public class ElasticDatabaseCreator {

  private static final Logger LOGGER = LogManager.getLogger(ElasticDatabaseCreator.class);

  /**
   * Creates the indices and mappings in elastic
   *
   * @param client The {@link IndicesAdminClient} to use
   * @throws DatabaseCreationException if an error occurs while creating it
   */
  public void create(IndicesAdminClient client) {
    try {

      String bookIndexName = StringConstant.BOOK_INDEX_NAME.getValue();
      if (!doesIndexExist(client, bookIndexName)) {
        createIndex(client, bookIndexName, "CreateBookIndex.json");
        LOGGER.info("Created book index");
      }

      String userIndexName = StringConstant.USER_INDEX_NAME.getValue();
      if (!doesIndexExist(client, userIndexName)) {
        createIndex(client, userIndexName, "CreateUserIndex.json");
        LOGGER.info("Created user index");
      }

    } catch (Exception e) {
      throw new DatabaseCreationException("Error creating the database", e);
    }
  }

  private boolean doesIndexExist(IndicesAdminClient client, String indexName) {
    return client.prepareExists(indexName).get().isExists();
  }

  /**
   * @param client The client to use
   * @param name The name of the index
   * @param jsonPath The path to the json settings file. Relative to "/resources/db/".
   */
  private void createIndex(IndicesAdminClient client, String name, String jsonPath) {
    String data = loadStringData("/resources/db/" + jsonPath);

    CreateIndexResponse indexResponse = client
        .prepareCreate(name)
        .setSource(data, XContentType.JSON)
        .get();

    if (!indexResponse.isAcknowledged()) {
      throw new DatabaseCreationException("Response was not acknowledged");
    }
  }

  private String loadStringData(@SuppressWarnings("SameParameterValue") String location) {
    try (InputStream resourceAsStream = getClass().getResourceAsStream(location);
        InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream);
        BufferedReader reader = new BufferedReader(inputStreamReader)) {

      StringBuilder builder = new StringBuilder();

      String tmp;
      while ((tmp = reader.readLine()) != null) {
        builder.append(tmp);
      }

      return builder.toString();

    } catch (IOException e) {
      LOGGER.log(Level.FATAL, "Could not load JSON while creating the database", e);
      throw new DatabaseCreationException("Can not continue, loading failed", e);
    }
  }

  /**
   * Static constants for the database
   */
  public enum StringConstant {
    BOOK_INDEX_NAME("books"),
    BOOK_TYPE_NAME("book"),
    USER_INDEX_NAME("users"),
    USER_TYPE_NAME("user");

    private String value;

    StringConstant(String value) {
      this.value = value;
    }

    /**
     * @return The value of this constant
     */
    public String getValue() {
      return value;
    }
  }
}
