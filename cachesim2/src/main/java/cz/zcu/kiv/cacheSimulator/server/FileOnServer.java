package cz.zcu.kiv.cacheSimulator.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

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
  private long fileSize;

  /**
   * promenna pro uchovani verze souboru (pri kazdem uplaodu se verze zvetsi)
   */
  private int version;

  /**
   * promenne pro pocitani hitu (statistiky na serveru) podle cachovaciho algoritmu
   */
  private final Map<ICache, Integer> readHit, writeHit;

  /**
   * seznam casu, kdy byl soubor pristupovan
   */
  private List<Long> writeTimes = null;

  /**
   * promenna pro uchovani, jak casto doslo k zapisu (aktualizace jen po nacteni vsech souboru)
   */
  private long writeHits = 0;

  /**
   * promenna pro urceni, jestli byl soubor odposledniho zapisu cten
   */
  private boolean wasReadSinceLastWrite = true;

  /**
   * promenna pro uchovani, jak casto se soubor zapisoval
   */
  private long accessTimePeriod = Long.MAX_VALUE;

  /**
   * pokud probiha prefatching, uklada se sem pocet cteni souboru
   */
  private int numberOfRead = 1;


  /**
   * konstruktor - inicializace promennych
   *
   * @param fileName
   * @param fileSize
   */
  public FileOnServer(final String fileName, final long fileSize) {
    super();
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.writeTimes = new ArrayList<>();
    this.version = 1;
    this.readHit = new ConcurrentHashMap<>();
    this.writeHit = new ConcurrentHashMap<>();
  }


  /**
   * metoda vrati pocet hitu na cteni souboru podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   * @return pocet hitu
   */
  public int getReadHit(final ICache cache) {
    final Integer readHits = this.readHit.get(cache);
    return readHits != null ? readHits : 0;
  }


  /**
   * metoda navysi pocet read hitu pro dany soubor podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   */
  public void increaseReadHit(final ICache cache) {
    final Integer readHits = this.readHit.get(cache);
    this.readHit.put(cache, readHits != null ? readHits + 1 : this.numberOfRead);
    this.wasReadSinceLastWrite = true;
  }


  /**
   * Metoda vrati pocet hitu na zapis u souboru podle cachovaciho algoritmu
   *
   * @param cache cachovaci algoritmus
   * @return pocet hitu
   */
  public int getWriteHit(final ICache cache) {
    final Integer writeHits = this.writeHit.get(cache);
    return writeHits != null ? writeHits : 0;
  }


  /**
   * metoda pro zvyseni poctu hitu u zapisu pro soubor
   *
   * @param cache cachovaci algoritmus, ktery zapisoval (identifikace klienta)
   */
  public void increaseWriteHit(final ICache cache) {
    final Integer writeHits = this.writeHit.get(cache);
    this.writeHit.put(cache, writeHits != null ? writeHits + 1 : 1);
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
    this.numberOfRead = 1;
    this.readHit.clear();
    this.writeHit.clear();
    this.writeTimes.clear();
  }


  /**
   * metoda pro vraceni verze souboru
   *
   * @return verze souboru
   */
  public int getVersion() {
    return this.version;
  }


  /**
   * metoda pro zvetseni verze souboru a ulozeni nove velikosti souboru
   *
   * @param fSize nova velikost souboru
   */
  public void updateFile(final long fSize) {
    this.version++;
    this.fileSize = fSize;

    if (this.writeTimes.size() == 0)
      this.writeTimes.add(GlobalVariables.getActualTime());
    else {
      if (this.writeTimes.get(this.writeTimes.size() - 1) != GlobalVariables.getActualTime())
        this.writeTimes.add(GlobalVariables.getActualTime());
    }
    this.wasReadSinceLastWrite = false;
  }


  /**
   * metoda pro update souboru bez aktualizace verze
   *
   * @param fSize nova velikost souboru
   */
  public void updateFileToVer(final long fSize) {
    this.fileSize = fSize;

// if (writeTimes.size() == 0)
// this.writeTimes.add(GlobalVariables.getActualTime());
// else{
// if (this.writeTimes.get(writeTimes.size() - 1) != GlobalVariables.getActualTime())
    this.writeTimes.add(GlobalVariables.getActualTime());
// }
    this.wasReadSinceLastWrite = false;
  }


  /**
   * metoda pro vypocet, jak casto byl soubor pristupovan v ramci sledovaneho casu
   *
   * @return perioda casu
   */
  public long writePeriod() {
    /*
     * if (RequstedFile.getAddTime() <= 0)
     * return Long.MAX_VALUE;
     * return RequstedFile.getAddTime() / version;
     */
    return this.accessTimePeriod;
  }


  /**
   * metoda vraci cas, kdy byl soubor naposledy zapisovan
   *
   * @return posledni cas zapisu
   */
  public long getLastWriteTime() {
    if (this.writeTimes.size() == 0)
      return -1;
    return this.writeTimes.get(this.writeTimes.size() - 1);
  }


  /**
   * metoda pro zjiosteni, jestli byl soubor cten od posledniho zapisu
   *
   * @return true, pokud byl cten od posledniho zapisu
   */
  public boolean isWasReadSinceLastWrite() {
    return this.wasReadSinceLastWrite;
  }


  /**
   * metod apro nastaveni, ze byl soubor cten od posledniho zapisu
   */
  public void setWasReadSinceLastWrite() {
    this.wasReadSinceLastWrite = true;
  }


  @Override
  public String toString() {
    long sumTime = 0;
    long nejmensi = Long.MAX_VALUE, nejvetsi = Long.MIN_VALUE;
    long time;

    for (int i = 0; i < this.writeTimes.size() - 1; i++) {
      time = this.writeTimes.get(i + 1) - this.writeTimes.get(i);
      sumTime += time;
      if (nejmensi > time)
        nejmensi = time;
      if (nejvetsi < time)
        nejvetsi = time;
    }

    if (this.writeTimes.size() > 1) {
      return "FileOnServer [Version=;" + this.version + "; prumernyCasPristupu=;" + sumTime
          / (this.writeTimes.size() - 1) + "; nejmensiPerioda=;" + nejmensi + "; nejvetsiPerioda=;"
          + nejvetsi + "; fileName=;" + this.fileName + "]";
    }

    return "FileOnServer [Version=" + this.version + ", fileName=" + this.fileName + ", size=" + this.fileSize + "]";
  }


  /**
   * metoda, ktera se postara o ulozeni casu, jak casto je soubor pristupovan
   */
  public void storeTimes() {
    this.accessTimePeriod = 6000000L;

    long sumtimes = 0;
    int pocet = 1;
    for (int i = 0; i < this.writeTimes.size() - 1; i++) {
      pocet++;
      sumtimes += this.writeTimes.get(i + 1) - this.writeTimes.get(i);
    }
    if (sumtimes > 1)
      this.accessTimePeriod = sumtimes / pocet;
// if (version > 1)
// this.accessTimePeriod = 105000L;
// if (version > 3)
// this.accessTimePeriod = 35000L;
//
// if (version > 2 && pocet > 2)
// System.out.println(fileName + "; version=;" + version + "; updateTimes=;" + pocet +
// "; averageTime=;" + sumtimes / pocet + "; accessPeriod=;"+accessTimePeriod );

  }


  // metoday pro ziskani a nastaveneni write hits
  public long getWriteHits() {
    return this.writeHits;
  }


  public void setWriteHits(final long writeHits) {
    this.writeHits = writeHits;
  }


  /**
   * metoda pro zvyseni poctu cteni souboru pri prefatchingu
   */
  public void increaseReadHitPrefatching() {
    this.numberOfRead++;
  }

}
