package cz.zcu.kiv.cacheSimulator.server;

import java.util.Hashtable;

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
  private final Hashtable<Integer, Integer> readHit;
  private final Hashtable<Integer, Integer> writeHit;

  /**
   * konstruktor - inicializace promennych
   *
   * @param fileName
   * @param fileSize
   */
  public FileOnServer(final String fileName, final long fileSize) {
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.readHit = new Hashtable<>();
    this.writeHit = new Hashtable<>();
  }

  /**
   * metoda vrati pocet hitu na cteni souboru podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   * @return pocet hitu
   */
  public int getReadHit(final int cacheHash) {
    if (!this.readHit.containsKey(cacheHash)) {
      return 0;
    }
    return this.readHit.get(cacheHash);
  }

  /**
   * metoda navysi pocet read hitu pro dany soubor podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   */
  public void increaseReadHit(final int cacheHash) {
    if (!this.readHit.containsKey(cacheHash)) {
      this.readHit.put(cacheHash, 1);
    } else {
      this.readHit.put(cacheHash, this.readHit.get(cacheHash).intValue() + 1);
    }
  }

  /**
   * Metoda vrati pocet hitu na zapis u souboru podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   * @return pocet hitu
   */
  public int getWriteHit(final int cacheHash) {
    if (!this.writeHit.containsKey(cacheHash)) {
      return 0;
    }
    return this.writeHit.get(cacheHash);
  }

  /**
   * metoda pro zvyseni poctu hitu u zapisu pro soubor
   *
   * @param cache cachovaci algoritmus, ktery zapisoval (identifikace klienta)
   */
  public void increaseWriteHit(final int cacheHash) {
    if (!this.writeHit.containsKey(cacheHash)) {
      this.writeHit.put(cacheHash, 1);
    } else {
      this.writeHit.put(cacheHash, this.writeHit.get(cacheHash) + 1);
    }
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
    this.readHit.clear();
    this.writeHit.clear();
  }
}
