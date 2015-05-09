package cz.zcu.kiv.cacheSimulator.dataAccess;

/**
 * trida pro uchovani jednoho pristupu k souboru
 * pouziva se v pripade, ze jsou data z logu uchovavana v pameti pro rychlejsi simulaci
 * @author Pavel Bzoch
 *
 */
public class RequestedFile {
	/**
	 * promenna pro uchovani jmena souboru
	 */
	private String fname;
	
	/**
	 * promenna pro uchovani casu pristupu k souboru
	 */
	private long accessTime;
	
	/**
	 * promenna pro uchovani velikosti souboru 
	 */
	private long fSize;
	
	/**
	 * promenna pro uchovani ID uzivatele, ktery k souboru pristoupil
	 */
	private long userID;
	
	/**
	 * promenna pro uchovani, zda byl nacten soubor pro cteni nebo doslo k zapisu souboru
	 */
	private boolean read;
	
	/**
	 * promenna pro pripocitavani casu, pokud simulace bezi vicekrat za sebou
	 */
	private static long addTime = 0;
	
	/**
	 * konstruktor - iniciace promennych
	 * @param fname jmeno souboru
	 * @param accessTime cas pristupu
	 * @param fSize velikost souboru
	 * @param userID identifikace uzivatele
	 */
	public RequestedFile(String fname, long accessTime, long fSize, long userID, boolean read) {
		super();
		this.fname = fname;
		this.accessTime = accessTime + addTime;
		this.fSize = fSize;
		this.userID = userID;
		this.read = read;
	}

	/**
	 * metoda pro vraceni jmeno souboru
	 * @return jmeno souboru
	 */
	public String getFname() {
		return fname;
	}

	/**
	 * metoda pro vraceni casu pristupu k souboru
	 * @return cas pristupu
	 */
	public long getAccessTime() {
		return accessTime;
	}

	/**
	 * metoda pro vraceni velikosti souboru
	 * @return velikost souboru
	 */
	public long getfSize() {
		return fSize;
	}

	/**
	 * metoda pro vraceni id uzivatele
	 * @return identifikace uzivatele
	 */
	public long getUserID() {
		return userID;
	}

	/**
	 * metoda vraci, jestli byl soubor cten/zapisovan
	 * @return true, pokud cten
	 */
	public boolean isRead() {
		return read;
	}
	
	/**
	 * metoda pro nastaveni add time
	 * @param addTime pripocitavany cas
	 */
	public static void setAddTime(long addTime) {
		if (addTime < 0)
			return;
		RequestedFile.addTime = addTime;
	}

	/**
	 * metoda pro zjisteni periody simulace
	 * @return cas, jak dlouho probihala simualace
	 */
	public static long getAddTime() {
		return addTime;
	}
}
