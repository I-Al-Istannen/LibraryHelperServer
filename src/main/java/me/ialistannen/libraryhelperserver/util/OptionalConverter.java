package me.ialistannen.libraryhelperserver.util;

import java.util.Optional;

/**
 * Converts my auxiliary and the java util Optionals to each other.
 */
public class OptionalConverter {


  /**
   * @param optional The {@link me.ialistannen.isbnlookuplib.util.Optional} optional
   * @param <T> The type of it
   * @return The JDK optional representing the same.
   */
  public static <T> Optional<T> toJDK(me.ialistannen.isbnlookuplib.util.Optional<T> optional) {
    return optional.isPresent() ? Optional.of(optional.get()) : Optional.empty();
  }
}
