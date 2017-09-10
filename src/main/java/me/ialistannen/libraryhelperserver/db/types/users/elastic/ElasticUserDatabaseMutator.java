package me.ialistannen.libraryhelperserver.db.types.users.elastic;

import me.ialistannen.libraryhelperserver.db.creation.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.types.users.UserDatabaseMutator;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import me.ialistannen.libraryhelperserver.db.util.exceptions.DatabaseException;
import me.ialistannen.libraryhelperserver.model.User;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * An implementation of {@link UserDatabaseMutator} using Elasticsearch.
 */
public class ElasticUserDatabaseMutator implements UserDatabaseMutator {

  private static final String INDEX_NAME = StringConstant.USER_INDEX_NAME.getValue();
  private static final String TYPE_NAME = StringConstant.USER_TYPE_NAME.getValue();

  private TransportClient client;

  public ElasticUserDatabaseMutator(TransportClient client) {
    this.client = client;
  }

  @Override
  public void storeOrUpdateUser(User user) {
    String json = DatabaseUtil.getGson().toJson(IntermediaryUser.fromUser(user));
    IndexResponse indexResponse = client.prepareIndex(INDEX_NAME, TYPE_NAME, user.getUsername())
        .setSource(json, XContentType.JSON)
        .get();

    DatabaseUtil.assertIsOkay(
        indexResponse, "Failed to create or update user: '" + user.getUsername() + "'"
    );
  }

  @Override
  public void deleteUser(String userName) {
    DeleteResponse deleteResponse = client.prepareDelete(INDEX_NAME, TYPE_NAME, userName)
        .get();

    if (deleteResponse.getResult() == Result.DELETED
        || deleteResponse.getResult() == Result.NOT_FOUND) {
      return;
    }
    throw new DatabaseException(String.format(
        "Could not delete user '%s', result '%s'.",
        userName, deleteResponse.getResult()
    ));
  }
}
