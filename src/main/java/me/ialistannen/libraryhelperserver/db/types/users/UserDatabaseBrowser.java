package me.ialistannen.libraryhelperserver.db.types.users;

import java.util.List;
import java.util.Optional;
import me.ialistannen.libraryhelperserver.model.User;

/**
 * A browser for the users database.
 */
public interface UserDatabaseBrowser {


  /**
   * Retrieves a user by his name.
   *
   * @param username The name of the user
   * @return The {@link User} with the name, if any
   */
  Optional<User> getUser(String username);

  /**
   * Retrieves all users.
   *
   * @return All {@link User}s in this database
   */
  List<User> getAllUsers();
}
