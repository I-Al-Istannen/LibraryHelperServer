package me.ialistannen.libraryhelperserver.db.types.users.elastic;

import java.util.Map;
import java.util.Objects;
import me.ialistannen.libraryhelperserver.model.User;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;
import me.ialistannen.libraryhelperserver.model.hashing.bcrypt.BcryptHash;
import me.ialistannen.libraryhelperserver.model.hashing.bcrypt.BcryptHasher;

/**
 * A {@link User} that can be stored in the database
 */
class IntermediaryUser {

  private transient Hash hash;
  private String hashAsString;
  private String username;
  private Map<String, String> claims;

  private IntermediaryUser(String hashAsString, String username, Map<String, String> claims) {
    this.hashAsString = hashAsString;
    this.username = username;
    this.claims = claims;
  }

  private String getHashAsString() {
    return hashAsString;
  }

  private String getUsername() {
    return username;
  }

  private Map<String, String> getClaims() {
    return claims;
  }

  User toUser() {
    if (hash == null) {
      hash = new BcryptHash(BcryptHasher.getInstance(), getHashAsString());
    }
    return new User(hash, getUsername(), getClaims());
  }

  static IntermediaryUser fromUser(User user) {
    return new IntermediaryUser(user.getHash().getAsString(), user.getUsername(), user.getClaims());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntermediaryUser that = (IntermediaryUser) o;
    return Objects.equals(hash, that.hash) &&
        Objects.equals(getUsername(), that.getUsername());
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash, getUsername());
  }

  @Override
  public String toString() {
    return "IntermediaryUser{"
        + "hash=" + hash
        + ", hashAsString='" + hashAsString + '\''
        + ", username='" + username + '\''
        + ", claims=" + claims
        + '}';
  }
}
