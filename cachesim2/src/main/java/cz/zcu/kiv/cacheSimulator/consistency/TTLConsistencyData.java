package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;

/**
 * trida pro uchovavani dat o TTL konzistentnosti
 *
 * @author Pavel Bžoch
 */
public class TTLConsistencyData {

  /**
   * odkaz na id uzivateke
   */
  protected long userID;

  /**
   * odkaz na cachovaci politiku
   */
  protected ICache cache;

  /**
   * kolikrat jsme se ptali na verzi souboru
   */
  private long asksCount = 0;

  /**
   * kolikrat jsme upgradovali
   */
  private long updatesCount = 0;

  /**
   * promenne pro ulozeni casu, kdy doslo naposledy ke sledovani veryi souboru
   */
  private long lastAccessTime = -1;
  /**
   * pocet nekonzistentnich dat, protoze upgrady se deji pouze po vyprseni TTL
   */
  private long inconsistenciesCount = 0;

  /**
   * velikost dat prenesenych kvuli konzistentnosti
   */
  private long transferredData = 0;


  /**
   * konstruktor - iniciace promennych
   *
   * @param userID
   *          id uzivatele
   * @param cache
   *          odkaz na cache
   */
  public TTLConsistencyData(final long userID, final ICache cache) {
    this.userID = userID;
    this.cache = cache;
    this.asksCount = 0;
    this.updatesCount = 0;
    this.inconsistenciesCount = 0;
    this.lastAccessTime = -1;
    this.transferredData = 0;
  }


  /**
   * metoda pro zvyseni poctu ptani se
   */
  public void updateAsks() {
    this.asksCount++;
  }


  /**
   * metoda pro update poctu updatu souboru
   */
  public void updateUpdates() {
    this.updatesCount++;
  }


  /**
   * metoda pro update poctu inkonzistetnosti
   */
  public void updateInconsistencies() {
    this.inconsistenciesCount++;
  }


  /**
   * metoda pro zvyseni velikosti prenesenych dat
   *
   * @param fSize velikost souboru
   */
  public void updateTransferredData(final long fSize) {
    this.transferredData += fSize;
  }


  /**
   * metoda pro porovnani se shodou na userID a cache
   *
   * @param userID
   *          userID
   * @param cache
   *          cache policy
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

    return "TTL consistency [userID=" + id + ", ip=" + ip + ", cache=" + this.cache
        + ", cache cap=" + this.cache.getCapacity() + ", pocetPtani=" + this.asksCount
        + ", pocetUpdatu=" + this.updatesCount + ", pocetInkonzistentnosti="
        + this.inconsistenciesCount + "]";
  }


  /**
   * metoda pro ziskanicasu, kdy naposledy doslo ke kontrole souboru
   *
   * @return cas posledni kontroly
   */
  public long getLastAccessTime() {
    return this.lastAccessTime;
  }


  /**
   * metoda pro nastaveni noveho casu kontroly souboru
   *
   * @param lastAccessTimeFrequent cas
   */
  public void setLastAccessTime(final long lastAccessTime) {
    this.lastAccessTime = lastAccessTime;
  }


  // sekce getru - ziskani pozadovanych hodnot
  public long getUserID() {
    return this.userID;
  }


  public ICache getCache() {
    return this.cache;
  }


  public long getAsksCount() {
    return this.asksCount;
  }


  public long getUpdatesCount() {
    return this.updatesCount;
  }


  public long getInconsistenciesCount() {
    return this.inconsistenciesCount;
  }


  public long getTransferredData() {
    return this.transferredData;
  }

}
