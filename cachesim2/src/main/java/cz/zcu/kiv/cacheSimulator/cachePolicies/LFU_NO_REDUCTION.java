package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;


/**
 * class for LFU with no reduction of references' count algorithm
 * 
 * SOURCE: Adapted from article "Analysis of caching algorithms for distributed file systems", by 
 * B. Reed and D. D. E. Long
 * 
 * @author Pavel Bžoch
 *
 */
public class LFU_NO_REDUCTION implements ICache {

	/**
	 * trida pro porovnani prvku
	 * 
	 * @author Pavel Bzoch
	 * 
	 */
	private class PairCompare implements Comparator<Pair<Integer, FileOnClient>> {

		@Override
		public int compare(final Pair<Integer, FileOnClient> o1, final Pair<Integer, FileOnClient> o2) {
			if (o1.getFirst() > o2.getFirst()) {
				return 1;
			} else if (o1.getFirst() < o2.getFirst()) {
				return -1;
			}
			return 0;
		}
	}

	/**
	 * struktura pro uchovani souboru
	 */
	private final ArrayList<Pair<Integer, FileOnClient>> list;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * velikost cache v kB
	 */
	private long capacity;

	/**
	 * promenne pro urceni, jestli je treba tridit
	 */
	private boolean needSort = true;

	/**
	 * konstruktor - inicializace cache
	 */
	public LFU_NO_REDUCTION() {
		this.list = new ArrayList<>();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public boolean isInCache(final String fName) {
		for (final Pair<Integer, FileOnClient> f : this.list) {
			if (f.getSecond().getFileName().equalsIgnoreCase(fName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(final String fName) {
		for (final Pair<Integer, FileOnClient> f : this.list) {
			if (f.getSecond().getFileName().equalsIgnoreCase(fName)) {
				f.setFirst(f.getFirst() + 1);
				this.needSort = true;
				return f.getSecond();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (final Pair<Integer, FileOnClient> f : this.list) {
			obsazeno += f.getSecond().getFileSize();
		}
		return this.capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		if (this.needSort) {
			this.list.sort(new PairCompare());
		}
		this.needSort = false;
		if (!this.list.isEmpty()) {
			this.list.remove(0);
		}
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
		this.list.add(new Pair<>(1, f));
		this.needSort = true;
	}
	
	@Override
	public String toString(){
		return "Standard LFU";
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
		this.needSort = true;
		this.list.clear();
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
		return "LFU_NO_REDUCTION;Standard LFU";
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
