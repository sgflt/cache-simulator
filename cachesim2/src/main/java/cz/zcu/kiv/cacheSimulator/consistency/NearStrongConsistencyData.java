package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;

/**
 * trida pro uchovani dat o jednom uzivateli a jedne caching policy
 *
 * @author Pavel Bžoch
 */
public class NearStrongConsistencyData {

  /**
   * odkaz na id uzivateke
   */
  protected long userID;

  /**
   * odkaz na cachovaci politiku
   */
  protected ICache cache;

  /**
   * kolikrat doslo k nekonzistentnosti dat
   */
  protected int inconsistencyCount;

  /**
   * objem nekonzistentnich dat
   */
  protected long inconsistencySize;


  /**
   * konstruktor - iniciace promennych
   *
   * @param userID id uzivatele
   * @param cache odkaz na cache
   */
  public NearStrongConsistencyData(final long userID, final ICache cache) {
    this.userID = userID;
    this.cache = cache;
    this.inconsistencyCount = 0;
    this.inconsistencySize = 0;
  }


  /**
   * metoda pro update statistik
   *
   * @param fSize velikost souboru
   */
  public void update(final long fSize) {
    this.inconsistencyCount++;
    this.inconsistencySize += fSize;
  }


  /**
   * metoda pro porovnani se shodou na userID a cache
   *
   * @param userID userID
   * @param cache cache policy
   * @return true, pokud je shoda
   */
  public boolean compareTo(final long userID, final ICache cache) {
    return this.cache == cache && userID == this.userID;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    final long id = this.userID >> 32;
    final long ip = this.userID - (id << 32);

    final int ipAndID = (int) (id + ip);

    result = prime * result + ((this.cache == null) ? 0 : this.cache.hashCode());
    result = prime * result + (ipAndID);
    return result;
  }


  @Override
  public String toString() {
    final long id = this.userID >> 32;
    final String ip = (GlobalMethods.intToIp(this.userID - (id << 32)));

    return "AccessCOnsistencyData [userID=" + id + ", ip=" + ip + ", cache=" + this.cache
        + ", cache cap=" + this.cache.getCacheCapacity() + ", inconsistencyCount="
        + this.inconsistencyCount + ", inconsistencySize=" + this.inconsistencySize + "]";
  }


  // sekce getru a setru
  public long getUserID() {
    return this.userID;
  }


  public ICache getCache() {
    return this.cache;
  }


  public int getInconsistencyCount() {
    return this.inconsistencyCount;
  }


  public long getInconsistencySize() {
    return this.inconsistencySize;
  }

}
