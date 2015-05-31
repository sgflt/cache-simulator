package cz.zcu.kiv.cacheSimulator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;

/**
 * ClassLoader.java
 * 10. 5. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class ClassLoader {

  private final static Logger LOGGER = Logger.getLogger(ClassLoader.class.getName());

  public static List<String> loadClassInfo(final String path, final String packageName) {
    if (path.endsWith(".jar")) {
      return ClassLoader.loadClassInfoFromJar(path, packageName + "/");
    }

   final String sep = System.getProperty("file.separator");
   return ClassLoader.loadClassInfoFromDirectory(path + sep, packageName  + sep);
  }

  /**
   * metoda pro nacteni cache policies z adresare
   *
   * @param path
   *          cesta
   */
  private static List<String> loadClassInfoFromDirectory(final String path, final String packageName) {
    final List<String> classInfo = new ArrayList<>();
    final String pathToPackage = (path + packageName);
    final File dir = new File(pathToPackage);
    final File[] files = dir.listFiles();

    for (final File file : files) {
      if (file.isFile() && !file.getName().startsWith("I") && !file.getName().contains("$")
          && !file.getName().contains("Data") && !file.getName().contains("MainGUI")) {
        try {
          final Class<?> myClass = Class.forName(packageName.replace('/', '.').replace('\\', '.')
              + file.getName().substring(0, file.getName().lastIndexOf(".class")));
          Object newObject = null;
          // chceme pouze tridy s konstruktory bez parametru
          final Constructor<?>[] cons = myClass.getConstructors();
          if (cons.length == 1 && cons[0].getParameterTypes().length == 0) {
            newObject = myClass.newInstance();
          } else {
            continue;
          }

          if (newObject instanceof ICache) {
            classInfo.add(((ICache) newObject).cacheInfo());
          } else if (newObject instanceof IConsistencySimulation) {
            classInfo.add(((IConsistencySimulation) newObject).getInfo());
          } else if (file.getName().contains("Panel")) {
            classInfo.add(myClass.getName());
          }
        } catch (final InstantiationException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
        } catch (final IllegalAccessException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
        } catch (final ClassNotFoundException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, null, e);
        }
      }
    }

    return classInfo;
  }


  /**
   * metoda pro nacteni cache simulatoru z jar souboru
   *
   * @param path
   *          cesta k jar souboru
   */
  public static List<String> loadClassInfoFromJar(final String path, final String packageName) {
    final List<String> classNames = GlobalMethods.getJarClassNames(path, packageName);
    final List<String> classInfo = new ArrayList<>();

    for (final String className : classNames) {
      if (className.contains("ICache") || className.contains("IConsistency")
          || className.contains("Data")) {
        continue;
      }

      try {
        final URL url = new URL("jar:file:/" + path + "!/");
        try (final URLClassLoader ucl = new URLClassLoader(new URL[] {url})) {
          final Class<?> myClass = Class.forName(className.replace("/", "."), true, ucl);
          Object newObject = null;
          // chceme pouze tridy s konstruktory bez parametru
          final Constructor<?>[] cons = myClass.getConstructors();

          if (cons.length == 1 && cons[0].getParameterTypes().length == 0) {
            newObject = myClass.newInstance();
          } else {
            continue;
          }

          if (newObject instanceof ICache) {
            classInfo.add(((ICache) newObject).cacheInfo());
          }
          if (newObject instanceof IConsistencySimulation) {
            classInfo.add(((IConsistencySimulation) newObject).getInfo());
          }
          if (className.contains("Panel")) {
            classInfo.add(myClass.getName());
          }
        }
      } catch (final InstantiationException ex) {
        Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
      } catch (final IllegalAccessException ex) {
        Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
      } catch (final MalformedURLException ex) {
        ex.printStackTrace();
        Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
      } catch (final ClassNotFoundException ex) {
        Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        ex.printStackTrace();
      } catch (final IOException ex) {
        Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    return classInfo;
  }
}
