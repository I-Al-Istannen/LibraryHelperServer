package me.ialistannen.libraryhelperserver.db.types.users.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.ialistannen.libraryhelperserver.db.creation.elastic.ElasticDatabaseCreator.StringConstant;
import me.ialistannen.libraryhelperserver.db.types.users.UserDatabaseBrowser;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import me.ialistannen.libraryhelperserver.model.User;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

/**
 * An implementation for the {@link UserDatabaseBrowser} using Elastic.
 */
public class ElasticUserDatabaseBrowser implements UserDatabaseBrowser {

  private TransportClient client;

  public ElasticUserDatabaseBrowser(TransportClient client) {
    this.client = client;
  }

  @Override
  public Optional<User> getUser(String username) {
    SearchResponse searchResponse = client
        .prepareSearch(StringConstant.USER_INDEX_NAME.getValue())
        .setTypes(StringConstant.USER_TYPE_NAME.getValue())
        .setQuery(QueryBuilders.termQuery(
            "username",
            username
        ))
        .get();

    DatabaseUtil.assertIsOkay(
        searchResponse, "Could not retrieve a user for the name: '" + username + "'"
    );

    if (searchResponse.getHits().getTotalHits() < 1) {
      return Optional.empty();
    }

    User user = searchHitToUser(searchResponse.getHits().getAt(0));
    return Optional.ofNullable(user);
  }

  @Override
  public List<User> getAllUsers() {
    SearchResponse searchResponse = client.prepareSearch(StringConstant.USER_INDEX_NAME.getValue())
        .setTypes(StringConstant.USER_TYPE_NAME.getValue())
        .setQuery(QueryBuilders.matchAllQuery())
        .setScroll(TimeValue.timeValueMinutes(1))
        .setSize(100)
        .get();

    List<User> results = new ArrayList<>();
    do {
      DatabaseUtil.assertIsOkay(searchResponse, "Search failed. Status: '%s'");

      for (SearchHit hit : searchResponse.getHits().getHits()) {
        results.add(searchHitToUser(hit));
      }

      searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
          .setScroll(TimeValue.timeValueMinutes(1))
          .get();

    } while (searchResponse.getHits().getHits().length > 0);

    return results;
  }

  private User searchHitToUser(SearchHit searchHit) {
    return DatabaseUtil.getGson()
        .fromJson(searchHit.getSourceAsString(), IntermediaryUser.class)
        .toUser();
  }
}
