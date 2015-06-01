package cz.zcu.kiv.cacheSimulator.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequestedFile;



/**
 * trida pro spravu souboru na serveru
 *
 * @author Pavel Bzoch
 *
 */
public class Server {

  private static final Logger LOG = LoggerFactory.getLogger(Server.class);

  /**
   * struktura pro uchovavni souboru pro bezne cachovaci algoritmy
   */
  private final Map<String, FileOnServer> fileTable;

  /**
   * struktura pro ulozeni nazvu souboru
   */
  private final List<String> fileNames;

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
  private static volatile Server instance = null;

  /**
   * konstruktor - zaplneni struktury soubory
   */
  private Server() {
    this.fileTable = new HashMap<>(200000, 0.8f);
    this.fileNames = new ArrayList<>();
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
    final long size = Math.abs((this.rnd.nextLong()) % (maxSize - minSize)) + minSize;
    final FileOnServer f = new FileOnServer(name, size);
    this.fileTable.put(f.getFileName(), f);
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
    return f;
  }

  /**
   * metoda pro zjisteni, zda soubor existuje na serveru
   * @param name jmeno souboru
   * @return soubor, pokud existuje, jinak null
   */
  public boolean contains(final String name) {
    return this.fileTable.containsKey(name);
  }

  /**
   * metoda pro ziskani souboru na cteni
   *
   * @param fname
   *            jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public FileOnServer getFileRead(final String fname, final ICache cache) {
    //LOG.trace("getFileRead(fname, cache={})", fname, cache);

    final FileOnServer ret = this.fileTable.get(fname);

    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (ret != null && cache.needServerStatistics()){
      ret.increaseReadHit(cache);
    }

    return ret;
  }

  /**
   * metoda pro ziskani souboru na zapis
   *
   * @param fname jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public FileOnServer getFileWrite(final String fname, final ICache cache) {
    final FileOnServer ret = this.fileTable.get(fname);

    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (ret != null && cache.needServerStatistics()){
      ret.increaseWriteHit(cache);
    }

    return ret;
  }

  /**
   * metoda pro ziskani souboru na zapis i cteni
   *
   * @param fname jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public FileOnServer getFileReadWrite(final String fname, final ICache cache) {
    final FileOnServer ret = this.fileTable.get(fname);

    //pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
    if (ret != null && cache.needServerStatistics()){
      ret.increaseWriteHit(cache);
      ret.increaseReadHit(cache);
    }

    return ret;
  }

  /**
   * metoda pro ziskani souboru
   *
   * @param fname jmeno souboru, ktery pozadujeme
   * @return objekt souboru
   */
  public FileOnServer getFile(final String fname) {
    return this.fileTable.get(fname);
  }

  /**
   * metoda pro ziskani globalniho poctu hitu pro vsechny soubory
   *
   * @return pocet hitu
   */
  public long getGlobalReadHits(final ICache cache) {
    return this.fileTable.values().stream().mapToLong(file -> file.getReadHit(cache)).sum();
  }

  /**
   * metoda vrati velikost souboru pro dane jmeno souboru
   * @param fname jmeno souboru
   * @return velikost souboru
   */
  public long getFileSize(final String fname){
    final FileOnServer file = this.fileTable.get(fname);

    if (file != null) {
      return file.getFileSize();
    }

    LOG.error("File not found!");
    return 0;
  }

  /**
   * metoda pro ziskani nazvu souboru
   * @return nazvu souboru
   */
  public List<String> getFileNames() {
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
      System.out.println(fos.getFileName() + "; readHit=" + fos.getReadHit(cache) + "; size=" + fos.getFileSize() + "; period=" + fos.writePeriod());
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
