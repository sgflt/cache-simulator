package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * class for LRFU algorithm
 * SOURCE: Adapted from article
 * "LRFU: a spectrum of policies that subsumes the least recently used and least frequently used policies"
 * , by D. Lee, J. Choi, J.-H. Kim, S. Noh, S. L. Min, Y. Cho and C. S. Kim
 *
 * @author Pavel Bžoch
 * @author Lukáš Kvídera
 * @version 2.1
 */
public class LRFU implements ICache {

  private static final Comparator<Triplet<FileOnClient, Long, Double>> comparator =
      (o1, o2) -> Double.compare(o1.getThird(), o2.getThird());

  /**
   * konstanta p
   */
  private static double P = 2.0f;

  /**
   * konstanta lambda
   */
  private static double LAMBDA = 0.045f;

  /**
   * atribut pro pocitani casu
   */
  private long timeCounter = 1;

  /**
   * atribut pro uchovani souboru v cache
   */
  private final List<Triplet<FileOnClient, Long, Double>> fList;

  /**
   * struktura pro ukladani souboru, ktere jsou vetsi nez cache
   */
  private final List<FileOnClient> fOverCapacity;

  /**
   * velikost cache v B
   */
  private long capacity = 0;

  /**
   * pocatecni kapacita cache
   */
  private long initialCapacity = 0;

  /**
   * promenna pro urceni, zda je potreba setridit pole cachovanych souboru
   */
  private boolean needSort = true;

  private long used;


  /**
   * konstruktor - iniciace parametru
   *
   * @param capacity
   */
  public LRFU() {
    this.needSort = true;
    this.fList = new ArrayList<>();
    this.fOverCapacity = new ArrayList<>();
  }


  @Override
  public boolean contains(final String fName) {
    for (final Triplet<FileOnClient, Long, Double> triplet : this.fList) {
      if (triplet.getFirst().getFileName().equalsIgnoreCase(fName))
        return true;
    }
    return false;
  }


  @Override
  public FileOnClient getFile(final String fName) {
    Triplet<FileOnClient, Long, Double> file = null;
    for (final Triplet<FileOnClient, Long, Double> triplet : this.fList) {
      if (triplet.getFirst().getFileName().equalsIgnoreCase(fName)) {
        file = triplet;
        break;
      }
    }
    if (file == null)
      return null;

    final long actTime = ++this.timeCounter;
    file.setThird(this.calculateF(0) + file.getThird() * this.calculateF(actTime - file.getSecond()));
    file.setSecond(actTime);
    this.needSort = true;
    return file.getFirst();
  }


  @Override
  public long freeCapacity() {
    return this.capacity - this.used;
  }


  @Override
  public void removeFile() {
    if (this.needSort) {
      Collections.sort(this.fList, comparator);
      this.needSort = false;
    }

    if (!this.fList.isEmpty()) {
      this.used -= this.fList.remove(0).getFirst().getFileSize();
    }
  }


  @Override
  public void insertFile(final FileOnClient f) {
    // napred zkontrolujeme, jestli se soubor vejde do cache
    // pokud se nevejde, vztvorime pro nej okenko
    if (f.getFileSize() > this.capacity) {
      if (!this.fOverCapacity.isEmpty()) {
        this.fOverCapacity.add(f);
        return;
      }

      while (this.freeCapacity() < (long) (this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
        this.removeFile();
      }

      this.fOverCapacity.add(f);
      this.capacity = (long) (this.capacity * (1 - GlobalVariables.getCacheCapacityForDownloadWindow()));
      return;
    }

    if (!this.fOverCapacity.isEmpty())
      this.checkTimes();

    // pokud se soubor vejde, fungujeme spravne
    this.needSort = true;
    while (this.freeCapacity() < f.getFileSize()) {
      this.removeFile();
    }

    this.fList.add(new Triplet<>(f, ++this.timeCounter, this.calculateF(0)));
    this.used += f.getFileSize();
  }


  /**
   * metoda pro vypocet priority
   *
   * @param x
   *          casovy parametr
   * @return priorita souboru
   */
  private double calculateF(final long x) {
    return Math.pow((1.0 / P), (LAMBDA * x));
  }


  @Override
  public String toString() {
    return "LRFU";
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
    this.timeCounter = 1;
    this.fList.clear();
    this.needSort = true;
    this.fOverCapacity.clear();
  }


  /**
   * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache
   * stazene - pak odstranime okenko
   */
  private void checkTimes() {
    boolean hasBeenRemoved = true;
    while (hasBeenRemoved) {
      hasBeenRemoved = false;
      if (!this.fOverCapacity.isEmpty() && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()) {
        this.fOverCapacity.remove(0);
        hasBeenRemoved = true;
      }
    }
    if (this.fOverCapacity.isEmpty()) {
      this.capacity = this.initialCapacity;
    }
  }


  @Override
  public String cacheInfo() {
    return "LRFU;LRFU";
  }


  public static double getP() {
    return P;
  }


  public static void setP(final double P) {
    LRFU.P = P;
  }


  public static double getLAMBDA() {
    return LAMBDA;
  }


  public static void setLAMBDA(final double LAMBDA) {
    LRFU.LAMBDA = LAMBDA;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.initialCapacity ^ (this.initialCapacity >>> 32));
    result = prime * result + this.toString().hashCode();
    return result;
  }


  @Override
  public void removeFile(final FileOnClient f) {
    for (final Triplet<FileOnClient, Long, Double> file : this.fList) {
      if (file.getFirst() == f) {
        this.fList.remove(file);
        this.used -= file.getFirst().getFileSize();
        break;
      }
    }
  }


  @Override
  public List<FileOnClient> getCachedFiles() {
    return this.fList.stream().map(triplet -> triplet.getFirst()).collect(Collectors.toList());
  }


  @Override
  public long getCapacity() {
    return this.initialCapacity;
  }

}
