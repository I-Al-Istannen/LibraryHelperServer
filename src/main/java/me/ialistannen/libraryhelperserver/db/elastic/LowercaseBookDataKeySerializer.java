package me.ialistannen.libraryhelperserver.db.elastic;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.libraryhelperserver.book.StringBookDataKey;
import me.ialistannen.libraryhelperserver.util.ReflectiveUtil;

/**
 * A {@link TypeAdapterFactory} that enables lower snake case serialization for {@link
 * BookDataKey}s.
 */
class LowercaseBookDataKeySerializer implements TypeAdapterFactory {

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    if (!ReflectiveUtil.getTypeHierarchy(type.getRawType()).contains(BookDataKey.class)) {
      return null;
    }

    @SuppressWarnings("unchecked")
    TypeAdapter<T> typeAdapter = (TypeAdapter<T>) new TypeAdapter<BookDataKey>() {
      @Override
      public void write(JsonWriter out, BookDataKey value) throws IOException {
        String name = StringBookDataKey.getNormalizedName(value.name());

        out.value(name);
      }

      @Override
      public BookDataKey read(JsonReader in) throws IOException {
        // we will not deserialize them, as that is sadly not really possible here anyways
        return null;
      }
    };
    return typeAdapter;
  }
}
