package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;

/**
 * trida pro simulaci pristupu bez konzistetnosti dat
 *
 * @author Pavel Bžoch
 */
public class NoConsistencyData extends NearStrongConsistencyData {

  /**
   * konstruktor - volani konstruktoru rodicovske tridy
   *
   * @param userID
   * @param cache
   */
  public NoConsistencyData(final long userID, final ICache cache) {
    super(userID, cache);
  }


  @Override
  public String toString() {
    final long id = this.userID >> 32;
    final String ip = (GlobalMethods.intToIp(this.userID - (id << 32)));

    return "NoConsistency [userID=" + id + ", ip=" + ip + ", cache=" + this.cache + ", cache cap="
        + this.cache.getCapacity() + ", inconsistencyCount=" + this.inconsistencyCount
        + ", inconsistencySize=" + this.inconsistencySize + "]";
  }

}
