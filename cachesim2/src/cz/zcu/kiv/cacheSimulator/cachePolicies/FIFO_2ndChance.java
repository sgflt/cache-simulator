package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


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
	private Queue<Pair<FileOnClient, Boolean>> fQueue;
	
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
	public FIFO_2ndChance() {
		fQueue = new LinkedList<Pair<FileOnClient, Boolean>>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}
	
	@Override
	public boolean isInCache(String fName) {
		for (Pair<FileOnClient, Boolean> f : fQueue) {
			if (f.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		for (Pair<FileOnClient, Boolean> f : fQueue) {
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
		for (Pair<FileOnClient, Boolean> f : fQueue) {
			obsazeno += f.getFirst().getFileSize();
		}
		return capacity - obsazeno;
	}

	@Override
	public void removeFile() {
		Pair<FileOnClient, Boolean> file = fQueue.remove();
		while(file.getSecond() == true){
			file.setSecond(false);
			fQueue.add(file);
			file = fQueue.remove();
		}
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
		fQueue.add(new Pair<FileOnClient, Boolean>(f, false));
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
		return "FIFO_2ndChance;FIFO 2nd chance";
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
		Pair<FileOnClient, Boolean> pair = null;
		for (Pair<FileOnClient, Boolean> file : fQueue){
			if (file.getFirst() == f){
				pair = file;
				break;
			}
		}
		if (pair != null){
			fQueue.remove(pair);
		}
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(fQueue.size());
		for (Pair<FileOnClient, Boolean> file : fQueue){
			list.add(file.getFirst());
		}
		return list;
	}
	
	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
