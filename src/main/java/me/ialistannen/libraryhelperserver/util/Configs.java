package me.ialistannen.libraryhelperserver.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Get values from the configuration.
 */
public class Configs {

  private static Config system = ConfigFactory.systemProperties();

  private static Config custom = new Builder()
      .addConfig("application_used.conf")
      .addConfig("application.conf")
      .build();

  public static Config getSystem() {
    return system;
  }

  public static Config getCustom() {
    return custom;
  }

  /**
   * @param key The key
   * @return The {@link Path} for the given key
   */
  public static Path getCustomAsPath(String key) {
    return Paths.get(getCustom().getString(key)
        .replace("~", System.getProperty("user.home"))
    ).toAbsolutePath();
  }

  private static class Builder {

    private List<String> configs = new ArrayList<>();

    Builder addConfig(String path) {
      configs.add(path);
      return this;
    }

    Config build() {
      if (configs.size() < 1) {
        throw new IllegalArgumentException("At least one config must be added!");
      }

      Config rootConfig = ConfigFactory.parseResources(configs.remove(0));

      for (String configPath : configs) {
        rootConfig = rootConfig.withFallback(ConfigFactory.parseResources(configPath)).resolve();
      }

      return rootConfig;
    }
  }
}
