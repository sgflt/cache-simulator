package cz.zcu.kiv.cacheSimulator.shared;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;

/**
 * trida pro reprezentaci souboru na strane klienta
 * udrzuje informace nutne pro cachovaci algoritmy
 * 
 * @author Pavel Bzoch
 *
 */
public class FileOnClient {

	/**
	 * nazev souboru vcetne cele cesty
	 */
	private String fileName;

	/**
	 * velikost souboru v Bytech
	 */
	private long fileSize;

	/**
	 * promenne pro pocitani hitu (statistiky na serveru)
	 */
	private int readHit = 0, writeHit = 0;
	
	/**
	 * cas, kdy soubor prisel do cache
	 */
	private long accessTime = 0;

	/**
	 * konstruktor - inicializace promennych
	 * @param fileName
	 * @param fileSize
	 * @param accessTime
	 */
	public FileOnClient(String fileName, long fileSize, long accessTime) {
		super();
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.accessTime = accessTime;
	}
	
	/**
	 * konstruktor - inicializace promennych - ze strany serveru
	 * @param file
	 * @param cache
	 * @param accessTime
	 */
	public FileOnClient(FileOnServer file, ICache cache, long accessTime){
		super();
		this.fileName = file.getFileName();
		this.fileSize = file.getFileSize();
		this.readHit = file.getReadHit(cache.hashCode());
		this.writeHit = file.getWriteHit(cache.hashCode());
		this.accessTime = accessTime;
	}
	
	/**
	 * metoda vraci, kdy je mozne soubor odstranit z cache
	 * na zaklade prumerne rychlosti site vypocte, jak dlouho bude trvat soubor stahnout
	 * @return cas mozneho odstraneni
	 */
	public long getFRemoveTime(){
		if (this.accessTime == 0) return 0;
		return this.fileSize * 8 /1000 / (GlobalVariables.getAverageNetworkSpeed()) + accessTime;
	}
	
	/**
	 * metoda vraci aktualni cas (cas pristupu k souboru)
	 * @return caspristupu
	 */
	public long getActualTime(){
		return this.accessTime;
	}

	/**
	 * getter pro zjisteni poctu pristupu na cteni souboru
	 * @return pocet cteni souboru
	 */
	public int getReadHit() {
		return readHit;
	}

	/**
	 * metoda pro zvyseni poctu pristupu na cteni
	 */
	public void increaseReadHit() {
		this.readHit++;
	}

	/**
	 * getter pro zjisteni poctu pristupu na zapis do souboru
	 * @return pocet zapisu do souboru
	 */
	public int getWriteHit() {
		return writeHit;
	}

	/**
	 * metoda pro zvyseni poctu pristupu na zapis
	 */
	public void increaseWriteHit() {
		this.writeHit++;
	}

	/**
	 * metoda pro zjisteni jmena souboru
	 * @return jmeno souboru
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * metoda pro zjisteni velikosti souboru
	 * @return velikost souboru
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * metoda por vyresetovani citacu
	 */
	public void resetCounters(){
		this.readHit = 0;
		this.writeHit = 0;
	}
}