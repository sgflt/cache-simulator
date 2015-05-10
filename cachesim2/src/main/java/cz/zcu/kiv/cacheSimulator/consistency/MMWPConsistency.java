package cz.zcu.kiv.cacheSimulator.consistency;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * trida pro uchovani dat
 *
 * @author Pavel Bžoch
 */
public class MMWPConsistency implements IConsistencySimulation {

  /**
   * promenna pro uchovani data o nekonzistentnim stavu
   */
  private final List<MMWPConsistencyData> inconsistencyHist;


  /**
   * konstruktor - iniciace promennych
   */
  public MMWPConsistency() {
    this.inconsistencyHist = new ArrayList<MMWPConsistencyData>();
  }


  /**
   * metoda pro ziskani datove instance podle cache a userID
   *
   * @param cache
   *          cachovaci politika
   * @param userID
   *          ID uzivatele
   * @return instance tridy BackgroundConsistencySingleData
   */
  protected MMWPConsistencyData getByCacheAndID(final ICache cache, final long userID) {
    for (final MMWPConsistencyData data : this.inconsistencyHist) {
      if (data.compareTo(userID, cache))
        return data;
    }
    final MMWPConsistencyData newData = new MMWPConsistencyData(userID, cache);
    this.inconsistencyHist.add(newData);
    return newData;
  }


  @Override
  public void updateConsistencyWrite(final ICache cache, final long userID,
      final FileOnClient fOnClient, final FileOnServer fOnServer) {
    fOnClient.updateVerAndSize(fOnServer);
  }


  @Override
  public void updateActualReadFile(final ICache cache, final long userID,
      final FileOnClient fOnClient, final FileOnServer fOnServer) {
    final List<FileOnClient> filesOnClient = cache.getCachedFiles();

    final MMWPConsistencyData data = this.getByCacheAndID(cache, userID);

    // kontrola souboru na verzi
    for (final FileOnClient f : filesOnClient) {
      boolean once = true;
      if (f.getTTL() >= Long.MAX_VALUE / 2)
        continue;
      // kontrola, jestli se mame ptat na verzi
      while (f.getLastVersionCheckTime() + f.getTTL() < GlobalVariables.getActualTime()) {
        data.updateAsks();
        f.setLastVersionCheckTime(f.getLastVersionCheckTime() + 12000/*
                                                                      * f.
                                                                      * getTTL
                                                                      * (
                                                                      * )
                                                                      */);
        if (once) {
          final FileOnServer fOnSerAct = Server.getInstance().getFile(f.getFileName(), cache);
          if (fOnSerAct.getVersion() != f.getVersion()) {
            data.updateUpdates();
            f.updateVerAndSize(fOnSerAct);
            data.updateTransferredData(f.getFileSize());
          }
        }
        once = false;
      }
    }

    // kontrola prave pristupovaneho souboru
    if (fOnClient.getVersion() != fOnServer.getVersion()) {
      // System.out.println(fOnServer.getFileName() + "; ver=" +
      // fOnServer.getVersion() + "; TTL=" +fOnClient.getTTL() + "; hits="
      // + fOnServer.getWriteHits() );
      data.updateInconsistencies(fOnClient);
    }

  }


  @Override
  public void printStat() {
    System.out.println("Statistiky pro simulaci pristupove konzistentnosti");
    for (final MMWPConsistencyData data : this.inconsistencyHist) {
      System.out.println(data);
    }

  }


  @Override
  public String getInfo() {
    return "MMWPConsistency;MMWP consistency control";
  }

  /**
   * promenne pro urceni ttl v jednotlivych skupinach
   */
  private static int ttl1 = 300000, ttl2 = 90000, ttl3 = 20000, ttl4 = 20000, ttl5 = 10000;

  /**
   * promenne pro urceni poctu hitu pro zarazeni do dane skupiny
   */
  private static int hits2 = 2, hits3 = 4, hits4 = 7, hits5 = 30;


  /**
   * metoda pro nastaveni TTL pro soubor podle poctu hitu
   *
   * @param writeHits
   *          pocet hitu
   * @return cas, jak casto se mame ptat na soubor v MMPW
   */
  public static long getTTL(final long writeHits) {
    long ret = MMWPConsistency.getTtl1();
    if (writeHits > MMWPConsistency.getHits2())
      ret = MMWPConsistency.getTtl2();
    if (writeHits > MMWPConsistency.getHits3())
      ret = MMWPConsistency.getTtl3();
    if (writeHits > MMWPConsistency.getHits4())
      ret = MMWPConsistency.getTtl4();
    if (writeHits > MMWPConsistency.getHits5())
      ret = MMWPConsistency.getTtl5();
    return ret;
  }


  // sekce getru a setru pro nastaveni a ziskani statickych promennych
  public static int getTtl1() {
    return ttl1 / 1000;
  }


  public static void setTtl1(final int ttl1) {
    MMWPConsistency.ttl1 = ttl1 * 1000;
  }


  public static int getTtl2() {
    return ttl2 / 1000;
  }


  public static void setTtl2(final int ttl2) {
    MMWPConsistency.ttl2 = ttl2 * 1000;
  }


  public static int getTtl3() {
    return ttl3 / 1000;
  }


  public static void setTtl3(final int ttl3) {
    MMWPConsistency.ttl3 = ttl3 * 1000;
  }


  public static int getTtl4() {
    return ttl4 / 1000;
  }


  public static void setTtl4(final int ttl4) {
    MMWPConsistency.ttl4 = ttl4 * 1000;
  }


  public static int getTtl5() {
    return ttl5 / 1000;
  }


  public static void setTtl5(final int ttl5) {
    MMWPConsistency.ttl5 = ttl5 * 1000;
  }


  public static int getHits2() {
    return hits2;
  }


  public static void setHits2(final int hits2) {
    MMWPConsistency.hits2 = hits2;
  }


  public static int getHits3() {
    return hits3;
  }


  public static void setHits3(final int hits3) {
    MMWPConsistency.hits3 = hits3;
  }


  public static int getHits4() {
    return hits4;
  }


  public static void setHits4(final int hits4) {
    MMWPConsistency.hits4 = hits4;
  }


  public static int getHits5() {
    return hits5;
  }


  public static void setHits5(final int hits5) {
    MMWPConsistency.hits5 = hits5;
  }


  @Override
  public String[] getHeaders() {
    final String[] ret = {"Cache capacity[MB]", "Number of requests", "Number of updates",
        "No of inconsistencies (G1)", "No of inconsistencies (G2)", "No of inconsistencies (G3)",
        "No of inconsistencies (G4)", "No of inconsistencies (G5)", "Size of transferred files[MB]"};
    return ret;

  }


  @Override
  public Object[][] getData(final String cacheName, final long userID) {
    final Object[][] ret = new Object[MainGUI.getInstance().getCacheSizes().length][this
        .getHeaders().length];
    int row = 0;
    boolean isRes = false;
    for (final MMWPConsistencyData data : this.inconsistencyHist) {
      if (!data.checkInconsistrencies()) {
        JOptionPane.showMessageDialog(MainGUI.getInstance(),
            "There are inconsistencies in results!\n", "Error", JOptionPane.ERROR_MESSAGE);
      }
      if (data.getUserID() == userID && data.getCache().getClass().getName().contains(cacheName)) {
        ret[row][0] = data.getCache().getCacheCapacity() / 1024 / 1024;
        ret[row][1] = data.getAsksCount();
        ret[row][2] = data.getUpdatesCount();
        ret[row][3] = data.getInconsLeastCount();
        ret[row][4] = data.getInconsMediumThiCount();
        ret[row][5] = data.getInconsMediumSecCount();
        ret[row][6] = data.getInconsMediumFirCount();
        ret[row][7] = data.getInconsFrequentCount();
        ret[row][8] = data.getTransferredData() / 1024 / 1024.0;
        ;
        row++;
        isRes = true;
      }
    }
    if (!isRes)
      return null;
    return ret;
  }

}
