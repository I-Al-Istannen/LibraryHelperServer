package me.ialistannen.libraryhelperserver.model.hashing.bcrypt;

import java.util.Objects;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;

/**
 *
 */
public class BcryptHash extends Hash {

  private String hashedPassword;
  private int rounds;

  public BcryptHash(HashingAlgorithm hashingAlgorithm, String hashedPassword, String salt) {
    super(hashingAlgorithm);
    this.hashedPassword = Objects.requireNonNull(hashedPassword, "hashedPassword can not be null!");

    this.rounds = getRoundsFromHash(salt);
  }

  // Taken from org.mindrot.jbcrypt.BCrypt.hashpw(), as no API is exposed.
  private int getRoundsFromHash(String salt) {
    char minor;
    int off;

    if (salt.charAt(0) != '$' || salt.charAt(1) != '2') {
      throw new IllegalArgumentException("Invalid salt version");
    }
    if (salt.charAt(2) == '$') {
      off = 3;
    } else {
      minor = salt.charAt(2);
      if (minor != 'a' || salt.charAt(3) != '$') {
        throw new IllegalArgumentException("Invalid salt revision");
      }
      off = 4;
    }

    // Extract number of rounds
    if (salt.charAt(off + 2) > '$') {
      throw new IllegalArgumentException("Missing salt rounds");
    }
    return Integer.parseInt(salt.substring(off, off + 2));
  }

  /**
   * @return The resulting hashed password
   */
  public String getHashedPassword() {
    return hashedPassword;
  }

  /**
   * @return The rounds used for this hash
   */
  public int getRounds() {
    return rounds;
  }
}
