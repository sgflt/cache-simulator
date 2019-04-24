package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


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
	private final Queue<FileOnClient> fQueue;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * velikost cache v B
	 */
	private long capacity = 0;

	/**
	 * konstruktor - inicializace cache
	 */
	public FIFO() {
		this.fQueue = new LinkedList<>();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public FileOnClient get(final String fileName) {
		for (final FileOnClient f : this.fQueue) {
			if (f.getFileName().equalsIgnoreCase(fileName)) {
        return f;
      }
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (final FileOnClient f : this.fQueue) {
			obsazeno += f.getFileSize();
		}
		return this.capacity - obsazeno;
	}

	@Override
	public void removeFile() {
    this.fQueue.remove();
	}

	@Override
	public void insertFile(final FileOnClient f) {
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vztvorime pro nej okenko
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
    this.fQueue.add(f);
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
	public void setCapacity(final long capacity) {
		this.capacity = capacity;
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
		return "FIFO;FIFO";
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
