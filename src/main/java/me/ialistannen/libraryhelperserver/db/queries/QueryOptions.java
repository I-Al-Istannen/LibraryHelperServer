package me.ialistannen.libraryhelperserver.db.queries;

/**
 * Contains the options used for a query.
 */
public interface QueryOptions {

  class BasicQueryOptions implements QueryOptions {

    private String queryString;
    private String fieldName;
    private final String rawFieldName;

    public BasicQueryOptions(String queryString, String fieldName, String rawFieldName) {
      this.queryString = queryString;
      this.fieldName = fieldName;
      this.rawFieldName = rawFieldName;
    }

    public String getQueryString() {
      return queryString;
    }

    public String getFieldName() {
      return fieldName;
    }

    public String getRawFieldName() {
      return rawFieldName;
    }
  }
}
