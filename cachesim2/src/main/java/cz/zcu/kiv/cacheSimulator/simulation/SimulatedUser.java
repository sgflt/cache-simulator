package cz.zcu.kiv.cacheSimulator.simulation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

/**
 * trida pro reprezentaci simulovaneho uzivatele kazdy uzivatel ma svou sadu
 * cachovacich algoritmu, sve jednoznacne ID
 *
 * @author Pavel Bzoch
 *
 */
public class SimulatedUser {

  /**
   * identifikator uzivatele
   */
  private final long ID;

  /**
   * promenna pro uchovani cachovacich algoritmu prvni je pro uchovani odkazu
   * na cache, druha je pro urceni cacheHit, treti je pro urceni saved traffic
   */
  private ArrayList<Triplet<ICache[], Long[], Long[]>> caches;

  /**
   * promenna pro uchovani archivu
   */
  private final Hashtable<String, Quartet<Long[], Long[], Double[], Double[]>> cachesResults;

  /**
   * promenna pro ulozeni celkoveho poctu pristupu k souborum
   */
  private long fileAccessed = 0;


  /**
   * promenna pro ulozeni celkoveho potrebneho datoveho prenosu
   */
  private long totalNetworkBandwidth = 0;


  /**
   * konstruktor - inicializace user ID
   *
   * @param iD
   *            user ID
   */
  public SimulatedUser(final long iD) {
    super();
    this.ID = iD;
    this.fileAccessed = 0;
    this.totalNetworkBandwidth = 0;
    this.loadCaches(MainGUI.getInstance().getCacheSizes());
    this.cachesResults = new Hashtable<String, Quartet<Long[], Long[], Double[], Double[]>>();
  }

  /**
   * metoda, ktera podle nasatveni v global variables nacte cachovaci
   * algoritmy
   */
  private void loadCaches(final Integer[] cacheSizes) {
    if (this.caches != null) {
      this.caches.clear();
    }
    else {
      this.caches = new ArrayList<Triplet<ICache[], Long[], Long[]>>();
    }

    ICache cache = null;
    for (final String cacheName : MainGUI.getInstance().getCachesNames()) {
      final ICache[] cachePolicies = new ICache[cacheSizes.length];
      final Long cacheHit[] = new Long[cacheSizes.length];
      final Long savedTraffic[] = new Long[cacheSizes.length];
      final Triplet<ICache[], Long[], Long[]> cacheStat = new Triplet<ICache[], Long[], Long[]>(cachePolicies, cacheHit, savedTraffic);

      for (int i = 0; i < cacheSizes.length; i++) {
        try {
          cache = (ICache) Class.forName(
              "cz.zcu.kiv.cacheSimulator.cachePolicies." + cacheName)
              .newInstance();
          cache.setCapacity(cacheSizes[i]*1024L*1024L);
          cacheStat.getFirst()[i] = cache;
          cacheStat.getSecond()[i] = 0L;
          cacheStat.getThird()[i] = 0L;
        } catch (final Exception ex) {
          Logger.getLogger(SimulatedUser.class.getName()).log(
              Level.SEVERE, null, ex);
        }
      }
      this.caches.add(cacheStat);
    }
  }

  /**
   * metoda pro ziskani ID
   *
   * @return id uzivatele
   */
  public long getID() {
    return this.ID;
  }

  public long getFileAccessed() {
    return this.fileAccessed;
  }

  public long getTotalNetworkBandwidth() {
    return this.totalNetworkBandwidth;
  }

  /**
   * metoda pro ziskani seznamu cachovacich algoritmu, ktere odpovidaji
   * uzivateli
   *
   * @return seznam algoritmu
   */
  public ArrayList<Triplet<ICache[], Long[], Long[]>> getCaches() {
    return this.caches;
  }

  /**
   * metoda pro zvyseni poctu pristupovanych souboru v ramci tohoto
   * cachovaciho algoritmu
   */
  public void incereaseFileAccess() {
    this.fileAccessed += 1;
  }

  /**
   * metoda pro zvyseni celkoveho poctu prenesenych bytu
   *
   * @param fileSize
   *            velikost pridavaneho souboru
   */
  public void increaseTotalNetworkBandwidth(final long fileSize) {
    this.totalNetworkBandwidth += fileSize;
  }

  /**
   * metoda pro vytisteni statistik
   */
  public void printStatistics() {
    if (this.fileAccessed < GlobalVariables.getLimitForStatistics())
      return;
    final long id = this.ID >> 32;
    final String ip = (GlobalMethods.intToIp(this.ID - (id << 32)));
    System.out.println("=================== Statistics for user id: " + id
        + ", ip: " + ip + " ===================\n");
    for (final Triplet<ICache[], Long[], Long[]> cache : this.caches) {
      System.out
          .println(cache.getFirst().toString()
              + " read hits: "
              + cache.getSecond()
              + ", saved capacity: "
              + cache.getThird()
              + ", cache hit ratio: "
            //  + (double) (((int) (cache.getSecond() * 10000 / fileAccessed)) / 100.0)
              + "%");
    }
    System.out.println("\nFiles requested: " + this.fileAccessed
        + ", total file sizes: " + this.totalNetworkBandwidth + "\n");
  }

  /**
   * metoda pro uloyei statistik do arraylistu
   */
  private void saveStatistics() {
    for (final Triplet<ICache[], Long[], Long[]> cache : this.caches) {
      final String cacheName = cache.getFirst()[0].toString();
      final Long[] cacheHit = cache.getSecond();
      final Long[] savedTraffic = cache.getThird();
      final Double[] hitRatio = new Double[cacheHit.length];
      final Double[] savedTrafRatio = new Double[cacheHit.length];

      for (int i = 0; i < cacheHit.length; i++) {
        if (this.fileAccessed > 0 && this.totalNetworkBandwidth > 0){
          hitRatio[i] = (double) (((int) (cacheHit[i] * 100000 / this.fileAccessed)) / 1000.0);
          savedTrafRatio[i] = (double) ((int) (savedTraffic[i] * 100000 / this.totalNetworkBandwidth) / 1000.0);
        }
        else{
          hitRatio[i] = -1.0;
          savedTrafRatio[i] = -1.0;
        }
      }

      this.cachesResults.put(cacheName,
          new Quartet<Long[], Long[], Double[], Double[]>(
              cacheHit, savedTraffic, hitRatio, savedTrafRatio
              )
          );

    }
  }

  /**
   * metoda pro ziskani vysledku mereni
   *
   * @return hashtable s vysledky
   */
  public Hashtable<String, Quartet<Long[], Long[], Double[], Double[]>> getCachesResults() {

    if (this.fileAccessed < GlobalVariables.getLimitForStatistics()) {
      return null;
    }

    if (this.cachesResults == null || this.cachesResults.isEmpty())
      this.saveStatistics();

    return this.cachesResults;
  }

  /**
   * metoda pro vyresetovani cachovacoch algoritmu a statistik
   */
  /*public void reset() {
    this.fileAccessedHist = this.fileAccessed;
    this.fileAccessed = 0;
    this.totalNetworkBandwidthHist = totalNetworkBandwidth;
    this.totalNetworkBandwidth = 0;

    for (Triplet<ICache[], Long[], Long[]> cache : caches) {
      for (int i = 0; i < cache.getFirst().length; i++){
        cache.getFirst()[i].reset();
        cache.getSecond()[i] = (0L);
        cache.getThird()[i] = (0L);
      }
    }
  }*/
}
