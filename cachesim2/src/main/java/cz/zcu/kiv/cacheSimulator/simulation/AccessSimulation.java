package cz.zcu.kiv.cacheSimulator.simulation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JOptionPane;

import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequstedFile;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

/**
 * trida pro simulaci pristupu k souborum a cachovacich algoritmu
 * 
 * @author Pavel Bzoch
 * 
 */
public class AccessSimulation extends Thread{


	private boolean doSimulation = true;
	
	/**
	 * promenna pro uchovani seznamu pristupovanych souboru
	 */
	private IFileQueue fileQueue;
	
	
	/**
	 * promenne pro urceni casu, po ktery probihala simulace
	 */
	private long timeAccessPeriod = 0;

	/**
	 * promenna pro uchovani uzivatelu a jejich cachovacich algoritmu
	 */
	private Hashtable<Long, SimulatedUser> userTable;
	
	/**
	 * promenna pro praci s konzistentnosti
	 */
	private IConsistencySimulation consistency;
	
	/**
	 * promenna pro uchovani odkazu na server
	 */
	protected Server server = Server.getInstance();
	
	private final MainGUI gui;
	
	private FileFactorySync sync;
	
	/**
	 * promenna pro urceni, kdy byl pristupovan prvni soubor
	 */
	@SuppressWarnings("unused")
	private long firstFileAccessTime = 0;

	public int threads = 4;
	
	/**
	 * konstruktor - inicializace promennych
	 * 
	 * @param fileQueue
	 *            seznam prisupovanych souboru
	 * @param consistency
	 * 			  trida pro praci s konzistentnosti
	 */
	public AccessSimulation(IFileQueue fileQueue, IConsistencySimulation consistency, MainGUI gui) {
		this.fileQueue = fileQueue;
		this.userTable = new Hashtable<Long, SimulatedUser>();
		this.consistency = consistency;
		this.gui = gui;
		this.sync = new FileFactorySync(fileQueue, this.threads);
	}
	

	/**
	 * metoda pro ziskani ci vytvoreni noveho uzivatele
	 * 
	 * @param userID
	 *            id uziavatele
	 * @return uzivatel s cachovacimi algoritmy
	 */
	public synchronized SimulatedUser getUser(long userID) {
		SimulatedUser user = userTable.get(userID);
		if (user == null) {
			user = new SimulatedUser(userID);
			userTable.put(userID, user);
		}
		return user;
	}
	
	protected synchronized void discardThread()
	{
		this.threads--;
	}
	
	public void stopSimulation()
	{
		this.doSimulation = false;
		synchronized (sync) {
			sync.notifyAll(); /* wake up all sleeping threads */
		}
	}
	
	public boolean doSimulation()
	{
		return this.doSimulation;
	}

	/**
	 * metoda pro spusteni simulace - pristupuje k souborum velikosti
	 * pristupovanych souboru se generuji automaticky 
	 */
	@Override
	public void run() {
		gui.disableComponentsForSimulation();
		gui.simulationProgressBar.setMaximum(100);
		
		//metoda, ktera nahraje soubory na server
		if (GlobalVariables.isLoadServerStatistic()){
			loadFilesToServer();
		}
	
		Thread[] threads = new Thread[this.threads];
		
		
		for (int tid = 0; tid < this.threads; tid++ )
		{
			threads[tid] = new Thread( new AccessSimulationThread(this, sync, consistency, tid) );
			threads[tid].start();
		}
		
		for (int tid = 0; tid < this.threads; tid++)
		{
			try {
				threads[tid].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		

		gui.simulationProgressBar.setVisible(false);
		gui.cacheResults = getResults();
		
		
		if (gui.cacheResults != null && gui.cacheResults.size() > 0) {
			gui.loadResultsToPanel();
			gui.loadConResultsToPanel(consistency);
			gui.enableComponentsAfterSimulaton(true);
		} else {
			JOptionPane.showMessageDialog(MainGUI.getInstance(),
					"There are no results!", "Error",
					JOptionPane.ERROR_MESSAGE);
			gui.enableComponentsAfterSimulaton(false);
		}
	
	}
	
	/**
	 * metoda, ktera nahraje soubory na server vcetne poctu pristupu
	 */
	private void loadFilesToServer() {
		fileQueue.setInfo("Pre-loading data to server... ");
		// pruchod strukturou + pristupovani souboru
		RequstedFile file = null;
		FileOnServer fOnServer = null;
		//cas pristupu k prvnimu a poslednimu souboru
		long timeOfFirstFile = 0, timeOfLastFile = 0;
		do {
			// pristupujeme dalsi soubor
			if (fileQueue instanceof LogReaderAFS){
				file = ((LogReaderAFS)fileQueue).getNextServerFile(false);
			}
			else
				file = fileQueue.getNextServerFile();
			if (file == null)
				break;
			if (timeOfFirstFile == 0)
				timeOfFirstFile = file.getAccessTime();
			timeOfLastFile = file.getAccessTime();

			fOnServer = server.existFileOnServer(file.getFname());
			if (fOnServer == null){
				if (file.getfSize() < 0){
					fOnServer = server.generateRandomFileSize(file.getFname(),
						GlobalVariables.getMinGeneratedFileSize(),
						GlobalVariables.getMaxGeneratedFileSize());
				}
				else{
					fOnServer = server.insertNewFile(file.getFname(), file.getfSize());
				}
			}
			
			//soubor byl zapsan, je treba aktualizovat velikost a verzi souboru 
			else if (!file.isRead()){
				fOnServer.updateFile(file.getfSize());
			}
			//soubor byl cten - zvetsime pocet cteni, abychom meli lepsi statistiky
			else if (file.isRead()){
				fOnServer.increaseReadHitPrefatching();
			}
			
			
		}while (file != null) ;
		this.firstFileAccessTime = timeOfFirstFile;
		this.timeAccessPeriod = timeOfLastFile - timeOfFirstFile;
		RequstedFile.setAddTime(timeAccessPeriod);
		fileQueue.resetQueue();
		server.storeFileAccessTimes();
		
		if (fileQueue instanceof LogReaderAFS){
			((LogReaderAFS)fileQueue).setInfo("Simulation in progress... ");
		}
	}

	/**
	 * metoda vraci vysledky vsech uzivatelu
	 * 
	 * @return vysledky vsech uzivatelu
	 */
	public ArrayList<UserStatistics> getResults() {
		ArrayList<UserStatistics> ret = new ArrayList<UserStatistics>();
		for (Iterator<SimulatedUser> it = userTable.values().iterator(); it
				.hasNext();) {
			SimulatedUser user = it.next();
			if (user.getCachesResults() != null) {
				ret.add(new UserStatistics(user));
			}			
		}
		return ret;
	}
}
