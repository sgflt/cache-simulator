package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.List;

import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * rozhrani pro cache
 * 
 * interface for caching policies
 * @author Pavel Bzoch
 * 
 */
public interface ICache {

	/**
	 * metoda pro zjisteni, jestli je soubor v cache
	 * 
	 * @param fName
	 *            jmeno souboru vcetne cesty / file name with path
	 * @return true, pokud je
	 */
	public boolean isInCache(String fName);

	/**
	 * Metoda pro ziskani souboru z cache
	 * 
	 * @param fName
	 *            jmeno souboru vcetne cesty
	 * @return objekt souboru
	 */
	public FileOnClient getFileFromCache(String fName);

	/**
	 * metoda pro ziskani velikosti volne kapacity cache
	 * 
	 * @return volna kapacita
	 */
	public long freeCapacity();

	/**
	 * metoda pro vyhozeni souboru z cache pri nedostecne kapacite
	 */
	public void removeFile();
	
	/**
	 * metoda pro odstraneni pozadovaneho souboru z cache
	 * pouziva se pri udrzovani konzistence
	 * @param f soubor k odstraneni
	 */
	public void removeFile(FileOnClient f);
	
	/**
	 * metoda pro ziskani seznamu vsech cachovanych souboru
	 * pouziva se pri udrzovani konzistence
	 * @return list souboru
	 */
	public List<FileOnClient> getCachedFiles();
	

	/**
	 * metoda pro vlozeni souboru do cache
	 * 
	 * @param f
	 *            soubor pro vlozeni
	 */
	public void insertFile(FileOnClient f);
	
	/**
	 * metoda pro zjisteni, zda dany algoritmus pozaduje statistiky ze serveru
	 * @return true, pokud pozaduje
	 */
	public boolean needServerStatistics();
	
	/**
	 * metoda pro nastaveni nove kapacity cache
	 * @param capacity nova kapacita
	 */
	public void setCapacity(long capacity);
	
	/**
	 * metoda pro vymazani cache 
	 */
	public void reset();
	
	/**
	 * metoda vraci jmeno tridy a kratky popis cache algoritmu oddeleny strednikem
	 * @return viz popis
	 */
	public String cacheInfo();
	
	/**
	 * metoda pro ziskani kapacity cache
	 * @return velikost cache
	 */
	public long getCacheCapacity();

}
