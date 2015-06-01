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

public class MMWPBatchConsistency implements IConsistencySimulation {

  /**
   * jak casto ce mame ptat na casto zapisovane soubory
   */
  private static final int FrequentTimePeriod = MMWPConsistency.getTtl5() * 1000;

  /**
   * jak casto se budeme ptat na soubory s druhy nejvice zapisy
   */
  private static final int MediumTimePeriodFirst = MMWPConsistency.getTtl4() * 1000;

  /**
   * jak casto se budeme ptat na soubory s treti nejvice zapisy
   */
  private static final int MediumTimePeriodSecond = MMWPConsistency.getTtl3() * 1000;

  /**
   * jak casto se budeme ptat na soubory s ctvrty nejvice zapisy
   */
  private static final int MediumTimePeriodThird = MMWPConsistency.getTtl2() * 1000;

  /**
   * jak casto se mame ptat na soubory, ktere byly zapisovany nejmene
   */
  private static final int LeastFreqTimePeriod = MMWPConsistency.getTtl1() * 1000;

  /**
   * vymezeni verzi pro nejcastejci ptani
   */
  private static final int groupG5hits = MMWPConsistency.getHits5();

  /**
   * vymezeni verzi pro druhe nejcastejci ptani
   */
  private static final int groupG4hits = MMWPConsistency.getHits4();

  /**
   * vymezeni verzi pro treti nejcastejci ptani
   */
  private static final int groupG3hits = MMWPConsistency.getHits3();

  /**
   * vymezeni verzi pro ctvrte nejcastejci ptani
   */
  private static final int groupG2hits = MMWPConsistency.getHits2();

  /**
   * promenna pro uchovani data o nekonzistentnim stavu
   */
  private final ArrayList<MMWPBatchConsistencyData> inconsistencyHist;

  /**
   * konstruktor - iniciace promennych
   */
  public MMWPBatchConsistency() {
    this.inconsistencyHist = new ArrayList<>();
  }

  /**
   * metoda pro ziskani datove instance podle cache a userID
   *
   * @param cache
   *            cachovaci politika
   * @param userID
   *            ID uzivatele
   * @return instance tridy BackgroundConsistencyBatchData
   */
  protected MMWPBatchConsistencyData getByCacheAndID(final ICache cache, final long userID) {
    for (final MMWPBatchConsistencyData data : this.inconsistencyHist) {
      if (data.compareTo(userID, cache))
        return data;
    }
    final MMWPBatchConsistencyData newData = new MMWPBatchConsistencyData(userID,
        cache);
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

    final MMWPBatchConsistencyData data = this.getByCacheAndID(cache, userID);

    // kontrola frequent period time
    this.checkFrequentPeriodTime(filesOnClient, data, cache);

    // kontrola medium period time
    this.checkMediumFirstPeriodTime(filesOnClient, data, cache);

    // kontrola medium period time
    this.checkMediumSecondPeriodTime(filesOnClient, data, cache);

    // kontrola medium period time
    this.checkMediumThirdPeriodTime(filesOnClient, data, cache);

    // kontrola least period time
    this.checkLeastPeriodTime(filesOnClient, data, cache);

    // kontrola prave pristupovaneho souboru
    if (fOnClient.getVersion() != fOnServer.getVersion()) {
      // System.out.println("Nekonzistence: " + fOnClient.getFileName()
      // + ", verze: " + fOnClient.getVersion());
      data.updateInconsistencies(fOnClient);
    }

  }

  private void checkMediumThirdPeriodTime(final List<FileOnClient> filesOnClient,
      final MMWPBatchConsistencyData data, final ICache cache) {
    this.updateTIme(data);
    boolean once = true;
    boolean updates = false;
    // kontrola probiha pouze pokud jsme v danem casovem intervalu
    while (data.getLastAccessTimeMediumThird() + MediumTimePeriodThird < GlobalVariables
        .getActualTime()) {
      data.setLastAccessTimeMediumThird(data
          .getLastAccessTimeMediumThird() + MediumTimePeriodThird);
      data.updateMediumThirdAsk();
      // kontrola souboru na verzi
      if (once)
        for (final FileOnClient f : filesOnClient) {
          // kontrola, jestli se mame ptat na verzi
          if (f.getVersion() < groupG3hits
              && f.getVersion() >= groupG2hits) {
            final FileOnServer fOnSerAct = Server.getInstance().getFile(
                f.getFileName()/*XXX, cache*/);
            if (fOnSerAct.getVersion() != f.getVersion()) {
              data.updateUpdates();
              f.updateVerAndSize(fOnSerAct);
              data.updateTraffic(fOnSerAct.getFileSize());
              updates = true;
            }
          }
        }
      if (updates)
        data.updateLeastFreqAsks();
      once = false;
    }

  }

  /**
   * metoda pro kontrolu konzistenci stredne casto zapisovanych souboru pri
   * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
   * jednotlive
   *
   * @param filesOnClient
   *            soubory, ktere jsou v cache
   * @param data
   *            data o konzistentnosti
   * @param cache
   *            reference na cachovaci algoritmus, odkud pochazi soubor
   */

  private void checkMediumSecondPeriodTime(final List<FileOnClient> filesOnClient,
      final MMWPBatchConsistencyData data, final ICache cache) {
    this.updateTIme(data);
    boolean once = true;
    boolean updates = false;
    // kontrola probiha pouze pokud jsme v danem casovem intervalu
    while (data.getLastAccessTimeMediumSecond() + MediumTimePeriodSecond < GlobalVariables
        .getActualTime()) {
      data.setLastAccessTimeMediumSecond(data
          .getLastAccessTimeMediumSecond() + MediumTimePeriodSecond);
      data.updateMediumSecAsks();

      // kontrola souboru na verzi
      if (once)
        for (final FileOnClient f : filesOnClient) {
          // kontrola, jestli se mame ptat na verzi
          if (f.getVersion() < groupG4hits
              && f.getVersion() >= groupG3hits) {
            final FileOnServer fOnSerAct = Server.getInstance().getFile(
                f.getFileName()/*, cache*/);
            if (fOnSerAct.getVersion() != f.getVersion()) {
              data.updateUpdates();
              f.updateVerAndSize(fOnSerAct);
              data.updateTraffic(fOnSerAct.getFileSize());
              updates = true;
            }
          }
        }
      if (updates)
        data.updateLeastFreqAsks();
      once = false;
    }
  }

  /**
   * metoda pro kontrolu konzistenci stredne casto zapisovanych souboru pri
   * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
   * jednotlive
   *
   * @param filesOnClient
   *            soubory, ktere jsou v cache
   * @param data
   *            data o konzistentnosti
   * @param cache
   *            reference na cachovaci algoritmus, odkud pochazi soubor
   */

  private void checkMediumFirstPeriodTime(final List<FileOnClient> filesOnClient,
      final MMWPBatchConsistencyData data, final ICache cache) {
    this.updateTIme(data);
    boolean once = true;
    boolean updates = false;
    // kontrola probiha pouze pokud jsme v danem casovem intervalu
    while (data.getLastAccessTimeMediumFirst() + MediumTimePeriodFirst < GlobalVariables
        .getActualTime()) {
      data.setLastAccessTimeMediumFirst(data
          .getLastAccessTimeMediumFirst() + MediumTimePeriodFirst);
      data.updateMediumFirstAsk();

      // kontrola souboru na verzi
      if (once)
        for (final FileOnClient f : filesOnClient) {
          // kontrola, jestli se mame ptat na verzi
          if (f.getVersion() < groupG5hits
              && f.getVersion() >= groupG4hits) {
            final FileOnServer fOnSerAct = Server.getInstance().getFile(
                f.getFileName()/*, cache*/);
            if (fOnSerAct.getVersion() != f.getVersion()) {
              data.updateUpdates();
              f.updateVerAndSize(fOnSerAct);
              data.updateTraffic(fOnSerAct.getFileSize());
              updates = true;
            }
          }
        }
      if (updates)
        data.updateLeastFreqAsks();
      once = false;
    }

  }

  /**
   * metoda pro kontrolu konzistenci nejmene casto zapisovanych souboru pri
   * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
   * jednotlive
   *
   * @param filesOnClient
   *            soubory, ktere jsou v cache
   * @param data
   *            data o konzistentnosti
   * @param cache
   *            reference na cachovaci algoritmus, odkud pochazi soubor
   */
  private void checkLeastPeriodTime(final List<FileOnClient> filesOnClient,
      final MMWPBatchConsistencyData data, final ICache cache) {
    this.updateTIme(data);
    boolean once = true;
    boolean updates = false;
    // kontrola probiha pouze pokud jsme v danem casovem intervalu
    while (data.getLastAccessTimeLeast() + LeastFreqTimePeriod < GlobalVariables
        .getActualTime()) {
      data.setLastAccessTimeLeast(data.getLastAccessTimeLeast()
          + LeastFreqTimePeriod);

      // kontrola souboru na verzi
      if (once)
        for (final FileOnClient f : filesOnClient) {
          // kontrola, jestli se mame ptat na verzi
          if (f.getVersion() < groupG2hits) {
            final FileOnServer fOnSerAct = Server.getInstance().getFile(
                f.getFileName()/*, cache*/);
            if (fOnSerAct.getVersion() != f.getVersion()) {
              data.updateUpdates();
              f.updateVerAndSize(fOnSerAct);
              data.updateTraffic(fOnSerAct.getFileSize());
              updates = true;
            }
          }
        }
      if (updates)
        data.updateLeastFreqAsks();
      once = false;
    }
  }

  /**
   * metoda pro kontrolu konzistenci nejcasteji zapisovanych souboru pri
   * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
   * jednotlive
   *
   * @param filesOnClient
   *            soubory, ktere jsou v cache
   * @param data
   *            data o konzistentnosti
   * @param cache
   *            reference na cachovaci algoritmus, odkud pochazi soubor
   */
  private void checkFrequentPeriodTime(final List<FileOnClient> filesOnClient,
      final MMWPBatchConsistencyData data, final ICache cache) {
    this.updateTIme(data);
    boolean once = true;
    boolean updates = false;
    // kontrola probiha pouze pokud jsme v danem casovem intervalu
    while (data.getLastAccessTimeFrequent() + FrequentTimePeriod < GlobalVariables
        .getActualTime()) {
      data.setLastAccessTimeFrequent(data.getLastAccessTimeFrequent()
          + FrequentTimePeriod);
      data.updateFreqAsks();

      // kontrola souboru na verzi
      if (once)
        for (final FileOnClient f : filesOnClient) {
          // kontrola, jestli se mame ptat na verzi
          if (f.getVersion() >= groupG5hits) {
            final FileOnServer fOnSerAct = Server.getInstance().getFile(
                f.getFileName()/*XXX, cache*/);
            if (fOnSerAct.getVersion() != f.getVersion()) {
              data.updateUpdates();
              f.updateVerAndSize(fOnSerAct);
              data.updateTraffic(fOnSerAct.getFileSize());
              updates = true;
            }
          }
        }
      if (updates)
        data.updateLeastFreqAsks();
      once = false;
    }
  }

  /**
   * metoda pro update casu
   *
   * @param data
   *            data, u kterych aktualizujeme cas
   */
  private void updateTIme(final MMWPBatchConsistencyData data) {
    if (data.getLastAccessTimeFrequent() == -1) {
      data.setLastAccessTimeFrequent(GlobalVariables.getActualTime());
    }
    if (data.getLastAccessTimeLeast() == -1) {
      data.setLastAccessTimeLeast(GlobalVariables.getActualTime());
    }
    if (data.getLastAccessTimeMediumSecond() == -1) {
      data.setLastAccessTimeMediumSecond(GlobalVariables.getActualTime());
    }
    if (data.getLastAccessTimeMediumFirst() == -1) {
      data.setLastAccessTimeMediumFirst(GlobalVariables.getActualTime());
    }
    if (data.getLastAccessTimeMediumThird() == -1) {
      data.setLastAccessTimeMediumThird(GlobalVariables.getActualTime());
    }

  }

  @Override
  public void printStat() {
    System.out
        .println("Statistiky pro simulaci pristupove konzistentnosti");
    System.out.println("T1=" + FrequentTimePeriod / 1000 + "s; T2="
        + MediumTimePeriodFirst / 1000 + "s; T3="
        + MediumTimePeriodSecond / 1000 + "s; T4="
        + MediumTimePeriodThird / 1000 + "s; T5=" + LeastFreqTimePeriod
        / 1000 + "s");
    for (final MMWPBatchConsistencyData data : this.inconsistencyHist) {
      System.out.println(data);
    }

  }

  @Override
  public String getInfo() {
    return "MMWPBatchConsistency;MMWP batch consistency control";
  }

  @Override
  public String[] getHeaders() {
    final String[] ret = { "Cache capacity[MB]", "No of requests (G1)",
        "No of inconsistencies (G1)", "No of requests (G2)",
        "No of inconsistencies (G2)", "No of requests (G3)",
        "No of inconsistencies (G3)", "No of requests (G4)",
        "No of inconsistencies (G4)", "No of requests (G5)",
        "No of inconsistencies (G5)", "No of updates",
        "Size of transferred files[MB]" };
    return ret;

  }

  @Override
  public Object[][] getData(final String cacheName, final long userID) {
    final Object[][] ret = new Object[MainGUI.getInstance().getCacheSizes().length][this.getHeaders().length];
    int row = 0;
    boolean isRes = false;
    for (final MMWPBatchConsistencyData data : this.inconsistencyHist) {
      if (!data.checkInconsistrencies()){
        JOptionPane.showMessageDialog(MainGUI.getInstance(),
            "There are inconsistencies in results!\n", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
      if (data.getUserID() == userID
          && data.getCache().getClass().getName().contains(cacheName)) {
        ret[row][0] = data.getCache().getCapacity() / 1024 / 1024;

        ret[row][1] = data.getNoOfLeastFreqAsk();
        ret[row][2] = data.getInconsLeastCount();

        ret[row][3] = data.getNoOfMediumThiAsk();
        ret[row][4] = data.getInconsMediumThiCount();

        ret[row][5] = data.getNoOfMediumSecAsk();
        ret[row][6] = data.getInconsMediumSecCount();

        ret[row][7] = data.getNoOfMediumFirAsk();
        ret[row][8] = data.getInconsMediumFirCount();

        ret[row][9] = data.getNoOfFreqAsk();
        ret[row][10] = data.getInconsFrequentCount();

        ret[row][11] = data.getNoOfUpdates();
        ret[row][12] = data.getNetTraffic() / 1024.0 / 1024.0;

        row++;
        isRes = true;
      }
    }
    if (!isRes)
      return null;
    return ret;
  }

}
