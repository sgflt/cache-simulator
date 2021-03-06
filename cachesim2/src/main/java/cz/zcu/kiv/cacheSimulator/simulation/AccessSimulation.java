package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_SS;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.OpenMode;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * trida pro simulaci pristupu k souborum a cachovacich algoritmu
 *
 * @author Pavel Bzoch
 */
public class AccessSimulation {

  private static final Logger LOG = LoggerFactory.getLogger(AccessSimulation.class);

  /**
   * promenna pro uchovani seznamu pristupovanych souboru
   */
  private final IFileQueue fileQueue;

  /**
   * promenna pro uchovani odkazu na server
   */
  private final Server server = Server.getInstance();

  /**
   * promenna pro uchovani uzivatelu a jejich cachovacich algoritmu
   */
  private final Hashtable<Long, SimulatedUser> userTable;

  /**
   * konstruktor - inicializace promennych
   *
   * @param fileQueue seznam prisupovanych souboru
   */
  public AccessSimulation(final IFileQueue fileQueue) {
    this.fileQueue = fileQueue;
    this.userTable = new Hashtable<>();
  }

  /**
   * metoda pro ziskani ci vytvoreni noveho uzivatele
   *
   * @param userID id uziavatele
   * @return uzivatel s cachovacimi algoritmy
   */
  private SimulatedUser getUser(final long userID) {
    return this.userTable.computeIfAbsent(userID, SimulatedUser::new);
  }

  /**
   * metoda pro spusteni simulace - pristupuje k souborum velikosti
   * pristupovanych souboru se generuji automaticky
   */
  public void simulateRandomFileSizes() {
    final Instant start = Instant.now();
    final SimulatedUser user = getUser(0);
    for (final var measurement : user.getMeasurements()) {
      Triplet<String, Long, Long> file;
      while ((file = this.fileQueue.getNextFileName()) != null) {
        // pokud na serveru soubor neexistuje, vytvorime jej s nahodnou
        // velikosti souboru
        if (!this.server.existFileOnServer(file.getFirst())) {
          this.server.generateRandomFileSize(
            file.getFirst(),
            GlobalVariables.getMinGeneratedFileSize(),
            GlobalVariables.getMaxGeneratedFileSize()
          );
        }

        // soubor je jiz v cache, aktualizujeme pouze statistiky
        measureHitRatio(file, measurement);
      }
      this.fileQueue.reset();
      this.server.softReset();
      measurement.getCache().reset();
    }

    Triplet<String, Long, Long> file;
    while ((file = this.fileQueue.getNextFileName()) != null) {
      // zvysime pocet pristupovanych souboru
      user.incereaseFileAccess();
      user.increaseTotalNetworkBandwidth(this.server.getFileSize(file.getFirst()));
    }

    LOG.info("Simulation done in {} ms", Duration.between(start, Instant.now()).toMillis());
  }

  private void measureHitRatio(final Triplet<String, Long, Long> file, final Measurement measurement) {
    final ICache cache = measurement.getCache();
    final FileOnClient fileOnClient = cache.get(file.getFirst());
    if (fileOnClient != null) {
      final Metrics metrics = measurement.getMetrics();
      metrics.incrementCacheHits();
      metrics.incrementSavedBandthwidth(fileOnClient.getFileSize());

      // statistiky na server u vsech souboru - i u tech, co
      // se pristupuji z cache
      if ((GlobalVariables.isSendStatisticsToServerLFUSS() && cache instanceof LFU_SS)
        || (GlobalVariables
        .isSendStatisticsToServerLRFUSS() && cache instanceof LRFU_SS)) {
        this.server.getFile(file.getFirst(), cache, OpenMode.READ);
      }
    } else {
// soubor neni v cache, musi se pro nej vytvorit zaznam
      cache.insertFile(new FileOnClient(this.server.getFile(file.getFirst(), cache, OpenMode.READ), cache, file.getSecond()));
    }
  }

  /**
   * metoda pro spusteni simulace - pristupuje k souborum velikosti souboru
   * jsou nacitany z logovaciho souboru
   */
  public void simulateFromLogFile() {
    // pruchod strukturou + pristupovani souboru
    Quartet<String, Long, Long, Long> file = this.fileQueue
      .getNextFileNameWithFSize();
    SimulatedUser user;
    while (file != null) {
      user = getUser(file.getFourth());
      // pokud na serveru soubor neexistuje, vytvorime jej s nactenou
      // velikosti souboru
      if (!this.server.existFileOnServer(file.getFirst())) {
        this.server.insertNewFile(file.getFirst(), file.getSecond());
      }
      // zvysime pocet pristupovanych souboru
      user.incereaseFileAccess();
      user.increaseTotalNetworkBandwidth(this.server.getFileSize(file
        .getFirst()));
      for (final var measurement : user.getMeasurements()) {
        // soubor je jiz v cache, aktualizujeme pouze statistiky
        final ICache cache = measurement.getCache();
        final FileOnClient fileOnClient = cache.get(file.getFirst());
        if (fileOnClient != null) {
          final Metrics metrics = measurement.getMetrics();
          metrics.incrementCacheHits();
          metrics.incrementSavedBandthwidth(
            cache.get(
              file.getFirst()).getFileSize()
          );
          // statistiky na server u vsech souboru - i u tech, co
          // se pristupuji z cache
          if ((GlobalVariables.isSendStatisticsToServerLFUSS() && cache instanceof LFU_SS)
            || (GlobalVariables
            .isSendStatisticsToServerLRFUSS() && cache instanceof LRFU_SS)) {
            this.server.getFile(file.getFirst(), cache, OpenMode.READ);
          }
        } else {
          // soubor neni v cache, musi se pro nej vytvorit zaznam
          cache.insertFile(new FileOnClient(this.server.getFile(file.getFirst(), cache, OpenMode.READ), cache, file.getThird()));
        }
        // pristupujeme dalsi soubor
        file = this.fileQueue.getNextFileNameWithFSize();
      }
    }
  }

  /**
   * metoda vraci vysledky vsech uzivatelu
   *
   * @return vysledky vsech uzivatelu
   */
  public List<UserStatistics> getResults() {
    final ArrayList<UserStatistics> ret = new ArrayList<>();
    for (final var user : this.userTable.values()) {
      if (user.getCachesResults() != null) {
        ret.add(new UserStatistics(user));
      }
    }
    return ret;
  }
}
