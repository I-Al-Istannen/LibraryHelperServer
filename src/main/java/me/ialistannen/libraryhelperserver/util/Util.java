package me.ialistannen.libraryhelperserver.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Some static utility methods.
 */
public class Util {

  /**
   * @param enumClass The class of the enum
   * @param transform The transformation to apply to the values
   * @param <T> The type of the enum
   * @param <V> The type of the transformed key
   * @return The reverse mapping table
   */
  public static <T extends Enum, V> Map<V, T> getEnumLookupTable(Class<T> enumClass,
      Function<T, V> transform) {

    Map<V, T> result = new HashMap<>();

    for (T t : enumClass.getEnumConstants()) {
      result.put(transform.apply(t), t);
    }

    return result;
  }
}
