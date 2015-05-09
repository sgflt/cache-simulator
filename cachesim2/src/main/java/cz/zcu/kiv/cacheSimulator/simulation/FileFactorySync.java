/**
 * FileFactorySync.java
 */
package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequstedFile;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;

/**
 * FileFactorySync.java
 *    5. 4. 2014
 *
 * @author Lukáš Kvídera, A11B0421P
 *
 */
public class FileFactorySync implements IFileQueue {

  private final IFileQueue fileQueue;

  private int threads;
  private final int[] waitingThreads = new int[2];
  private int filesGenerated = 0;


  /**
   * promenna pro uchovani odkazu na server
   */
  private final Server server = Server.getInstance();

  private final RequstedFile[] requestedFile = new RequstedFile[2];

  public FileFactorySync(final IFileQueue fileQueue, final int threads)
  {
    this.threads = threads;
    this.fileQueue = fileQueue;
  }

  public synchronized void discardThread()
  {
    this.threads--;
    if ( this.threads <= this.waitingThreads[this.filesGenerated % 2])
      this.notifyAll();
  }

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#getNextServerFile()
   */
  @Override
  public synchronized RequstedFile getNextServerFile() {
    FileOnServer fOnServer = null;
    final int quieueuId = this.filesGenerated % 2;

    /* first thread generates new file for speed up*/
    if ( this.waitingThreads[quieueuId] == 0 )
    {
      this.requestedFile[quieueuId] = this.fileQueue.getNextServerFile();

      if ( this.requestedFile[quieueuId] != null ) {
        fOnServer = this.server.existFileOnServer(this.requestedFile[quieueuId].getFname());
        if (fOnServer == null){
          if (this.requestedFile[quieueuId].getfSize() < 0){
            fOnServer = this.server.generateRandomFileSize(this.requestedFile[quieueuId].getFname(),
                GlobalVariables.getMinGeneratedFileSize(),
                GlobalVariables.getMaxGeneratedFileSize());
          }
          else{
            fOnServer = this.server.insertNewFile(this.requestedFile[quieueuId].getFname(), this.requestedFile[quieueuId].getfSize());
          }
        }

        //soubor byl zapsan, je treba aktualizovat velikost a verzi souboru
        else if (!this.requestedFile[quieueuId].isRead()){
          fOnServer.updateFile(this.requestedFile[quieueuId].getfSize());
        }
        //nastaveni, ze byl soubor cten od posledniho zapisu
        else{
          fOnServer.setWasReadSinceLastWrite();
        }
        //System.out.println("Factrory: " + filesGenerated + " | gen: " +requestedFile[quieueuId].getFname());
      }
      else
      {
        //System.out.println("Leaving factory");
        return null;
      }

    }

    this.waitingThreads[quieueuId]++;

    //System.out.println(waitingThreads[quieueuId] + " >= " + threads);
    if ( this.waitingThreads[quieueuId] >= this.threads )
    {
      this.filesGenerated++;
      this.waitingThreads[this.filesGenerated % 2] = 0;
      this.notifyAll();
      //System.out.println("notifyAll "+Arrays.toString(waitingThreads) + "\n\n");
    }
    else
    {
      //System.out.println("sleep:" + waitingThreads[quieueuId]);
      try{
        this.wait();
        //System.out.println("wokenUP:" + Arrays.toString(waitingThreads) + " | q: " + quieueuId);
      }
      catch( final InterruptedException e ){
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return this.requestedFile[quieueuId];
  }

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#resetQueue()
   */
  @Override
  public void resetQueue() {
    this.fileQueue.resetQueue();
  }

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#setInfo(java.lang.String)
   */
  @Override
  public void setInfo(final String info) {
    this.fileQueue.setInfo(info);
  }

  /* (non-Javadoc)
   * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#cleanUp()
   */
  @Override
  public void cleanUp() {
    this.fileQueue.cleanUp();
  }

}
