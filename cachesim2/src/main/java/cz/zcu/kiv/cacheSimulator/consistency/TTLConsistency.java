package cz.zcu.kiv.cacheSimulator.consistency;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * trida pro simulaci TTL konyistentnosti
 *
 * @author Pavel Bžoch
 */
public class TTLConsistency implements IConsistencySimulation {

  /**
   * jak casto ce mame ptat na casto zapisovane soubory - v ms
   */
  private static int TTL = 20000;

  /**
   * promenna pro uchovani data o nekonzistentnim stavu
   */
  private final ArrayList<TTLConsistencyData> inconsistencyHist;


  /**
   * konstruktor - iniciace promennych
   */
  public TTLConsistency() {
    this.inconsistencyHist = new ArrayList<>();
  }


  /**
   * metoda pro ziskani datove instance podle cache a userID
   *
   * @param cache
   *          cachovaci politika
   * @param userID
   *          ID uzivatele
   * @return instance tridy BackgroundConsistencyBatchData
   */
  protected TTLConsistencyData getByCacheAndID(final ICache cache, final long userID) {
    for (final TTLConsistencyData data : this.inconsistencyHist) {
      if (data.compareTo(userID, cache))
        return data;
    }
    final TTLConsistencyData newData = new TTLConsistencyData(userID, cache);
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

    // ziskani vsech cachovanych souboru
    final List<FileOnClient> filesOnClient = cache.getCachedFiles();

    final TTLConsistencyData data = this.getByCacheAndID(cache, userID);

    this.updateTIme(data);

    // kontrola souboru na verzi
    for (final FileOnClient f : filesOnClient) {
      boolean once = true;
      if (f.getTTL() >= Long.MAX_VALUE / 2)
        continue;
      // kontrola, jestli se mame ptat na verzi
      while (f.getLastVersionCheckTime() + TTL < GlobalVariables.getActualTime()) {
        data.updateAsks();
        f.setLastVersionCheckTime(f.getLastVersionCheckTime() + TTL);
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
// System.out.println("Nekonzistence: " + fOnClient.getFileName()
// + ", verze: " + fOnClient.getVersion());
      data.updateInconsistencies();
    }
  }


  /**
   * metoda pro update casu
   *
   * @param data
   *          data, u kterych aktualizujeme cas
   */
  private void updateTIme(final TTLConsistencyData data) {
    if (data.getLastAccessTime() == -1) {
      data.setLastAccessTime(GlobalVariables.getActualTime());
    }
  }


  @Override
  public void printStat() {
    System.out.println("Statistiky pro simulaci pristupove konzistentnosti");
    for (final TTLConsistencyData data : this.inconsistencyHist) {
      System.out.println(data);
    }
  }


  @Override
  public String getInfo() {
    return "TTLConsistency;Constant TTL consistency control";
  }


  // getr a setr pro TTL
  public static int getTtl() {
    return TTLConsistency.TTL;
  }


  public static void setTtl(final int value) {
    TTLConsistency.TTL = value;
  }


  @Override
  public String[] getHeaders() {
    final String[] ret = {"Cache capacity[MB]", "Number of requests", "Number of updates",
        "Number of Inconsistencies", "Size of transferred files[MB]"};
    return ret;

  }


  @Override
  public Object[][] getData(final String cacheName, final long userID) {
    final Object[][] ret = new Object[MainGUI.getInstance().getCacheSizes().length][this
        .getHeaders().length];
    int row = 0;
    boolean isRes = false;
    for (final TTLConsistencyData data : this.inconsistencyHist) {
      if (data.getUserID() == userID && data.getCache().getClass().getName().contains(cacheName)) {
        ret[row][0] = data.getCache().getCacheCapacity() / 1024 / 1024;
        ret[row][1] = data.getAsksCount();
        ret[row][2] = data.getUpdatesCount();
        ret[row][3] = data.getInconsistenciesCount();
        ret[row][4] = data.getTransferredData() / 1024.0 / 1024.0;
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
