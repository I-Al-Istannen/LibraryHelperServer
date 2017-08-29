package me.ialistannen.libraryhelperserver.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds a generic Map via a fluent interface.
 */
public class MapBuilder<K, V> {

  private Map<K, V> map = new HashMap<>();

  private MapBuilder() {
    // prevent instantiation
  }

  /**
   * Adds an entry.
   *
   * @param key The key
   * @param value The value
   * @return This builder
   */
  public MapBuilder<K, V> add(K key, V value) {
    map.put(key, value);
    return this;
  }

  /**
   * @return The built map. Mutable, not-shared copy.
   */
  public Map<K, V> build() {
    return new HashMap<>(map);
  }

  /**
   * @param <K> The type of the key
   * @param <V> The type of the value
   * @return A Builder for it
   */
  public static <K, V> MapBuilder<K, V> of() {
    return new MapBuilder<>();
  }

  /**
   * @param key The initial key
   * @param value The initial value
   * @param <K> The type of the key
   * @param <V> The type of the value
   * @return A Builder for it
   */
  public static <K, V> MapBuilder<K, V> of(K key, V value) {
    return new MapBuilder<K, V>().add(key, value);
  }
}
