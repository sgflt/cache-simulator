package cz.zcu.kiv.cacheSimulator.simulation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_SS;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS;
import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.LogReaderAFS;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequestedFile;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

/**
 * trida pro simulaci pristupu k souborum a cachovacich algoritmu
 *
 * @author Pavel Bzoch
 *
 */
public class AccessSimulation extends Observable implements Runnable {

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
  private final Map<Long, SimulatedUser> userTable;

  /**
   * promenna pro praci s konzistentnosti
   */
  private final IConsistencySimulation consistency;

  /**
   * promenna pro uchovani odkazu na server
   */
  protected Server server = Server.getInstance();

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
  public AccessSimulation(final IFileQueue fileQueue, final IConsistencySimulation consistency) {
    this.fileQueue = fileQueue;
    this.userTable = new HashMap<>();
    this.consistency = consistency;

    //metoda, ktera nahraje soubory na server
    if (GlobalVariables.isLoadServerStatistic()){
      this.loadFilesToServer();
    }
  }


  /**
   * metoda pro ziskani ci vytvoreni noveho uzivatele
   *
   * @param userID
   *            id uziavatele
   * @return uzivatel s cachovacimi algoritmy
   */
  SimulatedUser getUser(final long userID) {
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
  }

  public boolean doSimulation()
  {
    return this.doSimulation;
  }


  private FileOnServer updateFileInfo(final RequestedFile file, FileOnServer fOnServer) {
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
    //nastaveni, ze byl soubor cten od posledniho zapisu
    else{
      fOnServer.setWasReadSinceLastWrite();
    }

    return fOnServer;
  }

  private void cacheHit( final Triplet<ICache[], Long[], Long[]> cache, final RequestedFile file,
      final FileOnClient fOnClient, final FileOnServer fOnServer, final int i){
    //soubor je cten, zvysime read hit ratio
    if (file.isRead()){
      ++cache.getSecond()[i];
      cache.getThird()[i] += fOnClient.getFileSize();

      //kontrola konzistentnosti
      if (this.consistency != null){
        this.consistency.updateActualReadFile(cache.getFirst()[i], file.getUserID(), fOnClient, fOnServer);
      }
      else{
        //pokud neni kontrola konzistentnosti, tak alespon upravime velikost souboru
        fOnClient.updateSize(fOnServer);
      }

      // statistiky na server u vsech souboru - i u tech, co
      // se pristupuji z cache
      if ((GlobalVariables.isSendStatisticsToServerLFUSS() && (cache
          .getFirst()[i] instanceof LFU_SS))
          || (GlobalVariables
              .isSendStatisticsToServerLRFUSS() && (cache
              .getFirst()[i] instanceof LRFU_SS)))
        this.server.getFileRead(file.getFname(),
            cache.getFirst()[i]);
    }
    //soubor byl zapisovan, je treba resit konzistenci
    else{
//              if (consistency != null){
//                consistency.updateConsistencyWrite(cache.getFirst()[i], file.getUserID(),fOnClient, fOnServer);
//              }
//              //pokud neni kontrola konzistentnosti, tak alespon upravime velikost souboru
//              else{
//                fOnClient.updateSize(fOnServer);
//              }
      fOnServer.increaseWriteHit(cache.getFirst()[i]);

    }
  }

  private void cacheMiss(final ICache cache, final RequestedFile file) {
    // ukladame jen soubory, ktere jsou ctene a zvysujeme statistiky
    if (file.isRead()){
      synchronized (cache) {
        cache.insertFile(new FileOnClient(
            this.server.getFileRead(
                file.getFname(),
                cache
                ),
            cache, file.getAccessTime()
            )
        );
      }
    }
  }

  private void processFile(final Triplet<ICache[], Long[], Long[]> userCacheList,
      final RequestedFile file, final FileOnServer fOnServer, final int i) {
    final FileOnClient fOnClient;
    final ICache cache = userCacheList.getFirst()[i];
    synchronized(cache) {
        fOnClient = cache.getFile(file.getFname());
    }
    if (fOnClient != null) {
      this.cacheHit(userCacheList, file, fOnClient, fOnServer, i);
    }
    // soubor neni v cache, musi se pro nej vytvorit zaznam
    else {
      this.cacheMiss(cache, file);
    }
  }

  /**
   * metoda pro spusteni simulace - pristupuje k souborum velikosti
   * pristupovanych souboru se generuji automaticky
   */
  @Override
  public void run() {

    final Instant start = Instant.now();
    // pruchod strukturou + pristupovani souboru
    final ExecutorService executor = Executors.newWorkStealingPool(1);

    do {
      // pristupujeme dalsi soubor
      final RequestedFile filex = this.fileQueue.getNextServerFile();
      if (filex == null)
        break;

      final SimulatedUser user = this.getUser(filex.getUserID());
      // pokud na serveru soubor neexistuje, vytvorime jej s nahodnou
      // velikosti souboru
      final FileOnServer fOnServerx = this.updateFileInfo(filex, this.server.getFile(filex.getFname()));

      // zvysime pocet pristupovanych souboru
      if (filex.isRead()){
        user.incereaseFileAccess();
        user.increaseTotalNetworkBandwidth(fOnServerx.getFileSize());
      }

      for (final Triplet<ICache[], Long[], Long[]> userCacheList : user.getCaches()) {
        for (int ix = 0; ix < userCacheList.getFirst().length; ix++) {
          // soubor je jiz v cache, aktualizujeme pouze statistiky
          final RequestedFile file = filex;
          final int i = ix;
          final FileOnServer fOnServer = fOnServerx;
          executor.execute(() -> { this.processFile(userCacheList, file, fOnServer, i); });
        }
      }
    } while (this.doSimulation);

    this.await(executor);
    this.fileQueue.cleanUp();

    LOG.info("Simulation completed in {} seconds", Duration.between(start, Instant.now()).getSeconds());
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


    }while (file != null) ;
    this.firstFileAccessTime = timeOfFirstFile;
    this.timeAccessPeriod = timeOfLastFile - timeOfFirstFile;
    RequestedFile.setAddTime(this.timeAccessPeriod);
    this.fileQueue.resetQueue();
    this.server.storeFileAccessTimes();

    if (this.fileQueue instanceof LogReaderAFS){
      ((LogReaderAFS)this.fileQueue).setInfo("Simulation in progress... ");
    }
  }

  private final void await(final ExecutorService executor) {
    try {
      executor.shutdown();
      final int progressStep = (((ForkJoinPool)executor).getQueuedSubmissionCount()) / 100;
      while(!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
        LOG.info("Waiting to complete all tasks {}", executor);
        this.setChanged();
        this.notifyObservers(
                (100 - (((ForkJoinPool)executor).getQueuedSubmissionCount()) / progressStep)
            );

        if (!this.doSimulation) {
          executor.shutdownNow();
        }
      }
      LOG.info("Waiting COMPLETED ", executor);
    } catch (final InterruptedException e) {
      LOG.error("Interrupted", e);
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
