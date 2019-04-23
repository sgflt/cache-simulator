package cz.zcu.kiv.cacheSimulator.server;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.shared.OpenMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * trida pro spravu souboru na serveru
 *
 * @author Pavel Bzoch
 */
public class Server {

  /**
   * struktura pro uchovavni souboru pro bezne cachovaci algoritmy
   */
  private final Map<String, FileOnServer> fileTable;

  /**
   * promenne pro pocitani hitu (statistiky na serveru) podle cachovaciho algoritmu
   */
  private final Map<Integer, RequestCounter> readRequests = new HashMap<>();
  private final Map<Integer, RequestCounter> writeRequests = new HashMap<>();

  /**
   * promenna pro generovani nahodne velikosti souboru
   */
  private final Random rnd;

  /**
   * promenna pro ziskani instance serveru
   */
  private static Server instance = null;

  /**
   * promenna pro uchovani posledniho pristupovaneho nebo vytvoreneho souboru
   * pro rychlejsi pristup (predpoklad - naposledy pristupovany soubor bude zrejme jeste parkrat pristupovan)
   */
  private FileOnServer lastFile = null;

  /**
   * konstruktor - zaplneni struktury soubory
   */
  private Server() {
    this.fileTable = new HashMap<>(200000, 0.8f);
    this.rnd = new Random(0);
  }

  /**
   * staticky konstruktor - v programu pouzivame navrh sigleton
   *
   * @return
   */
  public static Server getInstance() {
    if (instance != null) {
      return Server.instance;
    }
    instance = new Server();
    return instance;
  }

  /**
   * metoda pro vzgenerovani nahodne velikosti souboru
   *
   * @param name    jmeno souboru
   * @param minSize minimalnio velikost
   * @param maxSize maximalni velikost
   */
  public void generateRandomFileSize(final String name, final long minSize, final long maxSize) {
    final long size;
    final FileOnServer f;
    size = Math.abs(this.rnd.nextLong()) % (maxSize - minSize) + minSize;
    f = new FileOnServer(name, size);
    this.fileTable.put(f.getFileName(), f);
    this.lastFile = f;
  }

  /**
   * metoda pro vlozeni noveho souboru na server (z logovaciho souboru)
   *
   * @param name jmeno souboru
   * @param size velikost souboru
   */
  public void insertNewFile(final String name, final long size) {
    final FileOnServer f = new FileOnServer(name, size);
    this.fileTable.put(f.getFileName(), f);
    this.lastFile = f;
  }

  /**
   * metoda pro zjisteni, zda soubor existuje na serveru
   *
   * @param name jmeno souboru
   * @return true, pokud existuje soubor
   */
  public boolean existFileOnServer(final String name) {
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(name)) {
      return true;
    }
    return this.fileTable.containsKey(name);
  }

  /**
   * metoda pro ziskani souboru na cteni
   *
   * @param fileName jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFile(final String fileName, final ICache cache, final OpenMode openMode) {
    if (this.fileTable.isEmpty()) {
      return null;
    }
    final FileOnServer fileOnServer;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fileName)) {
      fileOnServer = this.lastFile;
    } else {
      fileOnServer = this.fileTable.get(fileName);
    }
    if (fileOnServer == null) {
      return null;
    }
    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (cache.needServerStatistics()) {
      switch (openMode) {
        case READ:
          increaseGlobalReadRequestCounter(cache.hashCode());
          fileOnServer.increaseReadRequestCounter(cache.hashCode());
          break;
        case WRITE:
          increaseGlobalWriteRequestCounter(cache.hashCode());
          fileOnServer.increaseWriteRequestCounter(cache.hashCode());
          break;
        case READ_WRITE:
          increaseGlobalReadRequestCounter(cache.hashCode());
          increaseGlobalWriteRequestCounter(cache.hashCode());
          fileOnServer.increaseReadRequestCounter(cache.hashCode());
          fileOnServer.increaseWriteRequestCounter(cache.hashCode());
          break;
        default:
          throw new UnsupportedOperationException("Unknown open mode " + openMode);
      }
    }
    this.lastFile = fileOnServer;
    return fileOnServer;
  }

  /**
   * metoda pro ziskani globalniho poctu hitu pro vsechny soubory
   *
   * @return pocet hitu
   */
  public synchronized long getGlobalReadRequests(final ICache cache) {
    return this.readRequests.computeIfAbsent(cache.hashCode(), k -> new RequestCounter()).getHit();
  }

  /**
   * metoda vrati velikost souboru pro dane jmeno souboru
   *
   * @param fname jmeno souboru
   * @return velikost souboru
   */
  public long getFileSize(final String fname) {
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)) {
      return this.lastFile.getFileSize();
    } else {
      final FileOnServer file = this.fileTable.get(fname);
      if (file != null) {
        this.lastFile = file;
        return file.getFileSize();
      } else {
        System.out.println("Chyba!");
        return 0;
      }
    }
  }

  /**
   * metoda, ktera odstrani ze serveru vsechny souboru (je nasledne nutne soubory opet vytvorit pred dalsi simulaci)
   */
  public void hardReset() {
    this.fileTable.clear();
  }

  /**
   * metoda, ktera odtrani ze serveru statistiky u vsechn souboru
   */
  public void softReset() {
    this.readRequests.clear();
    this.writeRequests.clear();
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
  public void increaseGlobalReadRequestCounter(final int cacheHash) {
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
  public void increaseGlobalWriteRequestCounter(final int cacheHash) {
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
}
