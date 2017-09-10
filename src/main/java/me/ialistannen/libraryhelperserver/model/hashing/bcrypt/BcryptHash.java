package me.ialistannen.libraryhelperserver.model.hashing.bcrypt;

import java.lang.reflect.Field;
import java.util.Objects;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;
import org.mindrot.jbcrypt.BCrypt;

/**
 * A {@link Hash} for the {@link BcryptHasher}.
 */
public class BcryptHash extends Hash {

  private String hashedPassword;
  private HashParts parts;

  public BcryptHash(BcryptHasher hashingAlgorithm, String hashedPassword) {
    super(hashingAlgorithm);
    this.hashedPassword = Objects.requireNonNull(hashedPassword, "hashedPassword can not be null!");

    this.parts = dissectHash(hashedPassword);
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
  int getRounds() {
    return parts.getRounds();
  }

  @Override
  public String getAsString() {
    return getHashedPassword();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BcryptHash that = (BcryptHash) o;
    return Objects.equals(getHashedPassword(), that.getHashedPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHashedPassword());
  }

  private HashParts dissectHash(String hash) {
    return new Object() {
      private int rounds;
      private String salt;
      private String hashValue;

      private int position;

      private boolean eat(String toEat) {
        int startPos = position;
        for (char c : toEat.toCharArray()) {
          if (position >= hash.length() || hash.charAt(position++) != c) {
            position = startPos;
            return false;
          }
        }
        return true;
      }

      private String eat(int count) {
        if (position + count > hash.length()) {
          throw new IllegalArgumentException(
              "Hash size too small, expected " + (position + count) + " got " + hash.length()
          );
        }
        int startPos = position;
        position += count;
        return hash.substring(startPos, position);
      }

      private void eatHeader() {
        if (!eat("$2a$")) {
          throw new IllegalArgumentException(String.format(
              "Invalid hash, got '%s', expected '$2a$'", hash
          ));
        }
      }

      private void eatRounds() {
        int start = position;
        while (!eat("$")) {
          position++;
        }
        String roundsString = hash.substring(start, position - 1);
        rounds = Integer.parseInt(roundsString);
      }

      private void eatSalt() {
        salt = eat(getSaltLength());
      }

      private void eatPasswordHash() {
        hashValue = eat(hash.length() - position);
      }

      private HashParts parse() {
        eatHeader();
        eatRounds();
        eatSalt();
        eatPasswordHash();
        return new HashParts(rounds);
      }

      private int getSaltLength() {
        try {
          Field saltLenField = BCrypt.class.getDeclaredField("BCRYPT_SALT_LEN");
          saltLenField.setAccessible(true);

          return saltLenField.getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
          throw new RuntimeException("Couldn't obtain salt length");
        }
      }
    }.parse();
  }

  private static class HashParts {

    private int rounds;

    private HashParts(int rounds) {
      this.rounds = rounds;
    }

    private int getRounds() {
      return rounds;
    }
  }
}
