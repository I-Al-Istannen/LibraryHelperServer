package me.ialistannen.libraryhelperserver.db.types.users;

import me.ialistannen.libraryhelperserver.model.User;

/**
 * A class to mutate the user database.
 */
public interface UserDatabaseMutator {

  /**
   * Saves a user in the database or updates his record.
   *
   * @param user The {@link User} to save
   */
  void storeOrUpdateUser(User user);

  /**
   * Deletes a {@link User} from the database.
   *
   * @param user The {@link User} to delete
   */
  void deleteUser(User user);
}
