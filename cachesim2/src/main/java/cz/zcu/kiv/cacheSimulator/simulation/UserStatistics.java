/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.cacheSimulator.simulation;

import java.util.Hashtable;

import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;

/**
 * Trida pro uchovani statistik pro jednoho uzivatele
 * @author Pavel Bzoch
 */
public class UserStatistics {
    
    /**
     * promenna pro uchovani identifikace uzivatele
     */
    private long userID;
    
    /**
     * promenna pro ulozeni celkoveho poctu pristupu k souborum 
     */
    private long fileAccessed = 0;
    
    /**
     * promenna pro ulozeni celkoveho potrebneho datoveho prenosu
     */
    private long totalNetworkBandwidth = 0;
    
    /**
     * promenna pro uchovani archivu 
     */
    private Hashtable<String, Quartet<Long[], Long[], Double[], Double[]>> cachesResults;
    
    /**
     * velikosti cachi
     */
    private Integer[] cacheSizes;

    /**
     * konstruktor - inicializace promennych
     * @param user 
     */
    public UserStatistics(SimulatedUser user) {
        this.userID = user.getID();
        this.fileAccessed = user.getFileAccessed();
        this.totalNetworkBandwidth = user.getTotalNetworkBandwidth();
        this.cachesResults = user.getCachesResults();
        cacheSizes = new Integer[MainGUI.getInstance().getCacheSizes().length];
        System.arraycopy(MainGUI.getInstance().getCacheSizes(), 0, cacheSizes, 0, cacheSizes.length);
     }
    
    /**
     * metoda vraci jmena vsech simulovanych cache policy
     * @return pole jmen cache
     */
    public String[] getCacheNames(){
        String[] y = cachesResults.keySet().toArray(new String[0]);
        return y;
    }
    
    /**
     * metoda pro vraceni cache hits pro danou policy
     * @param cacheName jmeno cache
     * @return vysledky
     */
    public Long[] getCacheHits(String cacheName){
        return cachesResults.get(cacheName).getFirst();
    }
    
    /**
     * metoda vraci vysledky mereni cache hit ratio pro danou cache policy
     * @param cacheName jmeno cache
     * @return pole s vysledky
     */
    public Double[] getCacheHitRatios(String cacheName){
        return cachesResults.get(cacheName).getThird();
    }
    
    /**
     * metoda vraci vysledky usporeneho sitoveho provozu [MB]
     * @param cacheName jmeno cache
     * @return pole vysledku
     */
    public Long[] getSavedBytes(String cacheName){
        Long[] resB = cachesResults.get(cacheName).getSecond();
        Long[] resMB = new Long[resB.length];
        for (int i = 0; i < resB.length; i++){
            resMB[i] = resB[i] / 1024 / 1024;
        }
        return resMB;
    }
    
    /**
     * metoda vraci vysledky mereni saved bytes ratio pro danou cache policy
     * @param cacheName jmeno cache
     * @return pole s vysledky
     */
    public Double[] getCacheSavedBytesRatio(String cacheName){
        return cachesResults.get(cacheName).getFourth();
    }
    
        /**
     * metoda vraci vysledky data transfer degrease
     * @param cacheName jmeno cache
     * @return pole vysledku
     */
    public Long[] getDataTransferDegrease(String cacheName){
        Long[] savedBytes = cachesResults.get(cacheName).getSecond();
        Long[] ret = new Long[savedBytes.length];
        for (int i = 0; i < savedBytes.length; i++){
            ret[i] = (totalNetworkBandwidth - savedBytes[i]) / 1024 / 1024;
        }
        return ret;
    }
    
    /**
     * metoda vraci vysledky data transfer degrease ratio
     * @param cacheName jmeno cache
     * @return pole s vysledky
     */
    public Double[] getDataTransferDegreaseRatio(String cacheName){
        Double[] savedBytesRatio = getCacheSavedBytesRatio(cacheName);
        Double[] ret = new Double[savedBytesRatio.length];
        for (int i = 0; i < savedBytesRatio.length; i++){
            ret[i] = 100 - savedBytesRatio[i];
        }
        return ret;
    }

    /**
     * metoda pvraci userID
     * @return userID
     */
    public long getUserID() {
        return userID;
    }

    /**
     * metoda vraci pocet pristupovanych souboru
     * @return pocet pristupovanych souboru
     */
    public long getFileAccessed() {
        return fileAccessed;
    }

    /**
     * metoda pro vraceni celkove velikosti pristupovanych souboru
     * @return velikost sitoveho trafficu
     */
    public long getTotalNetworkBandwidth() {
        return totalNetworkBandwidth;
    }

    /**
     * metoda vraci velikosti cachi
     * @return pole s velikosti cache
     */
    public Integer[] getCacheSizes() {
        return cacheSizes;
    }
}
