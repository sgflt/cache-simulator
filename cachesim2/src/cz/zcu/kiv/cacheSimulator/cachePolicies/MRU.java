package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * class for MRU algorithm
 * 
 * SOURCE: Adapted from article "An evaluation of buffer management strategies for relational database systems", by
 * H.-T. Chou and D. J. DeWitt
 *  
 * @author Pavel Bžoch
 *
 */
public class MRU implements ICache {

	/**
	 * struktura pro uchovani souboru
	 */
	private ArrayList<FileOnClient> mruQueue;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * velikost cache v B
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * konstruktor - inicializace cache
	 */
	public MRU() {
		mruQueue = new ArrayList<FileOnClient>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	public boolean isInCache(String fName) {
		for (FileOnClient f : mruQueue) {
			if (f.getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		FileOnClient fileForGetting = null;
		for (FileOnClient f : mruQueue) {
			if (f.getFileName().equalsIgnoreCase(fName)) {
				fileForGetting = f;
				break;
			}
		}
		if (fileForGetting == null)
			return null;
		else {
			mruQueue.remove(fileForGetting);
			mruQueue.add(fileForGetting);
			return fileForGetting;
		}
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (FileOnClient f : mruQueue) {
			obsazeno += f.getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		mruQueue.remove(mruQueue.size() - 1);
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
		mruQueue.add(f);
	}
	
	@Override
	public String toString(){
		return "MRU";
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
		this.mruQueue.clear();
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
		return "MRU;MRU";
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
		mruQueue.remove(f);
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(mruQueue.size());
		mruQueue.addAll(mruQueue);
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}

}
