package cz.zcu.kiv.cacheSimulator;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;

/**
 * ClassLoader.java
 * 10. 5. 2015
 *
 * @author Lukáš Kvídera
 * @version 0.0
 */
public class ClassLoader {

  private final static Logger LOGGER =Logger.getLogger(ClassLoader.class.getName());
  /**
   * metoda pro nacteni cache policies z adresare
   *
   * @param path
   *          cesta
   */
  public static List<String> loadClassInfo(final String path, final String packageName) {
    final List<String> classInfo = new ArrayList<String>();
    final String pathToPackage = (path + packageName);
    final File dir = new File(pathToPackage);
    final File[] files = dir.listFiles();
    for (final File file : files) {
      if (file.isFile() && !file.getName().startsWith("I") && !file.getName().contains("$")
          && !file.getName().contains("Data") && !file.getName().contains("MainGUI")) {
        try {
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
            }
            if (newObject instanceof IConsistencySimulation) {
              classInfo.add(((IConsistencySimulation) newObject).getInfo());
            }
            if (file.getName().contains("Panel")) {
              classInfo.add(myClass.getName());
            }
          } catch (final InstantiationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
          } catch (final IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
          }
        } catch (final ClassNotFoundException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, null, e);
        }
      }
    }
    return classInfo;
  }
}
