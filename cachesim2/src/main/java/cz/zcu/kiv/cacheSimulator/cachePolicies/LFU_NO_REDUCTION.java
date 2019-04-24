package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


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
	 * struktura pro ukladani souboru, ktere jsou vetsi nez cache
	 */
	private final List<FileOnClient> fOverCapacity = new ArrayList<>();
	

	/**
	 * velikost cache v kB
	 */
	private long capacity;
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
	}

	@Override
	public FileOnClient get(final String fileName) {
		for (final var metaData : this.list) {
			if (metaData.getFileOnClient().getFileName().equalsIgnoreCase(fileName)) {
				metaData.increaseReadHits();
				this.needSort = true;
				return metaData.getFileOnClient();
			}
		}
		return null;
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
		this.list.add(new MetaData(fileOnClient));
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
