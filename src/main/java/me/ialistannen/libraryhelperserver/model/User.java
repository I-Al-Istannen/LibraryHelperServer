package me.ialistannen.libraryhelperserver.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;

/**
 * A User of the service.
 */
public class User {

  private Hash hash;
  private String username;

  private Map<String, String> claims;
  private Set<String> roles;

  /**
   * @param hash The {@link Hash} of the user
   * @param username The username of the user
   * @param roles The roles the user has
   * @param claims The additional claims for the user
   */
  public User(Hash hash, String username, Set<String> roles, Map<String, String> claims) {
    this.hash = hash;
    this.username = username;
    this.roles = new HashSet<>(roles);
    this.claims = claims;
  }

  /**
   * Checks whether the provided password is correct.
   *
   * @param password The password to check
   * @param updateFunction The function to decide whether to accept an updated hash and it is
   * responsible to store it again
   * @return True if the provided password is correct for this user
   */
  public boolean verifyAndUpdate(String password, Function<Hash, Boolean> updateFunction) {
    return hash.getHashingAlgorithm().verifyAndUpdate(hash, password, updateFunction);
  }

  /**
   * Checks whether the provided password is correct.
   *
   * @param password The password to check
   * @return True if the provided password is correct for this user
   */
  public boolean verify(String password) {
    return hash.getHashingAlgorithm().verify(hash, password);
  }

  /**
   * @return The claims for the user
   */
  public Map<String, String> getClaims() {
    return claims;
  }

  /**
   * @return The roles for the user
   */
  public Set<String> getRoles() {
    return roles;
  }

  /**
   * @return The username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @return The {@link Hash} for the user
   */
  public Hash getHash() {
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(getHash(), user.getHash()) &&
        Objects.equals(getUsername(), user.getUsername());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHash(), getUsername());
  }

  @Override
  public String toString() {
    return "User{"
        + "hash=" + hash
        + ", username='" + username + '\''
        + ", claims=" + claims
        + '}';
  }
}
