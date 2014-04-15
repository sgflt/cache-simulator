package cz.zcu.kiv.cacheSimulator.server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequstedFile;



/**
 * trida pro spravu souboru na serveru
 * 
 * @author Pavel Bzoch
 * 
 */
public class Server {

	/**
	 * struktura pro uchovavni souboru pro bezne cachovaci algoritmy
	 */
	private Hashtable<String, FileOnServer> fileTable;
	
	/**
	 * struktura pro ulozeni nazvu souboru
	 */
	private ArrayList<String> fileNames;
	
	/**
	 * promenna pro generovani nahodne velikosti souboru
	 */
	private Random rnd;
	
	/**
	 * promenna pro uchovani seed value pro random generator 
	 */
	private static int seedValue = 0;
	
	/**
	 * promenna pro ziskani instance serveru
	 */
	private static Server instance = null;
	
	/**
	 * promenna pro uchovani posledniho pristupovaneho nebo vytvoreneho souboru
	 * pro rychlejsi pristup (predpoklad - naposledy pristupovany soubor bude zrejme jeste parkrat pristupovan)
	 */
	private FileOnServer lastFile = null;

	/**
	 * konstruktor - zaplneni struktury soubory
	 */
	private Server() {
		fileTable = new Hashtable<String, FileOnServer>(200000, 0.8f);
		fileNames = new ArrayList<String>();
		rnd = new Random(seedValue);
	}
	
	/**
	 * staticky konstruktor - v programu pouzivame navrh sigleton
	 * @return
	 */
	public static Server getInstance(){
		if (instance != null)
			return Server.instance;
		instance = new Server();
		return instance;
	}

	/**
	 * metoda pro vzgenerovani nahodne velikosti souboru
	 * @param name jmeno souboru
	 * @param minSize minimalnio velikost 
	 * @param maxSize maximalni velikost
	 * @return nove vytvoreny soubor
	 */
	public FileOnServer generateRandomFileSize(String name, long minSize, long maxSize){
		long size;
		FileOnServer f;	
		size = Math.abs(rnd.nextLong()) % (maxSize - minSize) + minSize;
		f = new FileOnServer(name, size);
		fileTable.put(f.getFileName(), f);
		lastFile = f;
		return f;
	}
	
	/**
	 * metoda pro vlozeni noveho souboru na server (z logovaciho souboru)
	 * @param name jmeno souboru
	 * @param size velikost souboru
	 * @return nove vytvoreny soubor
	 */
	public FileOnServer insertNewFile(String name, long size){
		FileOnServer f = new FileOnServer(name, size);
		fileTable.put(f.getFileName(), f);
		lastFile = f;
		return f;
	}
	
	/**
	 * metoda pro zjisteni, zda soubor existuje na serveru
	 * @param name jmeno souboru
	 * @return soubor, pokud existuje, jinak null
	 */
	public FileOnServer existFileOnServer(String name){
		if (fileTable == null) return null;
		if (lastFile  != null && lastFile.getFileName().equalsIgnoreCase(name))
			return lastFile;
		return fileTable.get(name);
	}

	/**
	 * metoda pro ziskani souboru na cteni
	 * 
	 * @param fname
	 *            jmeno souboru, ktery pozadujeme
	 * @return objekt souboru
	 */
	public synchronized FileOnServer getFileRead(String fname, ICache cache) {
		if (fileTable == null || fileTable.isEmpty())
			return null;
		FileOnServer ret;
		if (lastFile != null && lastFile.getFileName().equalsIgnoreCase(fname)){
			ret = lastFile;
		}
		else ret = fileTable.get(fname);
		if (ret == null)
			return null;
		//pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
		if (cache.needServerStatistics()){
			ret.increaseReadHit(cache.hashCode());
		}
		lastFile = ret;
		return ret;
	}

	/**
	 * metoda pro ziskani souboru na zapis
	 * 
	 * @param fname
	 *            jmeno souboru, ktery pozadujeme
	 * @return objekt souboru
	 */
	public synchronized FileOnServer getFileWrite(String fname, ICache cache) {
		if (fileTable == null || fileTable.isEmpty())
			return null;
		FileOnServer ret;
		if (lastFile != null && lastFile.getFileName().equalsIgnoreCase(fname)){
			ret = lastFile;
		}
		else ret = fileTable.get(fname);
		if (ret == null)
			return null;
		//pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
		if (cache.needServerStatistics()){
			ret.increaseWriteHit(cache.hashCode());
		}
		lastFile = ret;
		return ret;
	}

	/**
	 * metoda pro ziskani souboru na zapis i cteni
	 * 
	 * @param fname
	 *            jmeno souboru, ktery pozadujeme
	 * @return objekt souboru
	 */
	public synchronized FileOnServer getFileReadWrite(String fname, ICache cache) {
		if (fileTable == null || fileTable.isEmpty())
			return null;
		FileOnServer ret;
		if (lastFile != null && lastFile.getFileName().equalsIgnoreCase(fname)){
			ret = lastFile;
		}
		else ret = fileTable.get(fname);
		if (ret == null)
			return null;
		//pokud cachovaci algoritmus vyzaduje statistiky ze serveru, jsou tyto pro nej aktualizovany
		if (cache.needServerStatistics()){
			ret.increaseWriteHit(cache.hashCode());
			ret.increaseReadHit(cache.hashCode());
		}
		lastFile = ret;
		return ret;
	}
	
	/**
	 * metoda pro ziskani souboru
	 * 
	 * @param fname
	 *            jmeno souboru, ktery pozadujeme
	 * @return objekt souboru
	 */
	public synchronized FileOnServer getFile(String fname, ICache cache) {
		if (fileTable == null || fileTable.isEmpty())
			return null;
		FileOnServer ret;
		if (lastFile != null && lastFile.getFileName().equalsIgnoreCase(fname)){
			ret = lastFile;
		}
		else ret = fileTable.get(fname);
		if (ret == null)
			return null;
		return ret;
	}

	/**
	 * metoda pro ziskani globalniho poctu hitu pro vsechny soubory
	 * 
	 * @return pocet hitu
	 */
	public synchronized long getGlobalReadHits(ICache cache) {
		long ret = 0; 
		for (FileOnServer f: fileTable.values())
		{ 
			ret += f.getReadHit(cache.hashCode()); 
		}
		return ret;
	}
	
	/**
	 * metoda vrati velikost souboru pro dane jmeno souboru
	 * @param fname jmeno souboru
	 * @return velikost souboru
	 */
	public synchronized long getFileSize(String fname){
		if (lastFile != null && lastFile.getFileName().equalsIgnoreCase(fname))
			return lastFile.getFileSize();
		else{
			FileOnServer file = fileTable.get(fname);
			if (file != null){
				lastFile = file;
				return file.getFileSize();
			}
			else{
				System.out.println("Chyba!");
				return 0;		
			}
		}
	}
	
	/**
	 * metoda pro ziskani nazvu souboru
	 * @return nazvu souboru
	 */
	public ArrayList<String> getFileNames() {
		//preklopeni nazvu souboru do listu pro pozdejsi praci s nimi 
		fileNames.clear();
		for (FileOnServer files : fileTable.values()) {
			fileNames.add(files.getFileName());
		}
		return fileNames;
	}
	
	/**
	 * metoda pro vytisteni statistik o pristupu k jednotlivym souborum
	*/
	public void printStatistics(ICache cache){
		System.out.println("\nFile accesses:");
		for(FileOnServer fos: fileTable.values()){
			System.out.println(fos.getFileName() + "; readHit=" + fos.getReadHit(cache.hashCode()) + "; size=" + fos.getFileSize() + "; period=" + fos.writePeriod());
		}
	}
	
	/**
	 * metoda pro vytisteni statistik o pristupu k jednotlivym souborum
	*/
	public void printStatistics(){
		System.out.println("\nFile accesses: (period=" + RequstedFile.getAddTime() + ")");
		for(FileOnServer fos: fileTable.values()){
			if (fos.getVersion() > 1)
				System.out.println(fos.getFileName() + "; size=" + fos.getFileSize() + "; period=" + fos.writePeriod() + "; version=" + fos.getVersion());
		}
	}
	
	/**
	 * metoda, ktera odstrani ze serveru vsechny souboru (je nasledne nutne soubory opet vytvorit pred dalsi simulaci)
	 */
	public void hardReset(){
		this.fileNames.clear();
		this.fileTable.clear();
		this.rnd = new Random(seedValue);
		RequstedFile.setAddTime(0);
	}
	
	/**
	 * metoda, ktera odtrani ze serveru statistiky u vsechn souboru
	 */
	public void softReset(){
		for (FileOnServer file: fileTable.values()){
			file.resetCounters();
		}
		this.rnd = new Random(seedValue);
		RequstedFile.setAddTime(0);
	}

	/**
	 * setter pro nastaveni seed value pro random generator
	 * @param seedValue seed value
	 */
	public static void setSeedValue(int seedValue) {
		Server.seedValue = seedValue;
	}
	
	/**
	 * metoda, ktera u vsech souboru ulozi, jak casto jsou pristupovane 
	 */
	public void storeFileAccessTimes(){
		for (FileOnServer file: fileTable.values()){
			file.storeTimes();
			file.setWriteHits(file.getVersion());
		}
	}
	
}
