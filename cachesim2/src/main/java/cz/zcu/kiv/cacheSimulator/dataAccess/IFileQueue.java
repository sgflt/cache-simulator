package cz.zcu.kiv.cacheSimulator.dataAccess;

import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

/**
 * rozhrani pro generovani nazvu souboru
 * <p>
 * interface for generating file requests
 *
 * @author Pavel Bzoch
 */
public interface IFileQueue {

  /**
   * metoda pro generovani jmena souboru a casu pristupu k nemu
   * method for generating file name, access time and userID
   *
   * @return trojice jmeno + cas + userID
   */
  Triplet<String, Long, Long> getNextFileName();


  /**
   * metoda vraci jmeno pristupovaneho souboru vcetne casu pristupu a velikosti pristupovaneho souboru
   * velikost - druhy argument, cas - treti argument; slouzi pro cteni z logu
   * <p>
   * method for generating file name, file size, access time and userID
   *
   * @return ctverice jmeno + velikost + cas + userID
   */
  Quartet<String, Long, Long, Long> getNextFileNameWithFSize();

  void reset();
}
