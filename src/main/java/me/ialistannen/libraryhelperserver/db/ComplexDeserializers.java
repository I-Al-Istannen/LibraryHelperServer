package me.ialistannen.libraryhelperserver.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnType;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelperserver.util.Util;

/**
 * A collection of {@link ComplexDeserializers} containing the standard ones.
 */
class ComplexDeserializers {

  private Map<BookDataKey, ComplexDeserializer> deserializerMap;

  ComplexDeserializers() {
    deserializerMap = new HashMap<>();

    addDeserializer(new ComplexDeserializer() {
      private final Map<String, IsbnType> TYPE_LOOKUP_MAP = Util.getEnumLookupTable(
          IsbnType.class, Enum::name
      );

      @Override
      public BookDataKey getBookDataKey() {
        return StandardBookDataKeys.ISBN;
      }

      @Override
      public Object deserialize(Object object) {
        Map<String, Object> map = castToObjectMap(object);

        if (map == null) {
          return null;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        IsbnType isbnType = TYPE_LOOKUP_MAP.get(map.get("type"));

        if (!(map.get("digits") instanceof Iterable)) {
          return null;
        }

        @SuppressWarnings("unchecked")
        List<? extends Number> digits = (List<? extends Number>) map.get("digits");

        short[] digitArray = new short[digits.size()];

        for (int i = 0; i < digits.size(); i++) {
          Number digit = digits.get(i);
          digitArray[i] = digit.shortValue();
        }

        return new Isbn(digitArray, isbnType);
      }
    });

    addDeserializer(new ComplexDeserializer() {
      @Override
      public BookDataKey getBookDataKey() {
        return StandardBookDataKeys.AUTHORS;
      }

      @Override
      public Object deserialize(Object object) {
        List<Pair<String, String>> result = new ArrayList<>();

        if (!(object instanceof List)) {
          return null;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> entries = (List<Map<String, String>>) object;

        for (Map<String, String> entry : entries) {
          String key = entry.get("key");
          String value = entry.get("value");
          result.add(new Pair<>(key, value));
        }

        return result;
      }
    });
  }

  private void addDeserializer(ComplexDeserializer deserializer) {
    deserializerMap.put(deserializer.getBookDataKey(), deserializer);
  }

  /**
   * @param key The key to check for
   * @return True if there is a deserializer for that key
   */
  boolean hasDeserializer(BookDataKey key) {
    return deserializerMap.containsKey(key);
  }

  /**
   * @param key The key of the object
   * @param value The value
   * @return The deserialized value
   */
  Object deserialize(BookDataKey key, Object value) {
    if (!hasDeserializer(key)) {
      return null;
    }
    return deserializerMap.get(key).deserialize(value);
  }
}
