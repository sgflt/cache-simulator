package cz.zcu.kiv.cacheSimulator.simulation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequestedFile;
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
public class AccessSimulation implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(AccessSimulation.class);

  private volatile boolean doSimulation = true;

  /**
   * promenna pro uchovani seznamu pristupovanych souboru
   */
  private final IFileQueue fileQueue;


  /**
   * promenne pro urceni casu, po ktery probihala simulace
   */
  private long timeAccessPeriod = 0;

  /**
   * promenna pro uchovani uzivatelu a jejich cachovacich algoritmu
   */
  private final Hashtable<Long, SimulatedUser> userTable;

  /**
   * promenna pro praci s konzistentnosti
   */
  private final IConsistencySimulation consistency;

  /**
   * promenna pro uchovani odkazu na server
   */
  protected Server server = Server.getInstance();

  private final MainGUI gui;

  private final FileFactorySync sync;

  /**
   * promenna pro urceni, kdy byl pristupovan prvni soubor
   */
  @SuppressWarnings("unused")
  private long firstFileAccessTime = 0;

  /**
   * konstruktor - inicializace promennych
   *
   * @param fileQueue
   *            seznam prisupovanych souboru
   * @param consistency
   *         trida pro praci s konzistentnosti
   */
  public AccessSimulation(final IFileQueue fileQueue, final IConsistencySimulation consistency, final MainGUI gui) {
    this.fileQueue = fileQueue;
    this.userTable = new Hashtable<>();
    this.consistency = consistency;
    this.gui = gui;
    this.sync = new FileFactorySync(fileQueue, 1);
  }


  /**
   * metoda pro ziskani ci vytvoreni noveho uzivatele
   *
   * @param userID
   *            id uziavatele
   * @return uzivatel s cachovacimi algoritmy
   */
  public synchronized SimulatedUser getUser(final long userID) {
    SimulatedUser user = this.userTable.get(userID);
    if (user == null) {
      user = new SimulatedUser(userID);
      this.userTable.put(userID, user);
    }
    return user;
  }

  public void stopSimulation()
  {
    this.doSimulation = false;
    synchronized (this.sync) {
      this.sync.notifyAll(); /* wake up all sleeping threads */
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
    this.gui.disableComponentsForSimulation();
    this.gui.simulationProgressBar.setMaximum(100);

    //metoda, ktera nahraje soubory na server
    if (GlobalVariables.isLoadServerStatistic()){
      this.loadFilesToServer();
    }

    final Thread threads = new Thread(new AccessSimulationThread(this, this.sync, this.consistency, 0));
    threads.start();

    final Instant start = Instant.now();
    try {
      threads.join();
      LOG.info("Simulation done in {} seconds",  Duration.between(start, Instant.now()).toMillis() / 6000);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    this.gui.simulationProgressBar.setVisible(false);
    this.gui.cacheResults = this.getResults();


    if (this.gui.cacheResults != null && this.gui.cacheResults.size() > 0) {
      this.gui.loadResultsToPanel();
      this.gui.loadConResultsToPanel(this.consistency);
      this.gui.enableComponentsAfterSimulaton(true);
    } else {
      JOptionPane.showMessageDialog(MainGUI.getInstance(),
          "There are no results!", "Error",
          JOptionPane.ERROR_MESSAGE);
      this.gui.enableComponentsAfterSimulaton(false);
    }

  }

  /**
   * metoda, ktera nahraje soubory na server vcetne poctu pristupu
   */
  private void loadFilesToServer() {
    this.fileQueue.setInfo("Pre-loading data to server... ");
    // pruchod strukturou + pristupovani souboru
    RequestedFile file = null;
    FileOnServer fOnServer = null;
    //cas pristupu k prvnimu a poslednimu souboru
    long timeOfFirstFile = 0, timeOfLastFile = 0;
    do {
      // pristupujeme dalsi soubor
      if (this.fileQueue instanceof LogReaderAFS){
        file = ((LogReaderAFS)this.fileQueue).getNextServerFile(false);
      }
      else
        file = this.fileQueue.getNextServerFile();
      if (file == null)
        break;
      if (timeOfFirstFile == 0)
        timeOfFirstFile = file.getAccessTime();
      timeOfLastFile = file.getAccessTime();

      fOnServer = this.server.getFile(file.getFname());
      if (fOnServer == null){
        if (file.getfSize() < 0){
          fOnServer = this.server.generateRandomFileSize(file.getFname(),
            GlobalVariables.getMinGeneratedFileSize(),
            GlobalVariables.getMaxGeneratedFileSize());
        }
        else{
          fOnServer = this.server.insertNewFile(file.getFname(), file.getfSize());
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


    } while (file != null);

    this.firstFileAccessTime = timeOfFirstFile;
    this.timeAccessPeriod = timeOfLastFile - timeOfFirstFile;
    RequestedFile.setAddTime(this.timeAccessPeriod);
    this.fileQueue.resetQueue();
    this.server.storeFileAccessTimes();

    if (this.fileQueue instanceof LogReaderAFS){
      ((LogReaderAFS)this.fileQueue).setInfo("Simulation in progress... ");
    }
  }

  /**
   * metoda vraci vysledky vsech uzivatelu
   *
   * @return vysledky vsech uzivatelu
   */
  public ArrayList<UserStatistics> getResults() {
    final ArrayList<UserStatistics> ret = new ArrayList<>();
    for (final Iterator<SimulatedUser> it = this.userTable.values().iterator(); it
        .hasNext();) {
      final SimulatedUser user = it.next();
      if (user.getCachesResults() != null) {
        ret.add(new UserStatistics(user));
      }
    }
    return ret;
  }
}
