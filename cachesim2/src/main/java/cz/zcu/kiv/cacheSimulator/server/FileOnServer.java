package cz.zcu.kiv.cacheSimulator.server;

import java.util.Hashtable;

/**
 * trida pro prezentaci souboru na strane serveru
 * kazdy soubor si u sebe nese informaci, kolikrat byl 
 * na strane serveru pristupovan jakym cachovacim algoritmem
 * 
 * @author Pavel Bzoch
 * 
 */
public class FileOnServer {

	/**
	 * nazev souboru vcetne cele cesty
	 */
	private String fileName;

	/**
	 * velikost souboru v Bytech
	 */
	private long fileSize;

	/**
	 * promenne pro pocitani hitu (statistiky na serveru) podle cachovaciho algoritmu
	 */
	private Hashtable<Integer, Integer> readHit, writeHit;

	/**
	 * konstruktor - inicializace promennych
	 * @param fileName
	 * @param fileSize
	 */
	public FileOnServer(String fileName, long fileSize) {
		super();
		this.fileName = fileName;
		this.fileSize = fileSize;
		readHit = new Hashtable<Integer, Integer>();
		writeHit = new Hashtable<Integer, Integer>();
	}

	/**
	 * metoda vrati pocet hitu na cteni souboru podle cachovaciho algoritmu
	 * @param cache cachovaci algoritmus
	 * @return pocet hitu
	 */
	public int getReadHit(int cacheHash) {
		if (!readHit.containsKey(cacheHash))
			return 0;
		return readHit.get(cacheHash);
	}

	/**
	 * metoda navysi pocet read hitu pro dany soubor podle cachovaciho algoritmu
	 * @param cache cachovaci algoritmus
	 */
	public void increaseReadHit(int cacheHash) {
		if (!readHit.containsKey(cacheHash)){
			readHit.put(cacheHash, new Integer(1));
		}
		else
		{
			readHit.put(cacheHash, new Integer(readHit.get(cacheHash).intValue() + 1));
		}
	}

	/**
	 * Metoda vrati pocet hitu na zapis u souboru podle cachovaciho algoritmu
	 * @param cache cachovaci algoritmus
	 * @return pocet hitu
	 */
	public int getWriteHit(int cacheHash) {
		if (!writeHit.containsKey(cacheHash))
			return 0;
		return writeHit.get(cacheHash);
	}

	/**
	 * metoda pro zvyseni poctu hitu u zapisu pro soubor
	 * @param cache cachovaci algoritmus, ktery zapisoval (identifikace klienta)
	 */
	public void increaseWriteHit(int cacheHash) {
		if (!writeHit.containsKey(cacheHash)){
			writeHit.put(cacheHash, new Integer(1));
		}
		else
		{
			writeHit.put(cacheHash, new Integer(writeHit.get(cacheHash).intValue() + 1));
		}
	}

	/**
	 * getter pro ziskani jmena souboru
	 * @return jmeno souboru
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * getter pro ziskani velikosti souboru
	 * @return velikost souboru
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * metoda pro vyresetovani citaci pristupu
	 */
	public void resetCounters(){
		this.readHit.clear();
		this.writeHit.clear();
	}
}
