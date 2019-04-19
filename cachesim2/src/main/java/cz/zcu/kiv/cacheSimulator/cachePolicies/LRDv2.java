package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;


/**
 * class for LRDv2 algorithm
 * 
 * SOURCE: Adapted from article "Principles of database buffer management", by
 * W. Effelsberg and T. Haerder
 *  
 * @author Pavel Bžoch
 *
 */
public class LRDv2 implements ICache{

	/**
	 * struktura pro uchovani souboru
	 * druhy argument - Reference counter
	 * treti argument - AT
	 * ctvrty argument - RD 
	 */
	private final ArrayList<Quartet<FileOnClient, Long,Long, Double>> fList;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	
	
	/**
	 * konstanta pro snizovani poctu referenci 
	 */
	private static double K1 = 1.8f;
	
	/**
	 * interval pro snizovani poctu referenci
	 */
	private static int INTERVAL = 20;

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
			if (arg0.getFourth() > arg1.getFourth()) {
				return 1;
			} else if (arg0.getFourth() < arg1.getFourth()) {
				return -1;
			}
			return 0;
		}
	}
	
	/**
	 * kapacita cache
	 */
	private long capacity;
	
	/**
	 * global counter
	 */
	private long GC;
	
	/**
	 * promenna pro nastaveni, zda se ma znovu pocitat RD
	 */
	private boolean needRecalculate = true;
	
		
	public LRDv2() {
		this.capacity = GlobalVariables.getCacheCapacity();
		this.fList = new ArrayList<>();
		this.GC = 0;
		this.fOverCapacity = new ArrayList<>();
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
				this.GC++;
				this.needRecalculate = true;
				recalculateReferences();
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
			this.fList.sort(new QuartetCompare());
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
		this.fList.add(new Quartet<>(f, (long) 1, this.GC++, 1.0));
		this.needRecalculate = true;
		recalculateReferences();
	}

	/**
	 * metoda pro rekalkulkaci poctu referenci
	 */
	private void recalculateReferences() {
		if (this.GC % INTERVAL == 0) {
			for(final Quartet<FileOnClient, Long,Long, Double> files : this.fList){
				files.setSecond((long)(files.getSecond() / K1));
			}
		}
	}
	
	@Override
	public String toString(){
		return "LRDv2";
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
		return "LRDv2;LRD version 2";
	}
        
        public static double getK1() {
            return K1;
        }

        public static void setK1(final double K1) {
            LRDv2.K1 = K1;
        }

        public static int getINTERVAL() {
            return INTERVAL;
        }

        public static void setINTERVAL(final int INTERVAL) {
            LRDv2.INTERVAL = INTERVAL;
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