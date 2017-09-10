package me.ialistannen.libraryhelperserver.model.hashing.bcrypt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import me.ialistannen.libraryhelperserver.model.hashing.HashingAlgorithm.Hash;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A test for the {@link BcryptHasher}.
 */
class BcryptHasherTest {

  private static final int ROUNDS = 10;
  private static BcryptHasher hasher;

  @BeforeAll
  static void setup() {
    hasher = new BcryptHasher(ROUNDS);
  }

  @Test
  void testGenerateHashSamePasswordDifferentHash() {
    String password = "test";

    assertNotEquals(hasher.hash(password), hasher.hash(password));
  }

  @Test
  void testVerifyFails() {
    String password = "test";
    BcryptHash hash = hasher.hash(password);

    assertFalse(hasher.verify(hash, "t"));
    assertFalse(hasher.verify(hash, "te"));
    assertFalse(hasher.verify(hash, "tes"));
    assertFalse(hasher.verify(hash, "tese"));
    assertFalse(hasher.verify(hash, "tesedsds"));
  }

  @Test
  void testVerifyWorks() {
    String password = "test";
    BcryptHash hash = hasher.hash(password);

    assertTrue(hasher.verify(hash, "test"));
  }

  @Test
  void testUpdateIterationCount() {
    String password = "test";
    BcryptHash hash = hasher.hash(password);

    BcryptHasher lowerIterations = new BcryptHasher(ROUNDS - 1);
    BcryptHasher higherIterations = new BcryptHasher(ROUNDS + 1);

    assertTrue(lowerIterations.verify(hash, "test"));
    assertTrue(higherIterations.verify(hash, "test"));

    assertFalse(lowerIterations.verify(hash, "testd"));
    assertFalse(higherIterations.verify(hash, "testd"));

    BcryptHash[] updatedHash = new BcryptHash[1];
    AtomicBoolean wasUpdated = new AtomicBoolean(false);
    Function<Hash, Boolean> updateFunction = newHash -> {
      wasUpdated.getAndSet(true);
      updatedHash[0] = (BcryptHash) newHash;
      return true;
    };
    lowerIterations.verifyAndUpdate(hash, password, updateFunction);
    assertTrue(wasUpdated.get(), "Hash was not updated!");
    assertTrue(lowerIterations.verify(updatedHash[0], password));

    wasUpdated.set(false);
    higherIterations.verifyAndUpdate(hash, password, updateFunction);
    assertTrue(wasUpdated.get(), "Hash was not updated!");
    assertTrue(higherIterations.verify(updatedHash[0], password));
  }

  @Test
  void testUpdateIgnoresInvalidPasswordIterationCount() {
    String password = "test";
    BcryptHash hash = hasher.hash(password);

    BcryptHasher lowerIterations = new BcryptHasher(ROUNDS - 1);
    BcryptHasher higherIterations = new BcryptHasher(ROUNDS + 1);

    BcryptHash[] updatedHash = new BcryptHash[1];
    AtomicBoolean wasUpdated = new AtomicBoolean(false);
    Function<Hash, Boolean> updateFunction = newHash -> {
      wasUpdated.getAndSet(true);
      updatedHash[0] = (BcryptHash) newHash;
      return true;
    };
    lowerIterations.verifyAndUpdate(hash, "garbage", updateFunction);
    assertFalse(wasUpdated.get(), "Hash was updated!");
    assertTrue(lowerIterations.verify(updatedHash[0], password));

    wasUpdated.set(false);
    higherIterations.verifyAndUpdate(hash, "garbage", updateFunction);
    assertFalse(wasUpdated.get(), "Hash was updated!");
    assertTrue(higherIterations.verify(updatedHash[0], password));
  }
}