package cz.zcu.kiv.cacheSimulator.consistency;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

public class MMWPBatchConsistency implements IConsistencySimulation {

	/**
	 * jak casto ce mame ptat na casto zapisovane soubory
	 */
	private static final int FrequentTimePeriod = MMWPConsistency.getTtl5() * 1000;

	/**
	 * jak casto se budeme ptat na soubory s druhy nejvice zapisy
	 */
	private static final int MediumTimePeriodFirst = MMWPConsistency.getTtl4() * 1000;

	/**
	 * jak casto se budeme ptat na soubory s treti nejvice zapisy
	 */
	private static final int MediumTimePeriodSecond = MMWPConsistency.getTtl3() * 1000;

	/**
	 * jak casto se budeme ptat na soubory s ctvrty nejvice zapisy
	 */
	private static final int MediumTimePeriodThird = MMWPConsistency.getTtl2() * 1000;

	/**
	 * jak casto se mame ptat na soubory, ktere byly zapisovany nejmene
	 */
	private static final int LeastFreqTimePeriod = MMWPConsistency.getTtl1() * 1000;

	/**
	 * vymezeni verzi pro nejcastejci ptani
	 */
	private static final int groupG5hits = MMWPConsistency.getHits5();

	/**
	 * vymezeni verzi pro druhe nejcastejci ptani
	 */
	private static final int groupG4hits = MMWPConsistency.getHits4();

	/**
	 * vymezeni verzi pro treti nejcastejci ptani
	 */
	private static final int groupG3hits = MMWPConsistency.getHits3();

	/**
	 * vymezeni verzi pro ctvrte nejcastejci ptani
	 */
	private static final int groupG2hits = MMWPConsistency.getHits2();

	/**
	 * promenna pro uchovani data o nekonzistentnim stavu
	 */
	private ArrayList<MMWPBatchConsistencyData> inconsistencyHist;

	/**
	 * konstruktor - iniciace promennych
	 */
	public MMWPBatchConsistency() {
		inconsistencyHist = new ArrayList<MMWPBatchConsistencyData>();
	}

	/**
	 * metoda pro ziskani datove instance podle cache a userID
	 * 
	 * @param cache
	 *            cachovaci politika
	 * @param userID
	 *            ID uzivatele
	 * @return instance tridy BackgroundConsistencyBatchData
	 */
	protected MMWPBatchConsistencyData getByCacheAndID(ICache cache, long userID) {
		for (MMWPBatchConsistencyData data : inconsistencyHist) {
			if (data.compareTo(userID, cache))
				return data;
		}
		MMWPBatchConsistencyData newData = new MMWPBatchConsistencyData(userID,
				cache);
		inconsistencyHist.add(newData);
		return newData;
	}

	@Override
	public void updateConsistencyWrite(ICache cache, long userID,
			FileOnClient fOnClient, FileOnServer fOnServer) {
		fOnClient.updateVerAndSize(fOnServer);
	}

	@Override
	public void updateActualReadFile(ICache cache, long userID,
			FileOnClient fOnClient, FileOnServer fOnServer) {

		// ziskani vsech cachovanych souboru
		List<FileOnClient> filesOnClient = cache.getCachedFiles();

		MMWPBatchConsistencyData data = getByCacheAndID(cache, userID);

		// kontrola frequent period time
		checkFrequentPeriodTime(filesOnClient, data, cache);

		// kontrola medium period time
		checkMediumFirstPeriodTime(filesOnClient, data, cache);

		// kontrola medium period time
		checkMediumSecondPeriodTime(filesOnClient, data, cache);

		// kontrola medium period time
		checkMediumThirdPeriodTime(filesOnClient, data, cache);

		// kontrola least period time
		checkLeastPeriodTime(filesOnClient, data, cache);

		// kontrola prave pristupovaneho souboru
		if (fOnClient.getVersion() != fOnServer.getVersion()) {
			// System.out.println("Nekonzistence: " + fOnClient.getFileName()
			// + ", verze: " + fOnClient.getVersion());
			data.updateInconsistencies(fOnClient);
		}

	}

	private void checkMediumThirdPeriodTime(List<FileOnClient> filesOnClient,
			MMWPBatchConsistencyData data, ICache cache) {
		updateTIme(data);
		boolean once = true;
		boolean updates = false;
		// kontrola probiha pouze pokud jsme v danem casovem intervalu
		while (data.getLastAccessTimeMediumThird() + MediumTimePeriodThird < GlobalVariables
				.getActualTime()) {
			data.setLastAccessTimeMediumThird(data
					.getLastAccessTimeMediumThird() + MediumTimePeriodThird);
			data.updateMediumThirdAsk();
			// kontrola souboru na verzi
			if (once)
				for (FileOnClient f : filesOnClient) {
					// kontrola, jestli se mame ptat na verzi
					if (f.getVersion() < groupG3hits
							&& f.getVersion() >= groupG2hits) {
						FileOnServer fOnSerAct = Server.getInstance().getFile(
								f.getFileName(), cache);
						if (fOnSerAct.getVersion() != f.getVersion()) {
							data.updateUpdates();
							f.updateVerAndSize(fOnSerAct);
							data.updateTraffic(fOnSerAct.getFileSize());
							updates = true;
						}
					}
				}
			if (updates)
				data.updateLeastFreqAsks();
			once = false;
		}

	}

	/**
	 * metoda pro kontrolu konzistenci stredne casto zapisovanych souboru pri
	 * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
	 * jednotlive
	 * 
	 * @param filesOnClient
	 *            soubory, ktere jsou v cache
	 * @param data
	 *            data o konzistentnosti
	 * @param cache
	 *            reference na cachovaci algoritmus, odkud pochazi soubor
	 */

	private void checkMediumSecondPeriodTime(List<FileOnClient> filesOnClient,
			MMWPBatchConsistencyData data, ICache cache) {
		updateTIme(data);
		boolean once = true;
		boolean updates = false;
		// kontrola probiha pouze pokud jsme v danem casovem intervalu
		while (data.getLastAccessTimeMediumSecond() + MediumTimePeriodSecond < GlobalVariables
				.getActualTime()) {
			data.setLastAccessTimeMediumSecond(data
					.getLastAccessTimeMediumSecond() + MediumTimePeriodSecond);
			data.updateMediumSecAsks();

			// kontrola souboru na verzi
			if (once)
				for (FileOnClient f : filesOnClient) {
					// kontrola, jestli se mame ptat na verzi
					if (f.getVersion() < groupG4hits
							&& f.getVersion() >= groupG3hits) {
						FileOnServer fOnSerAct = Server.getInstance().getFile(
								f.getFileName(), cache);
						if (fOnSerAct.getVersion() != f.getVersion()) {
							data.updateUpdates();
							f.updateVerAndSize(fOnSerAct);
							data.updateTraffic(fOnSerAct.getFileSize());
							updates = true;
						}
					}
				}
			if (updates)
				data.updateLeastFreqAsks();
			once = false;
		}
	}

	/**
	 * metoda pro kontrolu konzistenci stredne casto zapisovanych souboru pri
	 * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
	 * jednotlive
	 * 
	 * @param filesOnClient
	 *            soubory, ktere jsou v cache
	 * @param data
	 *            data o konzistentnosti
	 * @param cache
	 *            reference na cachovaci algoritmus, odkud pochazi soubor
	 */

	private void checkMediumFirstPeriodTime(List<FileOnClient> filesOnClient,
			MMWPBatchConsistencyData data, ICache cache) {
		updateTIme(data);
		boolean once = true;
		boolean updates = false;
		// kontrola probiha pouze pokud jsme v danem casovem intervalu
		while (data.getLastAccessTimeMediumFirst() + MediumTimePeriodFirst < GlobalVariables
				.getActualTime()) {
			data.setLastAccessTimeMediumFirst(data
					.getLastAccessTimeMediumFirst() + MediumTimePeriodFirst);
			data.updateMediumFirstAsk();

			// kontrola souboru na verzi
			if (once)
				for (FileOnClient f : filesOnClient) {
					// kontrola, jestli se mame ptat na verzi
					if (f.getVersion() < groupG5hits
							&& f.getVersion() >= groupG4hits) {
						FileOnServer fOnSerAct = Server.getInstance().getFile(
								f.getFileName(), cache);
						if (fOnSerAct.getVersion() != f.getVersion()) {
							data.updateUpdates();
							f.updateVerAndSize(fOnSerAct);
							data.updateTraffic(fOnSerAct.getFileSize());
							updates = true;
						}
					}
				}
			if (updates)
				data.updateLeastFreqAsks();
			once = false;
		}

	}

	/**
	 * metoda pro kontrolu konzistenci nejmene casto zapisovanych souboru pri
	 * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
	 * jednotlive
	 * 
	 * @param filesOnClient
	 *            soubory, ktere jsou v cache
	 * @param data
	 *            data o konzistentnosti
	 * @param cache
	 *            reference na cachovaci algoritmus, odkud pochazi soubor
	 */
	private void checkLeastPeriodTime(List<FileOnClient> filesOnClient,
			MMWPBatchConsistencyData data, ICache cache) {
		updateTIme(data);
		boolean once = true;
		boolean updates = false;
		// kontrola probiha pouze pokud jsme v danem casovem intervalu
		while (data.getLastAccessTimeLeast() + LeastFreqTimePeriod < GlobalVariables
				.getActualTime()) {
			data.setLastAccessTimeLeast(data.getLastAccessTimeLeast()
					+ LeastFreqTimePeriod);

			// kontrola souboru na verzi
			if (once)
				for (FileOnClient f : filesOnClient) {
					// kontrola, jestli se mame ptat na verzi
					if (f.getVersion() < groupG2hits) {
						FileOnServer fOnSerAct = Server.getInstance().getFile(
								f.getFileName(), cache);
						if (fOnSerAct.getVersion() != f.getVersion()) {
							data.updateUpdates();
							f.updateVerAndSize(fOnSerAct);
							data.updateTraffic(fOnSerAct.getFileSize());
							updates = true;
						}
					}
				}
			if (updates)
				data.updateLeastFreqAsks();
			once = false;
		}
	}

	/**
	 * metoda pro kontrolu konzistenci nejcasteji zapisovanych souboru pri
	 * realnem pouziti se predpoklada davkove ptani, pro simulaci se budeme ptat
	 * jednotlive
	 * 
	 * @param filesOnClient
	 *            soubory, ktere jsou v cache
	 * @param data
	 *            data o konzistentnosti
	 * @param cache
	 *            reference na cachovaci algoritmus, odkud pochazi soubor
	 */
	private void checkFrequentPeriodTime(List<FileOnClient> filesOnClient,
			MMWPBatchConsistencyData data, ICache cache) {
		updateTIme(data);
		boolean once = true;
		boolean updates = false;
		// kontrola probiha pouze pokud jsme v danem casovem intervalu
		while (data.getLastAccessTimeFrequent() + FrequentTimePeriod < GlobalVariables
				.getActualTime()) {
			data.setLastAccessTimeFrequent(data.getLastAccessTimeFrequent()
					+ FrequentTimePeriod);
			data.updateFreqAsks();

			// kontrola souboru na verzi
			if (once)
				for (FileOnClient f : filesOnClient) {
					// kontrola, jestli se mame ptat na verzi
					if (f.getVersion() >= groupG5hits) {
						FileOnServer fOnSerAct = Server.getInstance().getFile(
								f.getFileName(), cache);
						if (fOnSerAct.getVersion() != f.getVersion()) {
							data.updateUpdates();
							f.updateVerAndSize(fOnSerAct);
							data.updateTraffic(fOnSerAct.getFileSize());
							updates = true;
						}
					}
				}
			if (updates)
				data.updateLeastFreqAsks();
			once = false;
		}
	}

	/**
	 * metoda pro update casu
	 * 
	 * @param data
	 *            data, u kterych aktualizujeme cas
	 */
	private void updateTIme(MMWPBatchConsistencyData data) {
		if (data.getLastAccessTimeFrequent() == -1) {
			data.setLastAccessTimeFrequent(GlobalVariables.getActualTime());
		}
		if (data.getLastAccessTimeLeast() == -1) {
			data.setLastAccessTimeLeast(GlobalVariables.getActualTime());
		}
		if (data.getLastAccessTimeMediumSecond() == -1) {
			data.setLastAccessTimeMediumSecond(GlobalVariables.getActualTime());
		}
		if (data.getLastAccessTimeMediumFirst() == -1) {
			data.setLastAccessTimeMediumFirst(GlobalVariables.getActualTime());
		}
		if (data.getLastAccessTimeMediumThird() == -1) {
			data.setLastAccessTimeMediumThird(GlobalVariables.getActualTime());
		}

	}

	@Override
	public void printStat() {
		System.out
				.println("Statistiky pro simulaci pristupove konzistentnosti");
		System.out.println("T1=" + FrequentTimePeriod / 1000 + "s; T2="
				+ MediumTimePeriodFirst / 1000 + "s; T3="
				+ MediumTimePeriodSecond / 1000 + "s; T4="
				+ MediumTimePeriodThird / 1000 + "s; T5=" + LeastFreqTimePeriod
				/ 1000 + "s");
		for (MMWPBatchConsistencyData data : inconsistencyHist) {
			System.out.println(data);
		}

	}

	@Override
	public String getInfo() {
		return "MMWPBatchConsistency;MMWP batch consistency control";
	}

	@Override
	public String[] getHeaders() {
		String[] ret = { "Cache capacity[MB]", "No of requests (G1)",
				"No of inconsistencies (G1)", "No of requests (G2)",
				"No of inconsistencies (G2)", "No of requests (G3)",
				"No of inconsistencies (G3)", "No of requests (G4)",
				"No of inconsistencies (G4)", "No of requests (G5)",
				"No of inconsistencies (G5)", "No of updates",
				"Size of transferred files[MB]" };
		return ret;

	}

	@Override
	public Object[][] getData(String cacheName, long userID) {
		Object[][] ret = new Object[MainGUI.getInstance().getCacheSizes().length][getHeaders().length];
		int row = 0;
		boolean isRes = false;
		for (MMWPBatchConsistencyData data : inconsistencyHist) {
			if (!data.checkInconsistrencies()){
				JOptionPane.showMessageDialog(MainGUI.getInstance(),
						"There are inconsistencies in results!\n", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
			if (data.getUserID() == userID
					&& data.getCache().getClass().getName().contains(cacheName)) {
				ret[row][0] = data.getCache().getCacheCapacity() / 1024 / 1024;
				
				ret[row][1] = data.getNoOfLeastFreqAsk();
				ret[row][2] = data.getInconsLeastCount();
				
				ret[row][3] = data.getNoOfMediumThiAsk();
				ret[row][4] = data.getInconsMediumThiCount();
				
				ret[row][5] = data.getNoOfMediumSecAsk();
				ret[row][6] = data.getInconsMediumSecCount();
				
				ret[row][7] = data.getNoOfMediumFirAsk();
				ret[row][8] = data.getInconsMediumFirCount();
				
				ret[row][9] = data.getNoOfFreqAsk();
				ret[row][10] = data.getInconsFrequentCount();
				
				ret[row][11] = data.getNoOfUpdates();
				ret[row][12] = data.getNetTraffic() / 1024 / 1024.0;
				
				row++;
				isRes = true;
			}
		}
		if (!isRes)
			return null;
		return ret;
	}

}