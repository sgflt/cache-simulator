package cz.zcu.kiv.cacheSimulator.consistency;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * rozhrani pro praci a uchovavani informaci o konzistentnosti cachovanych dat
 *
 * @author Pavel Bžoch
 */
public interface IConsistencySimulation {

  /**
   * metoda pro update informaci o konzistenci souboru pri zjistenem zapisu do souboru
   *
   * @param cache cache, kde je souboru ulozen
   * @param userID identifikace uzivatele
   * @param fOnClient soubor, ktery je ulozen v cache
   * @param fOnServer nova verze souboru
   */
  public void updateConsistencyWrite(ICache cache, long userID, FileOnClient fOnClient,
      FileOnServer fOnServer);


  /**
   * metoda, ktera slouzi k aktualizaci dat pri ctenem souboru
   *
   * @param cache cache, kde je souboru ulozen
   * @param userID identifikace uzivatele
   * @param fOnClient soubor, ktery je ulozen v cache
   * @param fOnServer nova verze souboru
   */
  public void updateActualReadFile(ICache cache, long userID, FileOnClient fOnClient,
      FileOnServer fOnServer);


  /**
   * metoda, ktera vrati jmena sloupecku pro vzkrelseni do tabulky
   *
   * @return pole jmen
   */
  public String[] getHeaders();


  /**
   * metoda pro vraceni dat pro vykresleni do tabulky
   *
   * @param cacheName jmeno cache, pro kterou budeme vykreslovat
   * @param userID ID usera, pro ktereho vzkreslujeme
   * @return data pro tabulku
   */
  public Object[][] getData(String cacheName, long userID);


  /**
   * metoda pro ziskani informaci o konzistentnim modelu
   *
   * @return jmeno tridy a info
   */
  public String getInfo();


  /**
   * metoda pro tisk vysledku do konzole (ladici ucely)
   */
  public void printStat();

}
