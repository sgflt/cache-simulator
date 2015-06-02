/**
 * AccessSimulationThread.java
 */
package cz.zcu.kiv.cacheSimulator.simulation;

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
import cz.zcu.kiv.cacheSimulator.dataAccess.RequestedFile;
import cz.zcu.kiv.cacheSimulator.gui.MainGUI;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

/**
 * AccessSimulationThread.java
 *    12. 4. 2014
 *
 * @author Lukáš Kvídera
 *
 */
public class AccessSimulationThread  extends Observable implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(AccessSimulationThread.class);

  private final AccessSimulation simulation;
  private final FileFactorySync fileFactory;

  /**
   * promenna pro praci s konzistentnosti
   */
  private final IConsistencySimulation consistency;

  /**
   * promenna pro uchovani odkazu na server
   */
  private final Server server = Server.getInstance();

  public AccessSimulationThread(final AccessSimulation simulation, final FileFactorySync fileFactory,
      final IConsistencySimulation consistency, final int threadId)
  {
    this.simulation = simulation;
    this.fileFactory = fileFactory;
    this.consistency = consistency;
    this.addObserver(MainGUI.getInstance());
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    SimulatedUser user;
    RequestedFile filex;

    final ExecutorService executor = Executors.newWorkStealingPool();
    do {
      filex = this.fileFactory.getNextServerFile();
      if (filex == null)
        break;

      user = this.simulation.getUser(filex.getUserID());

      final FileOnServer fOnServer = this.server.getFile(filex.getFname());


      if (filex.isRead()) {
        user.incereaseFileAccess();
        user.increaseTotalNetworkBandwidth(fOnServer.getFileSize());
      }

      //iterování přes algoritmy
     for (final Triplet<ICache[], Long[], Long[]> cache : user.getCaches()) {
        for (int ix = 0; ix < cache.getFirst().length; ix++) {
          final int i = ix;
          final RequestedFile file = filex;
          executor.execute(() -> {
            //LOG.trace("run(ix={}, file={}, cache={})", i, file, cache.getFirst()[i] + " " + cache.getFirst()[i].getCacheCapacity());
            final FileOnClient fOnClient;

            synchronized (cache.getFirst()[i]) {
              fOnClient = cache.getFirst()[i].getFile(file.getFname());
            }

            if (fOnClient != null) {
              if (file.isRead()) {
                /* File is read so increase read hits */
                cache.getSecond()[i]++;
                cache.getThird()[i] += fOnClient.getFileSize();

                //kontrola konzistentnosti
                if (this.consistency != null) {
                  this.consistency.updateActualReadFile(cache.getFirst()[i], file.getUserID(),fOnClient, fOnServer);
                }
                else {
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
              else {
                //soubor byl zapisovan, je treba resit konzistenci
    //            if (consistency != null) {
    //              consistency.updateConsistencyWrite(cache.getFirst()[i], file.getUserID(),fOnClient, fOnServer);
    //            }
    //            //pokud neni kontrola konzistentnosti, tak alespon upravime velikost souboru
    //            else {
    //              fOnClient.updateSize(fOnServer);
    //            }
                fOnServer.increaseWriteHit(cache.getFirst()[i]);

              }
            }
            // soubor neni v cache, musi se pro nej vytvorit zaznam
            else {
              // ukladame jen soubory, ktere jsou ctene a zvysujeme statistiky
              if (file.isRead()){
                synchronized (cache.getFirst()[i]) {
                  cache.getFirst()[i].insertFile(new FileOnClient(this.server.getFileRead(file.getFname(),
                      cache.getFirst()[i]), cache.getFirst()[i], file.getAccessTime()));
                }
              }
            }
          });
        }
      }

    } while (this.simulation.doSimulation());

    this.await(executor);
    this.fileFactory.cleanUp();
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

        if (!this.simulation.doSimulation()) {
          executor.shutdownNow();
        }
      }
      LOG.info("Waiting COMPLETED ", executor);
    } catch (final InterruptedException e) {
      LOG.error("Interrupted", e);
    }
  }
}

