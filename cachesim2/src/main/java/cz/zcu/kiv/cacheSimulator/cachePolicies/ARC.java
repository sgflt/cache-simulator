package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;


/**
 * class for ARC algorithm
 * 
 * SOURCE: Adapted from article "ARC: A Self-Tuning, Low Overhead Replacement Cache", 
 * by Nimrod Megiddo , Dharmendra Modha
 * 
 * @author Pavel Bžoch
 *
 */
public class ARC implements ICache {

	/**
	 * argument pro adaptaci
	 */
	private double p = 0;

	/**
	 * listy pro uchovani souboru
	 */
	private final ArrayList<FileOnClient> B1;
	private final ArrayList<FileOnClient> B2;
	private final ArrayList<FileOnClient> T1;
	private final ArrayList<FileOnClient> T2;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final ArrayList<FileOnClient> fOverCapacity;
	
	/**
	 * kapacita cache
	 */
	private long capacity;
	
	public ARC() {
		super();
		this.capacity = GlobalVariables.getCacheCapacity();
		this.B1 = new ArrayList<FileOnClient>();
		this.B2 = new ArrayList<FileOnClient>();
		this.T1 = new ArrayList<FileOnClient>();
		this.T2 = new ArrayList<FileOnClient>();
		this.fOverCapacity = new ArrayList<FileOnClient>();
	}

	@Override
	public boolean isInCache(final String fName) {
		for (final FileOnClient file : this.T1) {
			if (file.getFileName().equalsIgnoreCase(fName)) {
				return true;
			}
		}
		for (final FileOnClient file : this.T2) {
			if (file.getFileName().equalsIgnoreCase(fName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(final String fName) {
		FileOnClient file = null;
		for (final FileOnClient actFile : this.T1) {
			if (actFile.getFileName().equalsIgnoreCase(fName)) {
				file = actFile;
				break;
			}
		}
		if (file != null) {
			this.T1.remove(file);
			this.T2.add(file);
		}
		for (final FileOnClient actFile : this.T2) {
			if (actFile.getFileName().equalsIgnoreCase(fName)) {
				file = actFile;
				break;
			}
		}
		if (file != null) {
			this.T2.remove(file);
			this.T2.add(file);
			return file;
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long sumCap = 0;
		for (final FileOnClient file : this.T1) {
			sumCap += file.getFileSize();
		}
		for (final FileOnClient file : this.T2) {
			sumCap += file.getFileSize();
		}
		return this.capacity - sumCap;
	}

	@Override
	public void removeFile() {

	}

	@Override
	//TODO
	public void insertFile(final FileOnClient f) {
		
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vztvorime pro nej okenko
		//TODO - upravit seznamy
		if (f.getFileSize() > this.capacity){
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
		int swi = 2;
		while (freeCapacity() < f.getFileSize()) {
			swi = 2;
			// soubor je v B1
			if (this.B1.indexOf(f) != -1) {
				this.p = Math.min(this.capacity,
					this.p + Math.max((double) B2cap() / B1cap(), 1));
				replace(this.p, false);
				swi = 1;
				continue;
			}
			// soubor je v B2
			if (this.B2.indexOf(f) != -1) {
				this.p = Math.max(0, this.p - Math.max((double) B1cap() / B2cap(), 1));
				replace(this.p, true);
				swi = 1;
				continue;
			}
			//soubor neni ani v T1, ani v T2, ani v B1, ani v B2
			if (T1cap() + B1cap() > this.capacity){
				if (T1cap() < this.capacity){
					this.B1.remove(0);
					replace(this.p, false);
				}
				else{
					this.T1.remove(0);
				}
				continue;
			}
			else if (T1cap() + T2cap() + B1cap() + B2cap() >= this.capacity){
				if (T1cap() + T2cap() + B1cap() + B2cap() >=  2 * this.capacity){
					if (this.B2.size() > 0){
						this.B2.remove(0);
					}
				}
				replace(this.p, false);
				continue;
			}
			else {
				if (this.T1.size() > 0){
					this.B1.add(this.T1.get(0));
					this.T1.remove(0);
					continue;
				}
				this.B2.add(this.T2.get(0));
				this.T2.remove(0);
			}
		}
		if (swi == 1) {
			this.T2.add(f);
		} else {
			this.T1.add(f);
		}
	}

	/**
	 * metoda replace
	 * 
	 * @param p
	 */
	private void replace(final double p, final boolean inB2) {
		if (this.T1.size() > 0
				&& (T1cap() > p || (inB2 && T1cap() > p))) {
			this.B1.add(this.T1.get(0));
			this.T1.remove(0);
		} else if (this.T2.size() > 0) {
			this.B2.add(this.T2.get(0));
			this.T2.remove(0);
		} else {
			this.B1.add(this.T1.get(0));
			this.T1.remove(0);
		}
	}

	/**
	 * metoda, ktera vrati obsazene misto B1
	 * 
	 * @return
	 */
	private long B1cap() {
		long sumCap = 0;
		for (final FileOnClient file : this.B1) {
			sumCap += file.getFileSize();
		}
		return sumCap;
	}

	/**
	 * metoda, ktera vrati obsazene misto B1
	 * 
	 * @return
	 */
	private long B2cap() {
		long sumCap = 0;
		for (final FileOnClient file : this.B2) {
			sumCap += file.getFileSize();
		}
		return sumCap;
	}

	/**
	 * metoda, ktera vrati obsazene misto B1
	 * 
	 * @return
	 */
	private long T1cap() {
		long sumCap = 0;
		for (final FileOnClient file : this.T1) {
			sumCap += file.getFileSize();
		}
		return sumCap;
	}

	/**
	 * metoda, ktera vrati obsazene misto B1
	 * 
	 * @return
	 */
	private long T2cap() {
		long sumCap = 0;
		for (final FileOnClient file : this.T2) {
			sumCap += file.getFileSize();
		}
		return sumCap;
	}
	
	@Override
	public String toString(){
		return "ARC";
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
		this.p = 0;
		this.B1.clear();
		this.B2.clear();
		this.T1.clear();
		this.T2.clear();
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
		return "ARC;ARC";
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
