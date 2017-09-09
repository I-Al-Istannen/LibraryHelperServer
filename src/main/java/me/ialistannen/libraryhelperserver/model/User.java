package me.ialistannen.libraryhelperserver.model;

import java.util.Map;
import java.util.function.Function;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;

/**
 * A User of the service.
 */
public class User {

  private Hash hash;

  private Map<String, String> claims;

  /**
   * @param hash The {@link Hash} of the user
   * @param claims The additional claims for the user
   */
  public User(Hash hash, Map<String, String> claims) {
    this.hash = hash;
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
}
