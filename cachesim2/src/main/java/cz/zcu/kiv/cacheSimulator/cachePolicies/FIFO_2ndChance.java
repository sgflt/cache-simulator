package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * class for FIFO with second chance algorithm
 * 
 * SOURCE: Adapted from article "Page Replacement and Reference Bit Emulation in Mach", 
 * by R. P. Draves
 * 
 * @author Pavel Bžoch
 *
 */
public class FIFO_2ndChance implements ICache{

	/**
	 * struktura pro uchovani souboru
	 */
	private final Queue<Pair<FileOnClient, Boolean>> fQueue;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	

	/**
	 * velikost cache v B
	 */
	private long capacity;
	
	/**
	 * konstruktor - inicializace cache
	 */
	public FIFO_2ndChance() {
		this.fQueue = new LinkedList<>();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fOverCapacity = new ArrayList<>();
	}

	@Override
	public FileOnClient get(final String fileName) {
		for (final Pair<FileOnClient, Boolean> f : this.fQueue) {
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
		for (final Pair<FileOnClient, Boolean> f : this.fQueue) {
			obsazeno += f.getFirst().getFileSize();
		}
		return this.capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		Pair<FileOnClient, Boolean> file = this.fQueue.remove();
		while (file.getSecond()) {
			file.setSecond(false);
			this.fQueue.add(file);
			file = this.fQueue.remove();
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
		this.fQueue.add(new Pair<>(f, false));
	}
	
	@Override
	public String toString(){
		return "FiFO 2nd chance";
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
		return "FIFO_2ndChance;FIFO 2nd chance";
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
