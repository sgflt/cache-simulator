package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * trida pro uchovani statistik pro single background consistency
 *
 * @author Pavel Bžoch
 */
public class MMWPConsistencyData {

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
   * velikost dat prenesenych kvuli konzistentnosti
   */
  private long transferredData = 0;

  /**
   * pocet nekonzistentnich dat, protoze upgrady se deji pouze po vyprseni TTL
   */
  private long inconsistenciesCount = 0;
  private long inconsFrequentCount = 0, inconsMediumFirCount = 0, inconsMediumSecCount = 0,
      inconsMediumThiCount = 0, inconsLeastCount = 0;


  /**
   * konstruktor - iniciace promennych
   *
   * @param userID id uzivatele
   * @param cache odkaz na cache
   */
  public MMWPConsistencyData(final long userID, final ICache cache) {
    this.userID = userID;
    this.cache = cache;
    this.asksCount = 0;
    this.updatesCount = 0;
    this.inconsistenciesCount = 0;
    this.transferredData = 0;
    this.inconsFrequentCount = 0;
    this.inconsLeastCount = 0;
    this.inconsMediumFirCount = 0;
    this.inconsMediumSecCount = 0;
    this.inconsMediumThiCount = 0;
  }


  /**
   * metoda pro zvyseni poctu ptani
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
  public void updateInconsistencies(final FileOnClient fOnCl) {
    this.inconsistenciesCount++;
    if (fOnCl.getVersion() >= MMWPConsistency.getHits5()) {
      this.inconsFrequentCount++;
      return;
    }
    if (fOnCl.getVersion() < MMWPConsistency.getHits2()) {
      this.inconsLeastCount++;
      return;
    }
    if (fOnCl.getVersion() < MMWPConsistency.getHits5()
        && fOnCl.getVersion() >= MMWPConsistency.getHits4()) {
      this.inconsMediumFirCount++;
      return;
    }
    if (fOnCl.getVersion() < MMWPConsistency.getHits4()
        && fOnCl.getVersion() >= MMWPConsistency.getHits3()) {
      this.inconsMediumSecCount++;
      return;
    }
    if (fOnCl.getVersion() < MMWPConsistency.getHits3()
        && fOnCl.getVersion() >= MMWPConsistency.getHits2()) {
      this.inconsMediumThiCount++;
      return;
    }
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

    return "BackgroudSingleConsistency  [userID=" + id + ", ip=" + ip + ", cache=" + this.cache
        + ", cache cap=" + this.cache.getCapacity() + ", pocetPtani=" + this.asksCount
        + ", pocetUpdatu=" + this.updatesCount + ", pocetInkonzistentnosti="
        + this.inconsistenciesCount + "]";
  }


  // sekce getru a setru pro zjisteni
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


  public long getTransferredData() {
    return this.transferredData;
  }


  public long getInconsFrequentCount() {
    return this.inconsFrequentCount;
  }


  public long getInconsMediumFirCount() {
    return this.inconsMediumFirCount;
  }


  public long getInconsMediumSecCount() {
    return this.inconsMediumSecCount;
  }


  public long getInconsMediumThiCount() {
    return this.inconsMediumThiCount;
  }


  public long getInconsLeastCount() {
    return this.inconsLeastCount;
  }


  public boolean checkInconsistrencies() {
    final long sum = this.inconsFrequentCount + this.inconsLeastCount + this.inconsMediumFirCount
        + this.inconsMediumSecCount + this.inconsMediumThiCount;
    return sum == this.inconsistenciesCount;
  }

}
