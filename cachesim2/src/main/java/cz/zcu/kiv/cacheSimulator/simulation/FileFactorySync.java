/**
 * FileFactorySync.java
 */
package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequestedFile;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

/**
 * FileFactorySync.java
 * 5. 4. 2014
 *
 * @author Lukáš Kvídera
 */
public class FileFactorySync implements IFileQueue {

  private final IFileQueue fileQueue;

  private int threads;
  private volatile int waitingThreads = 0;
  private volatile boolean canWait = true;

  /**
   * promenna pro uchovani odkazu na server
   */
  private final Server server = Server.getInstance();

  private volatile RequestedFile requestedFile;


  public FileFactorySync(final IFileQueue fileQueue, final int threads) {
    this.threads = threads;
    this.fileQueue = fileQueue;
  }


  public synchronized void discardThread() {
    this.threads--;
    if (this.threads <= this.waitingThreads)
      this.notifyAll();
  }


  private RequestedFile generateNewFile() {
    FileOnServer fOnServer = null;
    this.requestedFile = this.fileQueue.getNextServerFile();

    if ( this.requestedFile != null ) {
      fOnServer = this.server.existFileOnServer(this.requestedFile.getFname());
      if (fOnServer == null) {
        if (this.requestedFile.getfSize() < 0) {
          fOnServer = this.server.generateRandomFileSize(this.requestedFile.getFname(),
              GlobalVariables.getMinGeneratedFileSize(),
              GlobalVariables.getMaxGeneratedFileSize());
        }
        else{
          fOnServer = this.server.insertNewFile(this.requestedFile.getFname(), this.requestedFile.getfSize());
        }
      }

      //soubor byl zapsan, je treba aktualizovat velikost a verzi souboru
      else if (!this.requestedFile.isRead()){
        fOnServer.updateFile(this.requestedFile.getfSize());
      }
      //nastaveni, ze byl soubor cten od posledniho zapisu
      else{
        fOnServer.setWasReadSinceLastWrite();
      }
    }

    return this.requestedFile;
  }

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#getNextServerFile()
   */
  @Override
  public synchronized final RequestedFile getNextServerFile() {

    try {
      /* wait for threads that are going to run this iteration */
      while (!this.canWait) {
          this.wait();
      }
      ++this.waitingThreads;


      if (this.waitingThreads == 1) {
        this.generateNewFile();
      }
      else if (this.waitingThreads == this.threads) {
        this.canWait = !this.canWait;
        this.notifyAll();
      }

      /* wait for all threads to run previous iteration */
      while (this.canWait) {
          this.wait();
      }
      --this.waitingThreads;

      if (this.waitingThreads == 0) {
        this.canWait = !this.canWait;
        this.notifyAll();
      }

    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    return this.requestedFile;
  }


  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#resetQueue()
   */
  @Override
  public void resetQueue() {
    this.fileQueue.resetQueue();
  }


  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#setInfo(java.lang.String)
   */
  @Override
  public void setInfo(final String info) {
    this.fileQueue.setInfo(info);
  }


  /*
   * (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#cleanUp()
   */
  @Override
  public void cleanUp() {
    this.fileQueue.cleanUp();
  }

}
