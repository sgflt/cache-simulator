package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * class for RND algorithm
 * 
 * SOURCE: Adapted from article "Analysis of caching algorithms for distributed file systems", by
 * B. Reed and D. D. E. Long
 *  
 * @author Pavel Bžoch
 *
 */
public class RND implements ICache {

	/**
	 * struktura pro ukladani souboru
	 */
	private ArrayList<FileOnClient> list;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * kapacity cache v Bytech
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;
	
	/**
	 * konstanta pro udani seed value pro nahodny generatoe
	 */
	private final int seedValue = 0;

	/**
	 * pro nahodne generovane indexy vyhazovanych souboru
	 */
	Random rnd = null;

	/**
	 * konstruktor - inicializace promennych
	 * 
	 * @param capacity
	 */
	public RND() {
		list = new ArrayList<FileOnClient>();
		rnd = new Random(seedValue);
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (FileOnClient f : list) {
			if (f.getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		for (FileOnClient f : list) {
			if (f.getFileName().equalsIgnoreCase(fName))
				return f;
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (FileOnClient f : list) {
			obsazeno += f.getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		list.remove(rnd.nextInt(list.size()));

	}

	@Override
	public void insertFile(FileOnClient f) {
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vytvorime pro nej okenko
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
		list.add(f);
	}
	
	@Override
	public String toString(){
		return "RND";
	}
	
	@Override
	public boolean needServerStatistics() {
		return false;
	}
	
	@Override
	public void setCapacity(long capacity) {
		this.capacity = capacity;
		this.initialCapacity = capacity;
	}

	@Override
	public void reset() {
		this.list.clear();
		rnd = new Random(seedValue);
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
			this.capacity = this.initialCapacity;
		}
	}
	
	@Override
	public String cacheInfo(){
		return "RND;Random";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (initialCapacity ^ (initialCapacity >>> 32));
		result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
		return result;
	}
	
	@Override
	public void removeFile(FileOnClient f) {
		if (list.contains(f))
			list.remove(f);
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(this.list.size());
		list.addAll(this.list);
		return list;
	}
	
	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}

}
