package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * class for FIFO algorithm
 * 
 * SOURCE: Adapted from article "An anomaly in space-time characteristics of certain programs running in a paging machine", 
 * by L. A. Belady, R. A. Nelson and G. S. Shedler
 * 
 * @author Pavel Bžoch
 *
 */
public class FIFO implements ICache {

	/**
	 * struktura pro uchovani souboru
	 */
	private Queue<FileOnClient> fQueue;
	
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
	public FIFO() {
		fQueue = new LinkedList<FileOnClient>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	public boolean isInCache(String fName) {
		for (FileOnClient f : fQueue) {
			if (f.getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		for (FileOnClient f : fQueue) {
			if (f.getFileName().equalsIgnoreCase(fName))
				return f;
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (FileOnClient f : fQueue) {
			obsazeno += f.getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		fQueue.remove();
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
		fQueue.add(f);
	}
	
	@Override
	public String toString(){
		return "FIFO";
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
		this.fQueue.clear();
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
		return "FIFO;FIFO";
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
		if (fQueue.contains(f))
			fQueue.remove(f);
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(fQueue.size());
		list.addAll(fQueue);
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
