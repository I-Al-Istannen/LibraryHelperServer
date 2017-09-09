package me.ialistannen.libraryhelperserver.db.util.exceptions;

/**
 * An exception that indicates an error while doing a database operation.
 */
public class DatabaseException extends RuntimeException {

  public DatabaseException(String message) {
    super(message);
  }

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
