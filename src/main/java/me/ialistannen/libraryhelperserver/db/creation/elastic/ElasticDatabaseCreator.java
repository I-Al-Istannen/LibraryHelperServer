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

      if (!doesIndexExist(client)) {
        createIndex(client);
        System.out.println("Created");
      } else {
        blowUpIndex(client);
        System.out.println("*Boom*");
        System.exit(0);
      }

    } catch (Exception e) {
      throw new DatabaseCreationException("Error creating the database", e);
    }
  }

  private boolean doesIndexExist(IndicesAdminClient client) {
    return client.prepareExists(StringConstant.INDEX_NAME.getValue()).get().isExists();
  }

  private void createIndex(IndicesAdminClient client) {
    String data = loadStringData("/resources/db/Create.json");

    CreateIndexResponse indexResponse = client
        .prepareCreate(StringConstant.INDEX_NAME.getValue())
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

  private void blowUpIndex(IndicesAdminClient client) {
    if (!client.prepareDelete(StringConstant.INDEX_NAME.getValue()).get().isAcknowledged()) {
      LOGGER.warn("Not acknowledged delete");
    }
  }

  /**
   * Static constants for the database
   */
  public enum StringConstant {
    INDEX_NAME("books"),
    TYPE_NAME("book");

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
