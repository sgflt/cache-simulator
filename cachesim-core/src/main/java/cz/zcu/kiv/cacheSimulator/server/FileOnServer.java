package cz.zcu.kiv.cacheSimulator.server;

import java.util.HashMap;
import java.util.Map;

/**
 * trida pro prezentaci souboru na strane serveru
 * kazdy soubor si u sebe nese informaci, kolikrat byl
 * na strane serveru pristupovan jakym cachovacim algoritmem
 *
 * @author Pavel Bzoch
 */
public class FileOnServer {

  /**
   * nazev souboru vcetne cele cesty
   */
  private final String fileName;

  /**
   * velikost souboru v Bytech
   */
  private final long fileSize;

  /**
   * promenne pro pocitani hitu (statistiky na serveru) podle cachovaciho algoritmu
   */
  private final Map<Integer, RequestCounter> readRequests = new HashMap<>();
  private final Map<Integer, RequestCounter> writeRequests = new HashMap<>();

  /**
   * konstruktor - inicializace promennych
   *
   * @param fileName
   * @param fileSize
   */
  public FileOnServer(final String fileName, final long fileSize) {
    this.fileName = fileName;
    this.fileSize = fileSize;
  }

  /**
   * metoda vrati pocet hitu na cteni souboru podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   * @return pocet hitu
   */
  public int getCountOfReadRequests(final int cacheHash) {
    return getCountOfRequests(cacheHash, this.readRequests);
  }

  /**
   * metoda navysi pocet read hitu pro dany soubor podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   */
  public void increaseReadRequestCounter(final int cacheHash) {
    increaseRequestCounter(cacheHash, this.readRequests);
  }

  /**
   * Metoda vrati pocet hitu na zapis u souboru podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   * @return pocet hitu
   */
  public int getCountOfWriteRequests(final int cacheHash) {
    return getCountOfRequests(cacheHash, this.writeRequests);
  }

  private static int getCountOfRequests(final int cacheHash, final Map<Integer, RequestCounter> hitMap) {
    return hitMap.computeIfAbsent(cacheHash, k -> new RequestCounter()).getHit();
  }

  /**
   * metoda pro zvyseni poctu hitu u zapisu pro soubor
   *
   * @param cache cachovaci algoritmus, ktery zapisoval (identifikace klienta)
   */
  public void increaseWriteRequestCounter(final int cacheHash) {
    increaseRequestCounter(cacheHash, this.writeRequests);
  }

  private static void increaseRequestCounter(final int cacheHash, final Map<Integer, RequestCounter> cacheRequestMap) {
    cacheRequestMap.compute(cacheHash, (k, v) -> {
      if (v == null) {
        return new RequestCounter();
      }

      v.increaseHit();
      return v;
    });
  }

  /**
   * getter pro ziskani jmena souboru
   *
   * @return jmeno souboru
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * getter pro ziskani velikosti souboru
   *
   * @return velikost souboru
   */
  public long getFileSize() {
    return this.fileSize;
  }

  /**
   * metoda pro vyresetovani citaci pristupu
   */
  public void resetCounters() {
    this.readRequests.clear();
    this.writeRequests.clear();
  }
}
