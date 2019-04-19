package cz.zcu.kiv.cacheSimulator.shared;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author Lukáš Kvídera
 */
public class CacheLoader {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CacheLoader.class);
  private static final String POLICY_PACKAGE = "cz/zcu/kiv/cacheSimulator/cachePolicies";

  /**
   * Metoda pro ziskani jmen trid z jar souboru
   *
   * @param jarLocation cesta k jar souboru
   * @return seznam trid v dane package
   */
  private static List<String> getJarClassNames(final Path jarLocation) {
    final List<String> files = new ArrayList<>();

    if (jarLocation == null) {
      return files; // Empty.
    }

    // Lets stream the jar file
    try (final JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(jarLocation))) {
      JarEntry jarEntry;

      // Iterate the jar entries within that jar. Then make sure it follows the
      // filter given from the user.
      do {
        jarEntry = jarInputStream.getNextJarEntry();
        if (jarEntry != null) {
          String fileName = jarEntry.getName();
          if (fileName.contains(POLICY_PACKAGE) && !fileName.contains("$") && !fileName.equalsIgnoreCase(POLICY_PACKAGE)) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".class"));
            files.add(fileName);
          }
        }
      } while (jarEntry != null);
    } catch (final IOException ioe) {
      throw new RuntimeException("Unable to get Jar input stream from '" + jarLocation + "'", ioe);
    }
    return files;
  }


  /**
   * metoda pro nacteni cache simulatoru z jar souboru
   *
   * @param path cesta k jar souboru
   */
  public static List<String> loadCachesFromJar(final Path path) {
    final List<String> cacheClassNames = getJarClassNames(path);
    final var cacheInfos = new ArrayList<String>();
    for (final String cacheName : cacheClassNames) {
      if (!cacheName.contains("ICache")) {
        try {
          final URL url = new URL("jar:file:/" + path + "!/");
          try (final URLClassLoader ucl = new URLClassLoader(new URL[]{url})) {
            final ICache newCache = (ICache) (Class.forName(
              cacheName.replace("/", "."), true, ucl)).getDeclaredConstructor().newInstance();
            cacheInfos.add(newCache.cacheInfo());
          }
        } catch (
          final ClassNotFoundException |
            IllegalAccessException |
            InstantiationException |
            IOException |
            NoSuchMethodException |
            InvocationTargetException e
        ) {
          LOG.error("Cannnot load class", e);
        }
      }
    }
    return cacheInfos;
  }


  /**
   * metoda pro nacteni cache policies z adresare
   *
   * @param path cesta
   */
  public static List<String> loadCaches(final Path path) {
    final var cacheInfos = new ArrayList<String>();
    try (final var directoryStream = Files.newDirectoryStream(path.resolve(POLICY_PACKAGE))) {
      for (final var cache : directoryStream) {
        final var file = cache.toFile();
        if (file.isFile() && !file.getName().contains("ICache") && !file.getName().contains("$")) {
          loadCache(cacheInfos, file);
        }
      }
    } catch (final IOException e) {
      LOG.error("Cannnot load class", e);
    }
    return cacheInfos;
  }

  private static void loadCache(final ArrayList<String> cacheInfos, final File file) {
    try {
      final ICache newCache = (ICache) (Class
        .forName("cz.zcu.kiv.cacheSimulator.cachePolicies."
          + file.getName().substring(0, file.getName().lastIndexOf(".class"))))
        .getDeclaredConstructor().newInstance();
      final var cacheInfo = newCache.cacheInfo();
      cacheInfos.add(cacheInfo);
    } catch (final ClassNotFoundException |
      NoSuchMethodException |
      InstantiationException |
      IllegalAccessException |
      InvocationTargetException e) {
      LOG.error("Cannnot load class", e);
    }
  }
}
