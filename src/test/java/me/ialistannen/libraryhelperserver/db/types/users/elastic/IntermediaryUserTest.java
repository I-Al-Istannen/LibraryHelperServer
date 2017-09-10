package me.ialistannen.libraryhelperserver.db.types.users.elastic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import me.ialistannen.libraryhelperserver.db.util.DatabaseUtil;
import me.ialistannen.libraryhelperserver.model.User;
import me.ialistannen.libraryhelperserver.model.hashing.bcrypt.BcryptHash;
import me.ialistannen.libraryhelperserver.model.hashing.bcrypt.BcryptHasher;
import org.junit.jupiter.api.Test;

/**
 * Test whether transformations work.
 */
class IntermediaryUserTest {

  @Test
  void testConversationWorksInBothDirections() {
    BcryptHasher hasher = new BcryptHasher(11);
    BcryptHash hash = hasher.hash("test");

    String username = "Test User";
    Map<String, String> claims = new HashMap<>();
    claims.put("test", "value");
    claims.put("test key 2", "value 2");
    User user = new User(hash, username, claims);

    IntermediaryUser intermediaryUser = IntermediaryUser.fromUser(user);

    assertEquals(user, intermediaryUser.toUser());
  }

  @Test
  void testSerializationWorks() {
    BcryptHasher hasher = new BcryptHasher(11);
    BcryptHash hash = hasher.hash("test");

    String username = "Test User";
    Map<String, String> claims = new HashMap<>();
    claims.put("test", "value");
    claims.put("test key 2", "value 2");
    User user = new User(hash, username, claims);

    IntermediaryUser intermediaryUser = IntermediaryUser.fromUser(user);

    String json = DatabaseUtil.getGson().toJson(intermediaryUser);
    IntermediaryUser rebuildUser = DatabaseUtil.getGson()
        .fromJson(json, IntermediaryUser.class);

    assertEquals(intermediaryUser, rebuildUser);
  }
}