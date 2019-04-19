package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_SS;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.FileOnClient;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Quartet;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * trida pro simulaci pristupu k souborum a cachovacich algoritmu
 * 
 * @author Pavel Bzoch
 * 
 */
public class AccessSimulation {

	/**
	 * promenna pro uchovani seznamu pristupovanych souboru
	 */
	private final IFileQueue fileQueue;

	/**
	 * promenna pro uchovani odkazu na server
	 */
	private final Server server = Server.getInstance();

	/**
	 * promenna pro uchovani uzivatelu a jejich cachovacich algoritmu
	 */
	private final Hashtable<Long, SimulatedUser> userTable;

	/**
	 * konstruktor - inicializace promennych
	 * 
	 * @param fileQueue
	 *            seznam prisupovanych souboru
	 */
	public AccessSimulation(final IFileQueue fileQueue) {
		this.fileQueue = fileQueue;
		this.userTable = new Hashtable<>();
	}

	/**
	 * metoda pro ziskani ci vytvoreni noveho uzivatele
	 * 
	 * @param userID
	 *            id uziavatele
	 * @return uzivatel s cachovacimi algoritmy
	 */
	private SimulatedUser getUser(final long userID) {
		return this.userTable.computeIfAbsent(userID, SimulatedUser::new);
	}

	/**
	 * metoda pro spusteni simulace - pristupuje k souborum velikosti
	 * pristupovanych souboru se generuji automaticky
	 */
	public void simulateRandomFileSizes() {
		// pruchod strukturou + pristupovani souboru
		Triplet<String, Long, Long> file = this.fileQueue.getNextFileName();
		SimulatedUser user;
		while (file != null) {
			user = getUser(file.getThird());
			// pokud na serveru soubor neexistuje, vytvorime jej s nahodnou
			// velikosti souboru
			if (!this.server.existFileOnServer(file.getFirst())) {
				this.server.generateRandomFileSize(file.getFirst(), GlobalVariables
					.getMinGeneratedFileSize(), GlobalVariables
					.getMaxGeneratedFileSize());
			}
			// zvysime pocet pristupovanych souboru
			user.incereaseFileAccess();
			user.increaseTotalNetworkBandwidth(this.server.getFileSize(file.getFirst()));
			for (final Triplet<ICache[], Long[], Long[]> cache : user.getCaches()) {
				for (int i = 0; i < cache.getFirst().length; i++) {
					// soubor je jiz v cache, aktualizujeme pouze statistiky
					if (cache.getFirst()[i].isInCache(file.getFirst())) {
						cache.getSecond()[i] += 1;
						cache.getThird()[i] += cache.getFirst()[i]
								.getFileFromCache(file.getFirst())
								.getFileSize();

						// statistiky na server u vsech souboru - i u tech, co
						// se pristupuji z cache
						if ((GlobalVariables.isSendStatisticsToServerLFUSS() && (cache
								.getFirst()[i] instanceof LFU_SS))
								|| (GlobalVariables
										.isSendStatisticsToServerLRFUSS() && (cache
							.getFirst()[i] instanceof LRFU_SS))) {
							this.server.getFileRead(file.getFirst(), cache
								.getFirst()[i]);
						}
					}
					// soubor neni v cache, musi se pro nej vytvorit zaznam
					else {
						cache.getFirst()[i].insertFile(new FileOnClient(this.server
								.getFileRead(file.getFirst(),
										cache.getFirst()[i]),
								cache.getFirst()[i], file.getSecond()));
					}
				}
			}
			// pristupujeme dalsi soubor
			file = this.fileQueue.getNextFileName();
		}
	}

	/**
	 * metoda pro spusteni simulace - pristupuje k souborum velikosti souboru
	 * jsou nacitany z logovaciho souboru
	 */
	public void simulateFromLogFile() {
		// pruchod strukturou + pristupovani souboru
		Quartet<String, Long, Long, Long> file = this.fileQueue
				.getNextFileNameWithFSize();
		SimulatedUser user;
		while (file != null) {
			user = getUser(file.getFourth());
			// pokud na serveru soubor neexistuje, vytvorime jej s nactenou
			// velikosti souboru
			if (!this.server.existFileOnServer(file.getFirst())) {
				this.server.insertNewFile(file.getFirst(), file.getSecond());
			}
			// zvysime pocet pristupovanych souboru
			user.incereaseFileAccess();
			user.increaseTotalNetworkBandwidth(this.server.getFileSize(file
					.getFirst()));
			for (final Triplet<ICache[], Long[], Long[]> cache : user.getCaches()) {
				for (int i = 0; i < cache.getFirst().length; i++) {
					// soubor je jiz v cache, aktualizujeme pouze statistiky
					if (cache.getFirst()[i].isInCache(file.getFirst())) {
						cache.getSecond()[i] += 1;
						cache.getThird()[i] += cache.getFirst()[i].getFileFromCache(
										file.getFirst()).getFileSize();
						// statistiky na server u vsech souboru - i u tech, co
						// se pristupuji z cache
						if ((GlobalVariables.isSendStatisticsToServerLFUSS() && (cache
								.getFirst()[i] instanceof LFU_SS))
								|| (GlobalVariables
										.isSendStatisticsToServerLRFUSS() && (cache
							.getFirst()[i] instanceof LRFU_SS))) {
							this.server.getFileRead(file.getFirst(), cache
								.getFirst()[i]);
						}
					}
					// soubor neni v cache, musi se pro nej vytvorit zaznam
					else {
						cache.getFirst()[i].insertFile(
							new FileOnClient(this.server.getFileRead(file
										.getFirst(), cache.getFirst()[i]), cache
										.getFirst()[i], file.getThird()));
					}
				}
				// pristupujeme dalsi soubor
				file = this.fileQueue.getNextFileNameWithFSize();
			}
		}
	}

	/**
	 * metoda vraci vysledky vsech uzivatelu
	 * 
	 * @return vysledky vsech uzivatelu
	 */
	public List<UserStatistics> getResults() {
		final ArrayList<UserStatistics> ret = new ArrayList<>();
		for (final var user : this.userTable.values()) {
			if (user.getCachesResults() != null) {
				ret.add(new UserStatistics(user));
			}
		}
		return ret;
	}
}
