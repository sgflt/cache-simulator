package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;


/**
 * class for LFU-SS algorithm
 *  
 * @author Pavel B�och
 *
 */
public class LFU_SS implements ICache {

	/**
	 * trida pro porovnani prvku
	 * 
	 * @author Pavel Bzoch
	 * 
	 */
	protected class PairCompare implements Comparator<Pair<Double, FileOnClient>> {

		@Override
		public int compare(Pair<Double, FileOnClient> o1, Pair<Double, FileOnClient> o2) {
			if ((Double) o1.getFirst() > (Double) o2.getFirst())
				return 1;
			else if ((Double) o1.getFirst() < (Double) o2.getFirst())
				return -1;
			return 0;
		}
	}

	/**
	 * struktura pro uchovani souboru
	 */
	protected List<Pair<Double, FileOnClient>> list;
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private List<FileOnClient> fOverCapacity;
	

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
	protected boolean needSort = true;

	/**
	 * poromenna pro urceni globalniho poctu hitu na cteni
	 */
	protected long globalReadCount = Long.MAX_VALUE;
	
	/**
	 * promenna pro urceni poctu pristupu do cache a pro aktualizaci globalnich statistik
	 */
	private long accessCount = 0;
	
	/**
	 * promenna pro uchovani odkazu na server
	 */
	private Server server = Server.getInstance();

	/**
	 * konstruktor - inicializace cache
	 */
	public LFU_SS() {
		list = new LinkedList<Pair<Double, FileOnClient>>();
		this.fOverCapacity = new LinkedList<FileOnClient>();
	}

	@Override
	public boolean isInCache(String fName) {
		accessCount++;
		if (accessCount % 20 == 0){
			setGlobalReadCountServer(server.getGlobalReadHits(this));
		}
		for (Pair<Double, FileOnClient> f : list) {
			if (f.getSecond().getFileName().equalsIgnoreCase(fName))
				return true;
		}
		return false;
	}

	@Override
	public FileOnClient getFileFromCache(String fName) {
		Pair<Double, FileOnClient> pair = null;
		for (Pair<Double, FileOnClient> f : list) {
			if (f.getSecond().getFileName().equalsIgnoreCase(fName)) {
				pair = f;
				pair.setFirst(pair.getFirst() + 1.0);
				needSort = true;
				return pair.getSecond();
			}
		}
		return null;
	}

	@Override
	public long freeCapacity() {
		long obsazeno = 0;
		for (Pair<Double, FileOnClient> f : list) {
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

		if (list.size() > 2)
			if ((list.get(list.size() - 1)).getFirst() > 15) {
				for (Pair<Double, FileOnClient> f : list) {
					f.setFirst(f.getFirst() / 2);
				}
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
		double localReadCount = 0;
		for (Pair<Double, FileOnClient> files : list) {
			localReadCount += files.getFirst();
		}
		double readHits = ((double) f.getReadHit() - (double) f.getWriteHit())
				/ (double) globalReadCount * (double) localReadCount + 1.0;
		list.add(new Pair<Double, FileOnClient>(new Double(readHits), f));
		needSort = true;
	}

	/**
	 * metoda pro nastaveni poctu globalnich hitu
	 * 
	 * @param readCount
	 *            pocet hitu
	 */
	private void setGlobalReadCountServer(long readCount) {
		this.globalReadCount = readCount;
	}
	
	@Override
	public String toString(){
		return "LFU-SS";
	}
	
	@Override
	public boolean needServerStatistics() {
		return true;
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
		this.globalReadCount = Long.MAX_VALUE;
		this.accessCount = 0;
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
		return "LFU_SS;LFU-SS";
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
		Pair<Double, FileOnClient> pair = null;
		for (Pair<Double, FileOnClient> file : list){
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
		for (Pair<Double, FileOnClient> file : this.list){
			list.add(file.getSecond());
		}
		return list;
	}

	@Override
	public long getCacheCapacity() {
		return this.initialCapacity;
	}
}
