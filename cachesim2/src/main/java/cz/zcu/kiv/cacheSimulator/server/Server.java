package cz.zcu.kiv.cacheSimulator.server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequestedFile;



/**
 * trida pro spravu souboru na serveru
 *
 * @author Pavel Bzoch
 *
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
  private Random rnd;

  /**
   * promenna pro uchovani seed value pro random generator
   */
  private static int seedValue = 0;

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
    this.fileTable = new Hashtable<String, FileOnServer>(200000, 0.8f);
    this.fileNames = new ArrayList<String>();
    this.rnd = new Random(seedValue);
  }

  /**
   * staticky konstruktor - v programu pouzivame navrh sigleton
   * @return
   */
  public static Server getInstance(){
    if (instance != null)
      return Server.instance;
    instance = new Server();
    return instance;
  }

  /**
   * metoda pro vzgenerovani nahodne velikosti souboru
   * @param name jmeno souboru
   * @param minSize minimalnio velikost
   * @param maxSize maximalni velikost
   * @return nove vytvoreny soubor
   */
  public FileOnServer generateRandomFileSize(final String name, final long minSize, final long maxSize){
    long size;
    FileOnServer f;
    size = Math.abs(this.rnd.nextLong()) % (maxSize - minSize) + minSize;
    f = new FileOnServer(name, size);
    this.fileTable.put(f.getFileName(), f);
    this.lastFile = f;
    return f;
  }

  /**
   * metoda pro vlozeni noveho souboru na server (z logovaciho souboru)
   * @param name jmeno souboru
   * @param size velikost souboru
   * @return nove vytvoreny soubor
   */
  public FileOnServer insertNewFile(final String name, final long size){
    final FileOnServer f = new FileOnServer(name, size);
    this.fileTable.put(f.getFileName(), f);
    this.lastFile = f;
    return f;
  }

  /**
   * metoda pro zjisteni, zda soubor existuje na serveru
   * @param name jmeno souboru
   * @return soubor, pokud existuje, jinak null
   */
  public synchronized FileOnServer existFileOnServer(final String name){
    if (this.fileTable == null) return null;
    if (this.lastFile  != null && this.lastFile.getFileName().equalsIgnoreCase(name))
      return this.lastFile;
    return this.fileTable.get(name);
  }

  /**
   * metoda pro ziskani souboru na cteni
   *
   * @param fname
   *            jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFileRead(final String fname, final ICache cache) {
    if (this.fileTable == null || this.fileTable.isEmpty())
      return null;
    FileOnServer ret;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)){
      ret = this.lastFile;
    }
    else ret = this.fileTable.get(fname);
    if (ret == null)
      return null;
    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (cache.needServerStatistics()){
      ret.increaseReadHit(cache.hashCode());
    }
    this.lastFile = ret;
    return ret;
  }

  /**
   * metoda pro ziskani souboru na zapis
   *
   * @param fname
   *            jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFileWrite(final String fname, final ICache cache) {
    if (this.fileTable == null || this.fileTable.isEmpty())
      return null;
    FileOnServer ret;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)){
      ret = this.lastFile;
    }
    else ret = this.fileTable.get(fname);
    if (ret == null)
      return null;
    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (cache.needServerStatistics()){
      ret.increaseWriteHit(cache.hashCode());
    }
    this.lastFile = ret;
    return ret;
  }

  /**
   * metoda pro ziskani souboru na zapis i cteni
   *
   * @param fname
   *            jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFileReadWrite(final String fname, final ICache cache) {
    if (this.fileTable == null || this.fileTable.isEmpty())
      return null;
    FileOnServer ret;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)){
      ret = this.lastFile;
    }
    else ret = this.fileTable.get(fname);
    if (ret == null)
      return null;
    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (cache.needServerStatistics()){
      ret.increaseWriteHit(cache.hashCode());
      ret.increaseReadHit(cache.hashCode());
    }
    this.lastFile = ret;
    return ret;
  }

  /**
   * metoda pro ziskani souboru
   *
   * @param fname
   *            jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public synchronized FileOnServer getFile(final String fname, final ICache cache) {
    if (this.fileTable == null || this.fileTable.isEmpty())
      return null;
    FileOnServer ret;
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname)){
      ret = this.lastFile;
    }
    else ret = this.fileTable.get(fname);
    if (ret == null)
      return null;
    return ret;
  }

  /**
   * metoda pro ziskani globalniho poctu hitu pro vsechny soubory
   *
   * @return pocet hitu
   */
  public synchronized long getGlobalReadHits(final ICache cache) {
    long ret = 0;
    for (final FileOnServer f: this.fileTable.values())
    {
      ret += f.getReadHit(cache.hashCode());
    }
    return ret;
  }

  /**
   * metoda vrati velikost souboru pro dane jmeno souboru
   * @param fname jmeno souboru
   * @return velikost souboru
   */
  public synchronized long getFileSize(final String fname){
    if (this.lastFile != null && this.lastFile.getFileName().equalsIgnoreCase(fname))
      return this.lastFile.getFileSize();
    else{
      final FileOnServer file = this.fileTable.get(fname);
      if (file != null){
        this.lastFile = file;
        return file.getFileSize();
      }
      else{
        System.out.println("Chyba!");
        return 0;
      }
    }
  }

  /**
   * metoda pro ziskani nazvu souboru
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
  public void printStatistics(final ICache cache){
    System.out.println("\nFile accesses:");
    for(final FileOnServer fos: this.fileTable.values()){
      System.out.println(fos.getFileName() + "; readHit=" + fos.getReadHit(cache.hashCode()) + "; size=" + fos.getFileSize() + "; period=" + fos.writePeriod());
    }
  }

  /**
   * metoda pro vytisteni statistik o pristupu k jednotlivym souborum
  */
  public void printStatistics(){
    System.out.println("\nFile accesses: (period=" + RequestedFile.getAddTime() + ")");
    for(final FileOnServer fos: this.fileTable.values()){
      if (fos.getVersion() > 1)
        System.out.println(fos.getFileName() + "; size=" + fos.getFileSize() + "; period=" + fos.writePeriod() + "; version=" + fos.getVersion());
    }
  }

  /**
   * metoda, ktera odstrani ze serveru vsechny souboru (je nasledne nutne soubory opet vytvorit pred dalsi simulaci)
   */
  public void hardReset(){
    this.fileNames.clear();
    this.fileTable.clear();
    this.rnd = new Random(seedValue);
    RequestedFile.setAddTime(0);
  }

  /**
   * metoda, ktera odtrani ze serveru statistiky u vsechn souboru
   */
  public void softReset(){
    for (final FileOnServer file: this.fileTable.values()){
      file.resetCounters();
    }
    this.rnd = new Random(seedValue);
    RequestedFile.setAddTime(0);
  }

  /**
   * setter pro nastaveni seed value pro random generator
   * @param seedValue seed value
   */
  public static void setSeedValue(final int seedValue) {
    Server.seedValue = seedValue;
  }

  /**
   * metoda, ktera u vsech souboru ulozi, jak casto jsou pristupovane
   */
  public void storeFileAccessTimes(){
    for (final FileOnServer file: this.fileTable.values()){
      file.storeTimes();
      file.setWriteHits(file.getVersion());
    }
  }

}
