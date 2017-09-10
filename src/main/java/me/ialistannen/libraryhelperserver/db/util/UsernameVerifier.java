package me.ialistannen.libraryhelperserver.db.util;

/**
 * A class to verify a username.
 */
public class UsernameVerifier {

  /**
   * @param name The username to check
   * @return True if the username is valid.
   */
  public static boolean isValidUsername(String name) {
    return name.matches("[a-zA-Z0-9]+");
  }
}
