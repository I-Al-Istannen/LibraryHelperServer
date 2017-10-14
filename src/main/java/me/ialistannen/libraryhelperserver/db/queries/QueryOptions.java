package me.ialistannen.libraryhelperserver.db.queries;

/**
 * Contains the options used for a query.
 */
public interface QueryOptions {

  class BasicQueryOptions implements QueryOptions {

    private String queryString;
    private String fieldName;

    public BasicQueryOptions(String queryString, String fieldName) {
      this.queryString = queryString;
      this.fieldName = fieldName;
    }

    public String getQueryString() {
      return queryString;
    }

    public String getFieldName() {
      return fieldName;
    }
  }
}
