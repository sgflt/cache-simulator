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
	private final ArrayList<Pair<FileOnClient, Boolean>> Flist;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	
	
	/**
	 * ukazuje tam, kam se ma vlozit novy prvek
	 */
	private int index;

	/**
	 * velikost cache v B
	 */
	private long capacity;
	
	/**
	 * konstruktor - inicializace cache
	 */
	public Clock() {
		this.capacity = GlobalVariables.getCacheCapacity();
		this.Flist = new ArrayList<>();
		this.fOverCapacity = new ArrayList<>();
	}
	
	@Override
	public boolean contains(final String fileName) {
		for (final Pair<FileOnClient, Boolean> f : this.Flist) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fileName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FileOnClient get(final String fileName) {
		for (final Pair<FileOnClient, Boolean> f : this.Flist) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fileName)) {
				f.setSecond(true);
				return f.getFirst();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (final Pair<FileOnClient, Boolean> f : this.Flist) {
			obsazeno += f.getFirst().getFileSize();
		}
		return this.capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		this.index = this.index % this.Flist.size();
		Pair<FileOnClient, Boolean> file = this.Flist.get(this.index);
		while(file.getSecond() == true){
			file.setSecond(false);
			this.index = (this.index + 1) % this.Flist.size();
			file = this.Flist.get(this.index);
		}
		this.Flist.remove(file);
	}

	@Override
	public void insertFile(final FileOnClient f) {
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vztvorime pro nej okenko
		if (f.getFileSize() > this.capacity){
			if (!this.fOverCapacity.isEmpty()) {
				this.fOverCapacity.add(f);
				return;
			}
			while (freeCapacity() < (long) ((double) this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
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

		this.Flist.add(this.index, new Pair<>(f, true));
		this.index = (this.index + 1);
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
	public void setCapacity(final long capacity) {
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
			if (!this.fOverCapacity.isEmpty() && this.fOverCapacity.get(0).getFRemoveTime() < GlobalVariables.getActualTime()) {
				this.fOverCapacity.remove(0);
				hasBeenRemoved = true;
			}
		}
		if (this.fOverCapacity.isEmpty()) {
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
		result = prime * result + (int) (this.capacity ^ (this.capacity >>> 32));
		result = prime * result + ((toString() == null) ? 0 : toString().hashCode());
		return result;
	}

}
