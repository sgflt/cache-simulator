package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


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
		public int compare(Pair<Integer, FileOnClient> o1, Pair<Integer, FileOnClient> o2) {
			if ((Integer) o1.getFirst() > (Integer) o2.getFirst())
				return 1;
			else if ((Integer) o1.getFirst() < (Integer) o2.getFirst())
				return -1;
			return 0;
		}
	}

	/**
	 * struktura pro uchovani souboru
	 */
	private ArrayList<Pair<Integer, FileOnClient>> list;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * velikost cache v kB
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;

	/**
	 * promenne pro urceni, jestli je treba tridit
	 */
	private boolean needSort = true;

	/**
	 * konstruktor - inicializace cache
	 */
	public LFU_NO_REDUCTION() {
		list = new ArrayList<Pair<Integer, FileOnClient>>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for (Pair<Integer, FileOnClient> f : list) {
			if (f.getSecond().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		for (Pair<Integer, FileOnClient> f : list) {
			if (f.getSecond().getFileName().equalsIgnoreCase(fName)) {
				f.setFirst(f.getFirst() + 1);
				needSort = true;
				return f.getSecond();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (Pair<Integer, FileOnClient> f : list) {
			obsazeno += f.getSecond().getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		if (needSort) {
			Collections.sort(list, new PairCompare());
		}
		needSort = false;
		if (list.size() > 0)
			list.remove(0);
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
		list.add(new Pair<Integer, FileOnClient>(new Integer(1), f));
		needSort = true;
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
	public void setCapacity(long capacity) {
		this.capacity = capacity;
		this.initialCapacity = capacity;
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
		return "LFU_NO_REDUCTION;Standard LFU";
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
		Pair<Integer, FileOnClient> pair = null;
		for (Pair<Integer, FileOnClient> file : list){
			if (file.getSecond() == f){
				pair = file;
				break;
			}
		}
		if (pair != null){
			list.remove(pair);
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(this.list.size());
		for (Pair<Integer, FileOnClient> file : this.list){
			list.add(file.getSecond());
		}
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
