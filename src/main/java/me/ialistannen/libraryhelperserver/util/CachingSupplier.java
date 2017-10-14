package me.ialistannen.libraryhelperserver.util;

import java.util.function.Supplier;

/**
 * A supplier that caches the value.
 */
public class CachingSupplier<T> implements Supplier<T> {

  private T cachedValue;
  private Supplier<T> supplier;

  public CachingSupplier(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    if (cachedValue == null) {
      cachedValue = supplier.get();
    }
    return cachedValue;
  }
}
