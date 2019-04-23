package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.shared.GlobalMethods;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * trida pro reprezentaci simulovaneho uzivatele kazdy uzivatel ma svou sadu
 * cachovacich algoritmu, sve jednoznacne ID
 * 
 * @author Pavel Bzoch
 * 
 */
public class SimulatedUser {

	/**
	 * identifikator uzivatele
	 */
	private final long ID;

	/**
	 * promenna pro uchovani cachovacich algoritmu prvni je pro uchovani odkazu
	 * na cache, druha je pro urceni cacheHit, treti je pro urceni saved traffic
	 */
	private List<Measurement> measurements;

	/**
	 * promenna pro uchovani archivu
	 */
	private final List<Statistics> cachesResults = new ArrayList<>();

	/**
	 * promenna pro ulozeni celkoveho poctu pristupu k souborum
	 */
	private long fileAccessed = 0;


	/**
	 * promenna pro ulozeni celkoveho potrebneho datoveho prenosu
	 */
	private long totalNetworkBandwidth = 0;


	/**
	 * konstruktor - inicializace user ID
	 * 
	 * @param iD
	 *            user ID
	 */
	public SimulatedUser(final long iD) {
		this.ID = iD;
		loadCaches(MainGUI.getInstance().getCacheSizes());
	}

	/**
	 * metoda, ktera podle nasatveni v global variables nacte cachovaci
	 * algoritmy
	 */
	private void loadCaches(final Integer[] cacheSizes) {
		if (this.measurements != null) {
			this.measurements.clear();
		}
		this.measurements = new ArrayList<>();

		ICache cache;
		for (final String cacheName : MainGUI.getInstance().getCachesNames()) {
			for (int i = 0; i < cacheSizes.length; i++) {
				try {
					cache = (ICache) Class.forName(
							"cz.zcu.kiv.cacheSimulator.cachePolicies." + cacheName)
							.newInstance();
					cache.setCapacity(cacheSizes[i]*1024*1024);
					this.measurements.add(new Measurement(cache));
				} catch (final Exception ex) {
					Logger.getLogger(SimulatedUser.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		}
	}

	/**
	 * metoda pro ziskani ID
	 * 
	 * @return id uzivatele
	 */
	public long getID() {
		return this.ID;
	}

	public long getFileAccessed() {
		return this.fileAccessed;
	}

	public long getTotalNetworkBandwidth() {
		return this.totalNetworkBandwidth;
	}

	/**
	 * metoda pro ziskani seznamu cachovacich algoritmu, ktere odpovidaji
	 * uzivateli
	 * 
	 * @return seznam algoritmu
	 */
	public List<Measurement> getMeasurements() {
		return this.measurements;
	}

	/**
	 * metoda pro zvyseni poctu pristupovanych souboru v ramci tohoto
	 * cachovaciho algoritmu
	 */
	public void incereaseFileAccess() {
		this.fileAccessed += 1;
	}

	/**
	 * metoda pro zvyseni celkoveho poctu prenesenych bytu
	 * 
	 * @param fileSize
	 *            velikost pridavaneho souboru
	 */
	public void increaseTotalNetworkBandwidth(final long fileSize) {
		this.totalNetworkBandwidth += fileSize;
	}

	/**
	 * metoda pro vytisteni statistik
	 */
	public void printStatistics() {
		if (this.fileAccessed < GlobalVariables.getLimitForStatistics()) {
			return;
		}
		final long id = this.ID >> 32;
		final String ip = (GlobalMethods.intToIp(this.ID - (id << 32)));
		System.out.println("=================== Statistics for user id: " + id
				+ ", ip: " + ip + " ===================\n");
		for (final var measurement : this.measurements) {
			System.out
				.println(measurement.getCache()
							+ " read hits: "
					+ measurement.getMetrics().getCacheHits()
							+ ", saved capacity: "
					+ measurement.getMetrics().getSavedBandthwidth()
							+ ", cache hit ratio: "
						//	+ (double) (((int) (cache.getSecond() * 10000 / fileAccessed)) / 100.0)
							+ "%");
		}
		System.out.println("\nFiles requested: " + this.fileAccessed
			+ ", total file sizes: " + this.totalNetworkBandwidth + "\n");
	}

	private void saveStatistics() {
		for (final var measurement : this.measurements) {
			final ICache cache = measurement.getCache();
			final long cacheHit = measurement.getMetrics().getCacheHits();
			final long savedTraffic = measurement.getMetrics().getSavedBandthwidth();
			final double hitRatio = ((int) (cacheHit * 100000 / this.fileAccessed)) / 1000.0;
			final double savedTrafRatio = (int) (savedTraffic * 100000 / this.totalNetworkBandwidth) / 1000.0;
			this.cachesResults.add(new Statistics(cache, cacheHit, savedTraffic, hitRatio, savedTrafRatio));
		}
	}

	/**
	 * metoda pro ziskani vysledku mereni
	 * 
	 * @return hashtable s vysledky
	 */
	public List<Statistics> getCachesResults() {
		if (this.cachesResults.isEmpty()) {
			saveStatistics();
		}
		if (this.fileAccessed < GlobalVariables.getLimitForStatistics()) {
			return Collections.emptyList();
		}
		return this.cachesResults;
	}
}
