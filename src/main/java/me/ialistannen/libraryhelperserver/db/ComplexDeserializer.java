package me.ialistannen.libraryhelperserver.db;

import java.util.Map;
import me.ialistannen.isbnlookuplib.book.BookDataKey;

/**
 * A deserializer that can be used to deserialize a JsonObject returned by Gson. The structure gets
 * lost for complex types while serializing.
 */
interface ComplexDeserializer {

  BookDataKey getBookDataKey();

  /**
   * @param object The raw object
   * @return The deserialized object.
   */
  Object deserialize(Object object);

  /**
   * @param object The object to cast to a map
   * @return The map or null
   */
  default Map<String, Object> castToObjectMap(Object object) {
    if (!(object instanceof Map)) {
      return null;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) object;
    return map;
  }
}
