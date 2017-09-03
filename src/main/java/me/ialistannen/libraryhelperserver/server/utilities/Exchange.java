package me.ialistannen.libraryhelperserver.server.utilities;

/**
 * Utility methods to deal with Exchange objects.
 */
public class Exchange {

  //@formatter:off
  public interface BodyImpl extends JsonSender, JsonReader {}
  private static BodyImpl BODY = new BodyImpl() {};
  //@formatter:on

  public static BodyImpl body() {
    return BODY;
  }


  //@formatter:off
  public interface QueryParamsImpl extends QueryParams {}
  private static QueryParamsImpl QUERY_PARAMS = new QueryParamsImpl(){};
  //@formatter:on

  public static QueryParamsImpl queryParams() {
    return QUERY_PARAMS;
  }
}
