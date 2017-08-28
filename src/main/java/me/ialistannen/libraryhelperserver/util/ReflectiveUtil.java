package me.ialistannen.libraryhelperserver.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helps with some reflective operations.
 */
public class ReflectiveUtil {

  /**
   * Returns the complete (including interfaces) type hierarchy for a class.
   *
   * @param start The start class
   * @return The complete type hierarchy including all implemented interfaces
   */
  public static List<Class<?>> getTypeHierarchy(Class<?> start) {
    if (start.getSuperclass() == null) {
      return Collections.emptyList();
    }
    List<Class<?>> result = new ArrayList<>();

    result.add(start.getSuperclass());
    result.addAll(getTypeHierarchy(start.getSuperclass()));

    for (Class<?> anInterface : start.getInterfaces()) {
      result.add(anInterface);
      result.addAll(getTypeHierarchy(anInterface));
    }

    return result;
  }

}
