package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * trida pro uchovani dat pro simulaci davkoveho ptani na verzi souboru na
 * pozadi
 *
 * @author Pavel Bžoch
 */
public class MMWPBatchConsistencyData {

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
  private long noReqFrequent = 0, noReqMediumFirst = 0, noReqMediumSec = 0, noReqMediumThird = 0,
      noReqLeastFrequent = 0;

  /**
   * promenna pro urceni trafficu pro zjisteni konsistentnosti
   */
  private long netTraffic = 0;

  /**
   * kolikrat jsme upgradovali
   */
  private long noOfUpdates = 0;

  /**
   * promenne pro ulozeni casu, kdy doslo naposledy ke sledovani verzi souboru
   */
  private long lastAccessTimeFrequent = -1, lastAccessTimeMediumFirst = -1,
      lastAccessTimeMediumSecond = -1, lastAccessTimeMediumThird = -1, lastAccessTimeLeast = -1;

  /**
   * pocet nekonzistentnich dat, protoze upgrady se deji pouze po vyprseni TTL
   */
  private long inconsFrequentCount = 0, inconsMediumFirCount = 0, inconsMediumSecCount = 0,
      inconsMediumThiCount = 0, inconsLeastCount = 0;
  private long incosistenciesCount = 0;


  /**
   * konstruktor - iniciace promennych
   *
   * @param userID
   *          id uzivatele
   * @param cache
   *          odkaz na cache
   */
  public MMWPBatchConsistencyData(final long userID, final ICache cache) {
    this.userID = userID;
    this.cache = cache;
    this.noReqFrequent = 0;
    this.noReqLeastFrequent = 0;
    this.noReqMediumFirst = 0;
    this.noReqMediumSec = 0;
    this.noReqMediumThird = 0;
    this.netTraffic = 0;
    this.noOfUpdates = 0;
    this.inconsFrequentCount = 0;
    this.inconsLeastCount = 0;
    this.inconsMediumFirCount = 0;
    this.inconsMediumSecCount = 0;
    this.inconsMediumThiCount = 0;
    this.incosistenciesCount = 0;
    this.lastAccessTimeFrequent = -1;
    this.lastAccessTimeLeast = -1;
    this.lastAccessTimeMediumFirst = -1;
    this.lastAccessTimeMediumSecond = -1;
    this.lastAccessTimeMediumThird = -1;
  }


  /**
   * metoda pro zvyseni poctu ptani u nejcastejsich dotazu
   */
  public void updateFreqAsks() {
    this.noReqFrequent++;
  }


  /**
   * metoda pro zvyseni poctu ptani u druhych nejcastejsich dotazu
   */
  public void updateMediumFirstAsk() {
    this.noReqMediumFirst++;

  }


  /**
   * metoda pro zvyseni poctu ptani u tretich nejcastejsich dotazu
   */
  public void updateMediumSecAsks() {
    this.noReqMediumSec++;

  }


  /**
   * metoda pro zvyseni poctu ptani u nejcastejsich dotazu
   */
  public void updateLeastFreqAsks() {
    this.noReqLeastFrequent++;
  }


  /**
   * metoda pro update poctu updatu souboru
   */
  public void updateUpdates() {
    this.noOfUpdates++;
  }


  /**
   * metoda pro update poctu inkonzistetnosti
   */
  public void updateInconsistencies(final FileOnClient fOnCl) {
    this.incosistenciesCount++;
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
   * metoda pro porovnani se shodou na userID a cache
   *
   * @param userID
   *          userID
   * @param cache
   *          cache policy
   * @return true, pokud je shoda
   */
  public boolean compareTo(final long userID, final ICache cache) {
    final boolean ret = this.cache == cache && userID == this.userID;
    if (ret)
      return true;
    else
      return false;
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

    return "Background Batch Consistency [userID=" + id + ", ip=" + ip + ", cache=" + this.cache
        + ", cache cap=" + this.cache.getCacheCapacity() + ", pocetPtaniNejcastejsi=;"
        + this.noReqFrequent + "; pocetPtaniStredniPrvni=;" + this.noReqMediumFirst
        + "; pocetPtaniStredniDruhe=;" + this.noReqMediumSec + "; pocetPtaniStredniTreti=;"
        + this.noReqMediumThird + "; pocetPtaniNejmeneCaste=;" + this.noReqLeastFrequent
        + "; pocetUpdatu=" + this.noOfUpdates + +this.inconsFrequentCount
        + ", inconsMediumFirCount=" + this.inconsMediumFirCount + ", inconsMediumSecCount="
        + this.inconsMediumSecCount + ", inconsMediumThiCount=" + this.inconsMediumThiCount
        + ", inconsLeastCount=" + this.inconsLeastCount + "; traffic=;" + this.netTraffic + ";]";
  }


  /**
   * metoda pro stradani trafficu potrebneho na prenos nekonzistentniho souboru
   *
   * @param fSize velikost souboru
   */
  public void updateTraffic(final long fSize) {
    this.netTraffic += fSize;
  }


  /**
   * metoda pro ziskanicasu, kdy naposledy doslo ke kontrole casto pristupovanych souboru
   *
   * @return cas posledni kontroly
   */
  public long getLastAccessTimeFrequent() {
    return this.lastAccessTimeFrequent;
  }


  /**
   * metoda pro nastaveni noveho casu kontrolz casto pristupovanych souboru
   *
   * @param lastAccessTimeFrequent cas
   */
  public void setLastAccessTimeFrequent(final long lastAccessTimeFrequent) {
    this.lastAccessTimeFrequent = lastAccessTimeFrequent;
  }


  /**
   * metoda pro ziskanicasu, kdy naposledy doslo ke kontrole nejmene casto pristupovanych souboru
   *
   * @return cas posledni kontroly
   */
  public long getLastAccessTimeLeast() {
    return this.lastAccessTimeLeast;
  }


  /**
   * metoda pro nastaveni noveho casu kontroly nejmene casto pristupovanych souboru
   *
   * @param lastAccessTimeFrequent cas
   */
  public void setLastAccessTimeLeast(final long lastAccessTimeLeast) {
    this.lastAccessTimeLeast = lastAccessTimeLeast;
  }


  /**
   * metoda pro ziskanicasu, kdy naposledy doslo ke kontrole prvni stredne casto pristupovanych
   * souboru
   *
   * @return cas posledni kontroly
   */
  public long getLastAccessTimeMediumFirst() {
    return this.lastAccessTimeMediumFirst;
  }


  /**
   * metoda pro nastaveni noveho casu kontroly prvni stredne casto pristupovanych souboru
   *
   * @param lastAccessTimeFrequent cas
   */
  public void setLastAccessTimeMediumFirst(final long i) {
    this.lastAccessTimeMediumFirst = i;

  }


  /**
   * metoda pro ziskanicasu, kdy naposledy doslo ke kontrole druhe stredne casto pristupovanych
   * souboru
   *
   * @return cas posledni kontroly
   */
  public long getLastAccessTimeMediumSecond() {
    return this.lastAccessTimeMediumSecond;
  }


  /**
   * metoda pro nastaveni noveho casu kontroly druhe stredne casto pristupovanych souboru
   *
   * @param lastAccessTimeFrequent cas
   */
  public void setLastAccessTimeMediumSecond(final long i) {
    this.lastAccessTimeMediumSecond = i;
  }


  public void updateMediumThirdAsk() {
    this.noReqMediumThird++;
  }


  public long getLastAccessTimeMediumThird() {
    return this.lastAccessTimeMediumThird;
  }


  public void setLastAccessTimeMediumThird(final long i) {
    this.lastAccessTimeMediumThird = i;
  }


  // sekce getru pro yjisteni statistik pro vykresleni
  public long getUserID() {
    return this.userID;
  }


  public ICache getCache() {
    return this.cache;
  }


  public long getNoOfFreqAsk() {
    return this.noReqFrequent;
  }


  public long getNoOfMediumFirAsk() {
    return this.noReqMediumFirst;
  }


  public long getNoOfMediumSecAsk() {
    return this.noReqMediumSec;
  }


  public long getNoOfMediumThiAsk() {
    return this.noReqMediumThird;
  }


  public long getNoOfLeastFreqAsk() {
    return this.noReqLeastFrequent;
  }


  public long getNetTraffic() {
    return this.netTraffic;
  }


  public long getNoOfUpdates() {
    return this.noOfUpdates;
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
    return sum == this.incosistenciesCount;
  }

}
