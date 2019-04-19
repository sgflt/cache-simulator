package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Random;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;


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
	private final ArrayList<FileOnClient> list;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * kapacity cache v Bytech
	 */
	private long capacity = 0;
	
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
		this.list = new ArrayList<FileOnClient>();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.rnd = new Random(this.seedValue);
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(final String fName) {
		for (final FileOnClient f : this.list) {
			if (f.getFileName().equalsIgnoreCase(fName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(final String fName) {
		for (final FileOnClient f : this.list) {
			if (f.getFileName().equalsIgnoreCase(fName)) {
				return f;
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (final FileOnClient f : this.list) {
			obsazeno += f.getFileSize();
		}
		return this.capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		this.list.remove(this.rnd.nextInt(this.list.size()));

	}

	@Override
	public void insertFile(final FileOnClient f) {
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vytvorime pro nej okenko
		if (f.getFileSize() > this.capacity){
			if (!this.fOverCapacity.isEmpty()){
				this.fOverCapacity.add(f);
				return;
			}
			while (freeCapacity() < (long)((double)this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
				removeFile();
			}
			this.fOverCapacity.add(f);
			this.capacity = (long) ((double)this.capacity * (1-GlobalVariables.getCacheCapacityForDownloadWindow()));
		return;
		}
		
		if (!this.fOverCapacity.isEmpty()) {
			checkTimes();
		}
			
		//pokud se soubor vejde, fungujeme spravne
		while (freeCapacity() < f.getFileSize()) {
			removeFile();
		}
		this.list.add(f);
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
	public void setCapacity(final long capacity) {
		this.capacity = capacity;
	}

	@Override
	public void reset() {
		this.list.clear();
		this.rnd = new Random(this.seedValue);
		this.fOverCapacity.clear();
	}
	
	/**
	 * metoda pro kontrolu, zda jiz nejsou soubory s vetsi velikosti nez cache stazene - pak odstranime okenko
	 */
	private void checkTimes() {
		boolean hasBeenRemoved = true;
		while (hasBeenRemoved){
			hasBeenRemoved = false;
			if (!this.fOverCapacity.isEmpty() && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()){
				this.fOverCapacity.remove(0);
				hasBeenRemoved = true;
			}
		}
		if (this.fOverCapacity.isEmpty()){
			this.capacity = GlobalVariables.getCacheCapacity();
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
		result = prime * result + (int) (this.capacity ^ (this.capacity >>> 32));
		result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
		return result;
	}

}
