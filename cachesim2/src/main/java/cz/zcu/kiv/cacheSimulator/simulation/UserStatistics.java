/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Trida pro uchovani statistik pro jednoho uzivatele
 *
 * @author Pavel Bzoch
 */
public class UserStatistics {

  /**
   * promenna pro uchovani identifikace uzivatele
   */
  private final long userID;

  /**
   * promenna pro ulozeni celkoveho poctu pristupu k souborum
   */
  private final long fileAccessed;

  /**
   * promenna pro ulozeni celkoveho potrebneho datoveho prenosu
   */
  private final long totalNetworkBandwidth;

  /**
   * promenna pro uchovani archivu
   */
  private final List<Statistics> cachesResults;

  /**
   * velikosti cachi
   */
  private final Integer[] cacheSizes;

  /**
   * konstruktor - inicializace promennych
   *
   * @param user
   */
  public UserStatistics(final SimulatedUser user) {
    this.userID = user.getID();
    this.fileAccessed = user.getFileAccessed();
    this.totalNetworkBandwidth = user.getTotalNetworkBandwidth();
    this.cachesResults = user.getCachesResults();
    this.cacheSizes = new Integer[MainGUI.getInstance().getCacheSizes().length];
    System.arraycopy(MainGUI.getInstance().getCacheSizes(), 0, this.cacheSizes, 0, this.cacheSizes.length);
  }

  /**
   * metoda vraci jmena vsech simulovanych cache policy
   *
   * @return pole jmen cache
   */
  public List<String> getCacheNames() {
    return this.cachesResults.stream()
      .map(Statistics::getCache)
      .map(ICache::toString)
      .distinct()
      .collect(Collectors.toList());
  }

  /**
   * metoda pro vraceni cache hits pro danou policy
   *
   * @param cacheName jmeno cache
   * @return vysledky
   */
  public Long[] getCacheHits(final String cacheName) {
    return this.cachesResults.stream()
      .filter(s -> s.getCache().toString().equals(cacheName))
      .map(Statistics::getCacheHit)
      .toArray(Long[]::new);
  }

  /**
   * metoda vraci vysledky mereni cache hit ratio pro danou cache policy
   *
   * @param cacheName jmeno cache
   * @return pole s vysledky
   */
  public Double[] getCacheHitRatios(final String cacheName) {
    return this.cachesResults.stream()
      .filter(s -> s.getCache().toString().equals(cacheName))
      .map(Statistics::getHitRatio)
      .toArray(Double[]::new);
  }

  /**
   * metoda vraci vysledky usporeneho sitoveho provozu [MB]
   *
   * @param cacheName jmeno cache
   * @return pole vysledku
   */
  public Long[] getSavedBytes(final String cacheName) {
    return this.cachesResults.stream()
      .filter(s -> s.getCache().toString().equals(cacheName))
      .map(Statistics::getSavedTraffic)
      .map(UserStatistics::toMegabytes)
      .toArray(Long[]::new);
  }

  private static Long toMegabytes(final Long resB) {
    return resB / 1024 / 1024;
  }

  /**
   * metoda vraci vysledky mereni saved bytes ratio pro danou cache policy
   *
   * @param cacheName jmeno cache
   * @return pole s vysledky
   */
  public Double[] getCacheSavedBytesRatio(final String cacheName) {
    return this.cachesResults.stream()
      .filter(s -> s.getCache().toString().equals(cacheName))
      .map(Statistics::getSavedTrafRatio)
      .toArray(Double[]::new);
  }

  /**
   * metoda vraci vysledky data transfer degrease
   *
   * @param cacheName jmeno cache
   * @return pole vysledku
   */
  public Long[] getDataTransferDegrease(final String cacheName) {
    final Long[] savedBytes = getSavedBytes(cacheName);
    final Long[] ret = new Long[savedBytes.length];
    for (int i = 0; i < savedBytes.length; i++) {
      ret[i] = (this.totalNetworkBandwidth - savedBytes[i]) / 1024 / 1024;
    }
    return ret;
  }

  /**
   * metoda vraci vysledky data transfer degrease ratio
   *
   * @param cacheName jmeno cache
   * @return pole s vysledky
   */
  public Double[] getDataTransferDegreaseRatio(final String cacheName) {
    final Double[] savedBytesRatio = getCacheSavedBytesRatio(cacheName);
    final Double[] ret = new Double[savedBytesRatio.length];
    for (int i = 0; i < savedBytesRatio.length; i++) {
      ret[i] = 100 - savedBytesRatio[i];
    }
    return ret;
  }

  /**
   * metoda pvraci userID
   *
   * @return userID
   */
  public long getUserID() {
    return this.userID;
  }

  /**
   * metoda vraci pocet pristupovanych souboru
   *
   * @return pocet pristupovanych souboru
   */
  public long getFileAccessed() {
    return this.fileAccessed;
  }

  /**
   * metoda pro vraceni celkove velikosti pristupovanych souboru
   *
   * @return velikost sitoveho trafficu
   */
  public long getTotalNetworkBandwidth() {
    return this.totalNetworkBandwidth;
  }

  /**
   * metoda vraci velikosti cachi
   *
   * @return pole s velikosti cache
   */
  public Integer[] getCacheSizes() {
    return this.cacheSizes;
  }
}
