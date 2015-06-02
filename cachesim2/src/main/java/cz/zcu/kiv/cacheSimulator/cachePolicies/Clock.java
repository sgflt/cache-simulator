package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * trida pro cache algoritmus clock - podobne jako fifo
 *
 * class for CLOCK algorithm
 *
 * SOURCE: Adapted from book "Modern Operating Systems (Second Edition)",
 * by Andrew S. Tanenbaum
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
 */
public class Clock implements ICache {

  /**
   * struktura pro uchovani souboru
   */
  private final ArrayList<Pair<FileOnClient, Boolean>> Flist;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final ArrayList<FileOnClient> fOverCapacity;


  /**
   * ukazuje tam, kam se ma vlozit novy prvek
   */
  private int index = 0;

  /**
   * velikost cache v B
   */
  private long capacity = 0;

  /**
   * pocatecni kapacita cache
   */
  private long initialCapacity = 0;

  /**
   * konstruktor - inicializace cache
   */
  public Clock() {
    this.Flist = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }

  @Override
  public boolean contains(final String fName) {
    for (final Pair<FileOnClient, Boolean> f : this.Flist) {
      if (f.getFirst().getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }

  @Override
  public FileOnClient getFile(final String fName) {
    for (final Pair<FileOnClient, Boolean> f : this.Flist) {
      if (f.getFirst().getFileName().equalsIgnoreCase(fName)){
        f.setSecond(true);
        return f.getFirst();
      }
    }
    return null;
  }

  @Override
  public long freeCapacity() {
    long obsazeno = 0;
    for (final Pair<FileOnClient, Boolean> f : this.Flist) {
      obsazeno += f.getFirst().getFileSize();
    }
    return this.capacity - obsazeno;
  }

  @Override
  public void removeFile() {
    this.index = this.index % this.Flist.size();
    Pair<FileOnClient, Boolean> file = this.Flist.get(this.index);
    while(file.getSecond() == true){
      file.setSecond(false);
      this.index = (this.index + 1) % this.Flist.size();
      file = this.Flist.get(this.index);
    }
    this.Flist.remove(file);
  }

  @Override
  public void insertFile(final FileOnClient f) {
    //napred zkontrolujeme, jestli se soubor vejde do cache
    //pokud se nevejde, vztvorime pro nej okenko
    if (f.getFileSize() > this.capacity){
      if (!this.fOverCapacity.isEmpty()){
        this.fOverCapacity.add(f);
        return;
      }
      while (this.freeCapacity() < (long)(this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow()))
        this.removeFile();
      this.fOverCapacity.add(f);
      this.capacity = (long) (this.capacity * (1-GlobalVariables.getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty())
      this.checkTimes();


    //pokud se soubor vejde, fungujeme spravne
    while (this.freeCapacity() < f.getFileSize()) {
      this.removeFile();
    }

    this.Flist.add(this.index, new Pair<>(f, true));
    this.index = (this.index + 1);
  }

  @Override
  public String toString(){
    return "Clock";
  }

  @Override
  public boolean needServerStatistics() {
    return false;
  }

  @Override
  public void setCapacity(final long capacity) {
    this.capacity = capacity;
    this.initialCapacity = capacity;
  }

  @Override
  public void reset() {
    this.Flist.clear();
    this.index = 0;
    this.fOverCapacity.clear();
  }

  /**
   * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache stazene - pak odstranime okenko
   */
  private void checkTimes() {
    boolean hasBeenRemoved = true;
    while (hasBeenRemoved){
      hasBeenRemoved = false;
      if (!this.fOverCapacity.isEmpty() && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()){
        this.fOverCapacity.remove(0);
        hasBeenRemoved = true;
      }
    }
    if (this.fOverCapacity.isEmpty()){
      this.capacity = this.initialCapacity;
    }
  }

  @Override
  public String cacheInfo(){
    return "Clock;Clock";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.initialCapacity ^ (this.initialCapacity >>> 32));
    result = prime * result + ((this.toString() == null) ? 0 : this.toString().hashCode());
    return result;
  }

  @Override
  public void removeFile(final FileOnClient f) {
    Pair<FileOnClient, Boolean> pair = null;
    for (final Pair<FileOnClient, Boolean> file : this.Flist){
      if (file.getFirst() == f){
        pair = file;
        break;
      }
    }
    if (pair != null){
      this.Flist.remove(pair);
    }
  }

  @Override
  public List<FileOnClient> getCachedFiles() {
    final List<FileOnClient> list = new ArrayList<>(this.Flist.size());
    for (final Pair<FileOnClient, Boolean> file : this.Flist){
      list.add(file.getFirst());
    }
    return list;
  }

  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }

}
