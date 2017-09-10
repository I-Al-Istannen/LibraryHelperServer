package me.ialistannen.libraryhelperserver.model.hashing;

import java.util.function.Function;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;

/**
 * A hashing algorithm to use against the data.
 *
 * @param <T> The type of {@link Hash} this algorithm uses.
 */
public interface HashingAlgorithm<T extends Hash> {

  /**
   * @param hash The {@link Hash} to check against
   * @param password The password to check
   * @param updateFunction The function to decide whether to accept an updated hash and it is
   * responsible to store it again
   * @return True if the given password hashes to the given hash
   */
  boolean verifyAndUpdate(T hash, String password, Function<Hash, Boolean> updateFunction);

  /**
   * @param hash The {@link Hash} to check against
   * @param password The password to check
   * @return True if the given password hashes to the given hash
   */
  boolean verify(T hash, String password);

  /**
   * Hashes and salts a password.
   *
   * @param password The password to hash
   * @return The {@link Hash} for the password
   */
  T hash(String password);

  /**
   * A hash. What it does is defined by subclasses.
   */
  abstract class Hash {

    private HashingAlgorithm hashingAlgorithm;

    public Hash(HashingAlgorithm hashingAlgorithm) {
      this.hashingAlgorithm = hashingAlgorithm;
    }

    /**
     * @return The {@link HashingAlgorithm} that was used
     */
    public <T extends Hash> HashingAlgorithm<T> getHashingAlgorithm() {
      @SuppressWarnings("unchecked")
      HashingAlgorithm<T> t = this.hashingAlgorithm;
      return t;
    }

    /**
     * @return The string version of this hash
     */
    public abstract String getAsString();
  }
}
