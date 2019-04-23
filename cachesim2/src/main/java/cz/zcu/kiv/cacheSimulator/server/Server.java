package cz.zcu.kiv.cacheSimulator.server;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;

import java.util.ArrayList;
import java.util.Hashtable;
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
  private final Hashtable<String, FileOnServer> fileTable;

  /**
   * struktura pro ulozeni nazvu souboru
   */
  private final ArrayList<String> fileNames;

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
    this.fileTable = new Hashtable<>(200000, 0.8f);
    this.fileNames = new ArrayList<>();
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
    if (this.fileTable == null) {
      return false;
    }
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(name)) {
      return true;
    }
    return this.fileTable.containsKey(name);
  }

  /**
   * metoda pro ziskani souboru na cteni
   *
   * @param fname jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFileRead(final String fname, final ICache cache) {
    if (this.fileTable == null || this.fileTable.isEmpty()) {
      return null;
    }
    final FileOnServer ret;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)) {
      ret = this.lastFile;
    } else {
      ret = this.fileTable.get(fname);
    }
    if (ret == null) {
      return null;
    }
    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (cache.needServerStatistics()) {
      ret.increaseReadHit(cache.hashCode());
    }
    this.lastFile = ret;
    return ret;
  }

  /**
   * metoda pro ziskani souboru na zapis
   *
   * @param fname jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFileWrite(final String fname, final ICache cache) {
    if (this.fileTable == null || this.fileTable.isEmpty()) {
      return null;
    }
    final FileOnServer ret;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)) {
      ret = this.lastFile;
    } else {
      ret = this.fileTable.get(fname);
    }
    if (ret == null) {
      return null;
    }
    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (cache.needServerStatistics()) {
      ret.increaseWriteHit(cache.hashCode());
    }
    this.lastFile = ret;
    return ret;
  }

  /**
   * metoda pro ziskani souboru na zapis i cteni
   *
   * @param fname jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFileReadWrite(final String fname, final ICache cache) {
    if (this.fileTable == null || this.fileTable.isEmpty()) {
      return null;
    }
    final FileOnServer ret;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)) {
      ret = this.lastFile;
    } else {
      ret = this.fileTable.get(fname);
    }
    if (ret == null) {
      return null;
    }
    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (cache.needServerStatistics()) {
      ret.increaseWriteHit(cache.hashCode());
      ret.increaseReadHit(cache.hashCode());
    }
    this.lastFile = ret;
    return ret;
  }

  /**
   * metoda pro ziskani globalniho poctu hitu pro vsechny soubory
   *
   * @return pocet hitu
   */
  public synchronized long getGlobalReadHits(final ICache cache) {
    long ret = 0;
    for (final FileOnServer f : this.fileTable.values()) {
      ret += f.getReadHit(cache.hashCode());
    }
    return ret;
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
   * metoda pro ziskani nazvu souboru
   *
   * @return nazvu souboru
   */
  public ArrayList<String> getFileNames() {
    //preklopeni nazvu souboru do listu pro pozdejsi praci s nimi
    this.fileNames.clear();
    for (final FileOnServer files : this.fileTable.values()) {
      this.fileNames.add(files.getFileName());
    }
    return this.fileNames;
  }

  /**
   * metoda pro vytisteni statistik o pristupu k jednotlivym souborum
   */
  public void printStatistics(final ICache cache) {
    System.out.println("\nFile accesses:");
    for (final FileOnServer fos : this.fileTable.values()) {
      System.out.println(fos.getFileName() + ";" + fos.getReadHit(cache.hashCode()) + ";" + fos.getFileSize());
    }
  }

  /**
   * metoda, ktera odstrani ze serveru vsechny souboru (je nasledne nutne soubory opet vytvorit pred dalsi simulaci)
   */
  public void hardReset() {
    this.fileNames.clear();
    this.fileTable.clear();
  }

  /**
   * metoda, ktera odtrani ze serveru statistiky u vsechn souboru
   */
  public void softReset() {
    for (final FileOnServer file : this.fileTable.values()) {
      file.resetCounters();
    }
  }
}
