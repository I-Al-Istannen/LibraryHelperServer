package me.ialistannen.libraryhelperserver.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Helps a bit with enums.
 */
public class EnumUtil {

  /**
   * Returns a reverse map for enum constant lookups.
   *
   * @param enumClass The enum class
   * @param transform The transform to apply
   * @param <K> The type of the resulting map key
   * @param <T> The type of the enum
   * @return A reverse mapping
   */
  public static <K, T extends Enum<T>> Map<K, T> getReverseMapping(Class<T> enumClass,
      Function<T, K> transform) {

    Map<K, T> result = new HashMap<>();

    for (T t : enumClass.getEnumConstants()) {
      result.put(transform.apply(t), t);
    }

    return result;
  }
}
