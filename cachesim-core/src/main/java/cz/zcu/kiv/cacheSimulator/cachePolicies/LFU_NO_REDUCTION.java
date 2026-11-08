package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileKey;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
	 * struktura pro uchovani souboru
	 */
	private final List<MetaData> list = new ArrayList<>();

	/**
	 * the same entries by name key, for lookups
	 */
	private final Map<String, MetaData> byName = new HashMap<>();
	
	/**
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final List<FileOnClient> fOverCapacity = new ArrayList<>();
	

	/**
	 * velikost cache v kB
	 */
	private long capacity;
	/**
	 * the cache size set by setCapacity, restored when the download window closes
	 */
	private long configuredCapacity;
	private long usedCapacity;

	/**
	 * promenne pro urceni, jestli je treba tridit
	 */
	private boolean needSort = true;

	/**
	 * konstruktor - inicializace cache
	 */
	public LFU_NO_REDUCTION() {
		this.capacity = GlobalVariables.getCacheCapacity();
		this.configuredCapacity = this.capacity;
	}

	@Override
	public FileOnClient get(final String fileName) {
		final var metaData = this.byName.get(FileKey.of(fileName));
		if (metaData == null) {
			return null;
		}
		metaData.increaseReadHits();
		this.needSort = true;
		return metaData.getFileOnClient();
	}

	@Override
	public long freeCapacity() {
		return this.capacity - this.usedCapacity;
	}

	@Override
	public void removeFile() {
		if (this.needSort) {
			this.list.sort(Comparator.comparing(MetaData::getReadHits));
		}
		this.needSort = false;
		if (!this.list.isEmpty()) {
			final MetaData removedFile = this.list.remove(0);
			this.byName.remove(FileKey.of(removedFile.getFileOnClient().getFileName()));
			this.usedCapacity -= removedFile.getFileOnClient().getFileSize();
		}
	}

	@Override
	public void insertFile(final FileOnClient fileOnClient) {
		//napred zkontrolujeme, jestli se soubor vejde do cache
		//pokud se nevejde, vztvorime pro nej okenko
		if (fileOnClient.getFileSize() > this.capacity) {
			if (!this.fOverCapacity.isEmpty()){
				this.fOverCapacity.add(fileOnClient);
				return;
			}
			while (freeCapacity() < (long)((double)this.capacity * GlobalVariables.getCacheCapacityForDownloadWindow())) {
				removeFile();
			}
			this.fOverCapacity.add(fileOnClient);
			this.capacity = (long) ((double)this.capacity * (1-GlobalVariables.getCacheCapacityForDownloadWindow()));
			return;
		}
		
		if (!this.fOverCapacity.isEmpty()) {
			checkTimes();
		}
			
		//pokud se soubor vejde, fungujeme spravne
		while (freeCapacity() < fileOnClient.getFileSize()) {
			removeFile();
		}
		final MetaData entry = new MetaData(fileOnClient);
		this.list.add(entry);
		this.byName.put(FileKey.of(fileOnClient.getFileName()), entry);
		this.usedCapacity += fileOnClient.getFileSize();
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
		this.configuredCapacity = capacity;
	}

	@Override
	public void reset() {
		this.capacity = this.configuredCapacity;
		this.needSort = true;
		this.list.clear();
		this.byName.clear();
		this.usedCapacity = 0;
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
			this.capacity = this.configuredCapacity;
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

	private static class MetaData {

		private final FileOnClient fileOnClient;
		private int readHits = 1;

		MetaData(final FileOnClient fileOnClient) {
			this.fileOnClient = fileOnClient;
		}

		FileOnClient getFileOnClient() {
			return this.fileOnClient;
		}

		int getReadHits() {
			return this.readHits;
		}

		void increaseReadHits() {
			++this.readHits;
		}
	}
}
