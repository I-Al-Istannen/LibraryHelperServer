package me.ialistannen.libraryhelperserver.db.exceptions;

/**
 * Thrown when an error occurred creating the database.
 */
public class DatabaseCreationException extends DatabaseException {

  public DatabaseCreationException(String message) {
    super(message);
  }

  public DatabaseCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
