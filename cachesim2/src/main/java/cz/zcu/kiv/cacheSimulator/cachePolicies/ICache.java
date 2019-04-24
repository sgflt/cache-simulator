package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;

/**
 * rozhrani pro cache
 * 
 * interface for caching policies
 * 
 * @author Pavel Bzoch
 * 
 */
public interface ICache {

	/**
	 * This method performs lookup into cache and possibly returns a contained file.
	 *
	 * @param fileName to retrieve a file
	 * @return file from cache
	 */
	FileOnClient get(String fileName);

	/**
	 * metoda pro ziskani velikosti volne kapacity cache
	 * 
	 * @return volna kapacita
	 */
	long freeCapacity();

	/**
	 * metoda pro vyhozeni souboru z cache pri nedostecne kapacite
	 */
	void removeFile();

	/**
	 * metoda pro vlozeni souboru do cache
	 * 
	 * @param f
	 *            soubor pro vlozeni
	 */
	void insertFile(FileOnClient f);
	
	/**
	 * metoda pro zjisteni, zda dany algoritmus pozaduje statistiky ze serveru
	 * @return true, pokud pozaduje
	 */
	boolean needServerStatistics();
	
	/**
	 * metoda pro nastaveni nove kapacity cache
	 * @param capacity nova kapacita
	 */
	void setCapacity(long capacity);
	
	/**
	 * metoda pro vymazani cache 
	 */
	void reset();
	
	/**
	 * metoda vraci jmeno tridy a kratky popis cache algoritmu oddeleny strednikem
	 * @return viz popis
	 */
	String cacheInfo();

}
