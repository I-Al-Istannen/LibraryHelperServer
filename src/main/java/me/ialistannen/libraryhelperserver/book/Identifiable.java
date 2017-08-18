package me.ialistannen.libraryhelperserver.book;

/**
 * Exposes a getId method that can be used to uniquely identify this object.
 *
 * @param <T> The type of the key
 */
public interface Identifiable<T> {

  /**
   * @return The key that uniquely identifies this object.
   */
  T getKey();
}
