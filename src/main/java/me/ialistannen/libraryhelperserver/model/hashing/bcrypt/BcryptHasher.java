package me.ialistannen.libraryhelperserver.model.hashing.bcrypt;

import java.util.function.Function;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

/**
 * A {@link HashingAlgorithm} using Bcrypt
 */
public class BcryptHasher implements HashingAlgorithm<BcryptHash> {

  private static final Logger LOGGER = LogManager.getLogger(BcryptHasher.class);

  private final int rounds;

  /**
   * @param rounds The amount of rounds to use
   */
  public BcryptHasher(int rounds) {
    this.rounds = rounds;
  }

  @Override
  public boolean verifyAndUpdate(BcryptHash hash, String password,
      Function<Hash, Boolean> updateFunction) {
    boolean checkpw = verify(hash, password);

    if (hash.getRounds() != rounds) {
      LOGGER.debug("Converting a hash from {} to {} rounds", hash.getRounds(), rounds);
      return updateFunction.apply(hash(password));
    }

    return checkpw;
  }

  @Override
  public boolean verify(BcryptHash hash, String password) {
    return BCrypt.checkpw(password, hash.getHashedPassword());
  }

  @Override
  public BcryptHash hash(String password) {
    String salt = BCrypt.gensalt(rounds);
    String passwordHash = BCrypt.hashpw(password, salt);

    return new BcryptHash(this, passwordHash, salt);
  }
}
