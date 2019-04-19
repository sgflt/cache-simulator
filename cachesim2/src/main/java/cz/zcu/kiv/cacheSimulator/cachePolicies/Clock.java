package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;


/**
 * trida pro cache algoritmus clock - podobne jako fifo
 * 
 * class for CLOCK algorithm
 * 
 * SOURCE: Adapted from book "Modern Operating Systems (Second Edition)", 
 * by Andrew S. Tanenbaum
 * 
 * @author Pavel Bzoch
 *
 */
public class Clock implements ICache {

	/**
	 * struktura pro uchovani souboru
	 */
	private ArrayList<Pair<FileOnClient, Boolean>> Flist;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;
	
	
	/**
	 * ukazuje tam, kam se ma vlozit novy prvek
	 */
	private int index = 0;

	/**
	 * velikost cache v B
	 */
	private long capacity = 0;
	
	/**
	 * konstruktor - inicializace cache
	 */
	public Clock() {
		this.capacity = GlobalVariables.getCacheCapacity();
		Flist = new ArrayList<Pair<FileOnClient, Boolean>>();
		fOverCapacity = new ArrayList<FileOnClient>();
	}
	
	@Override
	public boolean isInCache(String fName) {
		for (Pair<FileOnClient, Boolean> f : Flist) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		for (Pair<FileOnClient, Boolean> f : Flist) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fName)){
				f.setSecond(true);
				return f.getFirst();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (Pair<FileOnClient, Boolean> f : Flist) {
			obsazeno += f.getFirst().getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		index = index % Flist.size();
		Pair<FileOnClient, Boolean> file = Flist.get(index);
		while(file.getSecond() == true){
			file.setSecond(false);
			index = (index + 1) % Flist.size();
			file = Flist.get(index);
		}
		Flist.remove(file);
	}

	@Override
	public void insertFile(FileOnClient f) {
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vztvorime pro nej okenko
		if (f.getFileSize() > this.capacity){
			if (!fOverCapacity.isEmpty()){
				fOverCapacity.add(f);
				return;
			}
			while (freeCapacity() < (long)((double)this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow()))
				removeFile();
			fOverCapacity.add(f);
			this.capacity = (long) ((double)this.capacity * (1-GlobalVariables.getCacheCapacityForDownloadWindow()));			
			return;
		}
		
		if (!fOverCapacity.isEmpty())
			checkTimes();
			
		//pokud se soubor vejde, fungujeme spravne
		while (freeCapacity() < f.getFileSize()) {
			removeFile();
		}
		
		Flist.add(index, new Pair<FileOnClient, Boolean>(f, true));
		index = (index + 1);
	}
	
	@Override
	public String toString(){
		return "Clock";
	}
	
	@Override
	public boolean needServerStatistics() {
		return false;
	}
	
	@Override
	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}

	@Override
	public void reset() {
		this.Flist.clear();
		this.index = 0;
		this.fOverCapacity.clear();
	}
	
	/**
	 * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache stazene - pak odstranime okenko
	 */
	private void checkTimes() {
		boolean hasBeenRemoved = true;
		while (hasBeenRemoved){
			hasBeenRemoved = false;
			if (!fOverCapacity.isEmpty() && fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()){
				fOverCapacity.remove(0);
				hasBeenRemoved = true;
			}
		}
		if (fOverCapacity.isEmpty()){
			this.capacity = GlobalVariables.getCacheCapacity();
		}
	}
	
	@Override
	public String cacheInfo(){
		return "Clock;Clock";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (capacity ^ (capacity >>> 32));
		result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
		return result;
	}

}
