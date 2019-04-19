package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;


/**
 * class for LRDv1 algorithm
 * 
 * SOURCE: Adapted from article "Principles of database buffer management", by
 * W. Effelsberg and T. Haerder
 *  
 * @author Pavel Bžoch
 *
 */
public class LRDv1 implements ICache{

	/**
	 * struktura pro uchovani souboru
	 * druhy argument - Reference counter
	 * treti argument - AT
	 * ctvrty argument - RD 
	 */
	private final ArrayList<Quartet<FileOnClient, Long,Long, Double>> fList;
	
	/**
	 * trida pro porovnani prvku
	 * 
	 * @author Pavel Bzoch
	 * 
	 */
	private class QuartetCompare implements
			Comparator<Quartet<FileOnClient, Long,Long, Double>> {

		@Override
		public int compare(final Quartet<FileOnClient, Long,Long, Double> arg0,
											 final Quartet<FileOnClient, Long,Long, Double> arg1) {
			if ((Double) arg0.getFourth() > (Double) arg1.getFourth()) {
				return 1;
			} else if ((Double) arg0.getFourth() < (Double) arg1.getFourth()) {
				return -1;
			}
			return 0;
		}
	}
	
	/**
	 * kapacita cache
	 */
	private long capacity = 0;
	
	/**
	 * global counter
	 */
	private long GC = 0;
	
	/**
	 * promenna pro nastaveni, zda se ma znovu pocitat RD
	 */
	private boolean needRecalculate = true;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	
	
	/**
	 * konstruktor - iniciace promennych	
	 */
	public LRDv1() {
		super();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fList = new ArrayList<Quartet<FileOnClient,Long,Long,Double>>();
		this.GC = 0;
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(final String fName) {
		for(final Quartet<FileOnClient, Long,Long, Double> files : this.fList){
			if (files.getFirst().getFileName().equalsIgnoreCase(fName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(final String fName) {
		for(final Quartet<FileOnClient, Long,Long, Double> files : this.fList){
			if (files.getFirst().getFileName().equalsIgnoreCase(fName)){
				files.setSecond(files.getSecond() + 1);
				this.needRecalculate = true;
				this.GC++;
				return files.getFirst();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long sumCap = 0;
		for(final Quartet<FileOnClient, Long,Long, Double> files : this.fList){
			sumCap += files.getFirst().getFileSize();
		}
		return this.capacity - sumCap;
	}

	@Override
	public void removeFile() {
		if (this.needRecalculate){
			for(final Quartet<FileOnClient, Long,Long, Double> files : this.fList){
				files.setFourth((double)files.getSecond() / ((double) this.GC - files.getThird()));
			}
			Collections.sort(this.fList, new QuartetCompare());
		}
		this.needRecalculate = false;
		this.fList.remove(0);
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
		while(freeCapacity() < f.getFileSize()){
			removeFile();
		}
		this.fList.add(new Quartet<FileOnClient, Long, Long, Double>(f, (long)1, this.GC++, 1.0));
		this.needRecalculate = true;
	}
	
	@Override
	public String toString(){
		return "LRDv1";
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
		this.fList.clear();
		this.GC = 0;
		this.needRecalculate = true;
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
		return "LRDv1;LRD version 1";
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
