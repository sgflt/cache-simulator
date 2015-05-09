package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.consistency.MMWPConsistency;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

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
	 * promenna pro uchovani verze cachovaneho souboru
	 */
	private int fVesrion;

	/**
	 * promenne pro pocitani hitu (statistiky na serveru)
	 */
	private int readHit = 0, writeHit = 0;
	
	/**
	 * cas, kdy soubor prisel do cache
	 */
	private long accessTime = 0;
	
	/**
	 * cas, jak casto se ma aktualizovat soubor
	 */
	private long TTL = Long.MAX_VALUE;
	
	/**
	 * cas, kdy byla u souboru naposledy kontrolovana verze se serverem 
	 */
	private long lastVersionCheckTime = 0;

	
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
		this.fVesrion = file.getVersion();
		this.lastVersionCheckTime = accessTime;
		this.TTL = MMWPConsistency.getTTL(file.getWriteHits());  
	}
	
	/**
	 * metoda pro porovnani aktualniho souboru se servrovou verzi (porovnani verzi)
	 * @param f soubor ze serveru
	 * @return true, pokud jsou stejne verze
	 */
	public boolean compareVersionWithServerFile(FileOnServer f){
		//soubory nejsou shodne - nemelo by se nekdy stat
		if (!this.fileName.equalsIgnoreCase(f.getFileName()))
			return true;
		//verze si odpovidaji - true
		if (this.fVesrion == f.getVersion()){
			return true;
		}
		return false;
	}
	
	/**
	 * metoda pro srovnani metadat tak, aby byly shodne se serverovou verzi souboru
	 * @param f soubor na serveru
	 */
	public void updateVerAndSize(FileOnServer f){
		//soubory si neopovidaji
		if (!this.fileName.equalsIgnoreCase(f.getFileName()))
			return;
		this.fVesrion = f.getVersion();
		this.fileSize = f.getFileSize();
	}
	
	/**
	 * metoda pro srovnani metadat tak, aby byly shodne se serverovou velikosti souboru
	 * @param f soubor na serveru
	 */
	public void updateSize(FileOnServer f){
		//soubory si neopovidaji
		if (!this.fileName.equalsIgnoreCase(f.getFileName()))
			return;
		this.fileSize = f.getFileSize();
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
	 * @return cas pristupu
	 */
	public long getActualTime(){
		return this.accessTime;
	}
	
	/**
	 * getter casoveho intevalu, jak casto se mame ptat na soubor, jestli je v cache 
	 * @return perioda
	 */
	public long getTTL() {
		return TTL;
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

	/**
	 * metoda pro ziskanei verze souboru
	 * @return verze souboru
	 */
	public int getVersion() {
		return fVesrion;
	}

	/**
	 * metoda pro zjisteni casu, kdy byla u souboru naposledy kontrolovana verze 
	 * @return cas
	 */
	public long getLastVersionCheckTime() {
		return lastVersionCheckTime;
	}

	/**
	 * metoda pro nastaveni casu, kdy byla u souboru naposledy kontrolovana verze 
	 * @return cas
	 */
	public void setLastVersionCheckTime(long lastVersionCheckTime) {
		this.lastVersionCheckTime = lastVersionCheckTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(!(obj instanceof FileOnClient)){
			return false;
		}
		FileOnClient other = (FileOnClient) obj;
		if(fileName == null){
			if(other.fileName != null){
				return false;
			}
		}
		else if(!fileName.equals(other.fileName)){
			return false;
		}
		if(fileSize != other.fileSize){
			return false;
		}
		return true;
	}
	
	

}