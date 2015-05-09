package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


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
	private ArrayList<Quartet<FileOnClient, Long,Long, Double>> fList;
	
	/**
	 * trida pro porovnani prvku
	 * 
	 * @author Pavel Bzoch
	 * 
	 */
	private class QuartetCompare implements
			Comparator<Quartet<FileOnClient, Long,Long, Double>> {

		@Override
		public int compare(Quartet<FileOnClient, Long,Long, Double> arg0,
				Quartet<FileOnClient, Long,Long, Double> arg1) {
			if ((Double) arg0.getFourth() > (Double) arg1.getFourth())
				return 1;
			else if ((Double) arg0.getFourth() < (Double) arg1.getFourth())
				return -1;
			return 0;
		}
	}
	
	/**
	 * kapacita cache
	 */
	private long capacity = 0;
	
	/**
	 * pocatecni kapacita cache
	 */
	private long initialCapacity = 0;
	
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
	private ArrayList<FileOnClient> fOverCapacity;
	
	
	/**
	 * konstruktor - iniciace promennych	
	 */
	public LRDv1() {
		super();
		this.fList = new ArrayList<Quartet<FileOnClient,Long,Long,Double>>();
		this.GC = 0;
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		for(Quartet<FileOnClient, Long,Long, Double> files : fList){
			if (files.getFirst().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		for(Quartet<FileOnClient, Long,Long, Double> files : fList){
			if (files.getFirst().getFileName().equalsIgnoreCase(fName)){
				files.setSecond(files.getSecond() + 1);
				needRecalculate = true;
				GC++;
				return files.getFirst();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long sumCap = 0;
		for(Quartet<FileOnClient, Long,Long, Double> files : fList){
			sumCap += files.getFirst().getFileSize();
		}
		return this.capacity - sumCap;
	}

	@Override
	public void removeFile() {
		if (needRecalculate){
			for(Quartet<FileOnClient, Long,Long, Double> files : fList){
				files.setFourth((double)files.getSecond() / ((double)GC - files.getThird()));
			}
			Collections.sort(fList, new QuartetCompare());
		}
		needRecalculate = false;
		fList.remove(0);
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
		while(freeCapacity() < f.getFileSize()){
			removeFile();
		}
		fList.add(new Quartet<FileOnClient, Long, Long, Double>(f, (long)1, GC++, 1.0));
		needRecalculate = true;
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
	public void setCapacity(long capacity) {
		this.capacity = capacity;
		this.initialCapacity = capacity;
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
		return "LRDv1;LRD version 1";
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
		Quartet<FileOnClient, Long,Long, Double> quart = null;
		for (Quartet<FileOnClient, Long,Long, Double> file : fList){
			if (file.getFirst() == f){
				quart = file;
				break;
			}
		}
		if (quart != null){
			fList.remove(quart);
		}
		
	}

	@Override
	public List<FileOnClient> getCachedFiles() {
		List<FileOnClient> list = new ArrayList<FileOnClient>(fList.size());
		for (Quartet<FileOnClient, Long,Long, Double> file : fList){
			list.add(file.getFirst());
		}
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
