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
 *		5. 4. 2014
 *
 * @author Lukáš Kvídera, A11B0421P
 *
 */
public class FileFactorySync implements IFileQueue {
	
	private IFileQueue fileQueue;
	
	private int threads;
	private int[] waitingThreads = new int[2];
	private int filesGenerated = 0;
	
	
	/**
	 * promenna pro uchovani odkazu na server
	 */
	private Server server = Server.getInstance();
	
	private RequstedFile[] requestedFile = new RequstedFile[2];
	
	public FileFactorySync(IFileQueue fileQueue, int threads)
	{
		this.threads = threads;
		this.fileQueue = fileQueue;
	}
	
	public synchronized void discardThread()
	{
		threads--;
		if ( threads <= waitingThreads[filesGenerated % 2])
			notifyAll();
	}

	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#getNextServerFile()
	 */
	@Override
	public synchronized RequstedFile getNextServerFile() {
		FileOnServer fOnServer = null;
		int quieueuId = filesGenerated % 2;
		
		/* first thread generates new file for speed up*/
		if ( waitingThreads[quieueuId] == 0 )
		{
			requestedFile[quieueuId] = fileQueue.getNextServerFile();
			
			if ( requestedFile[quieueuId] != null ) {
				fOnServer = server.existFileOnServer(requestedFile[quieueuId].getFname());
				if (fOnServer == null){
					if (requestedFile[quieueuId].getfSize() < 0){
						fOnServer = server.generateRandomFileSize(requestedFile[quieueuId].getFname(),
								GlobalVariables.getMinGeneratedFileSize(),
								GlobalVariables.getMaxGeneratedFileSize());
					}
					else{
						fOnServer = server.insertNewFile(requestedFile[quieueuId].getFname(), requestedFile[quieueuId].getfSize());
					}
				}
				
				//soubor byl zapsan, je treba aktualizovat velikost a verzi souboru 
				else if (!requestedFile[quieueuId].isRead()){
					fOnServer.updateFile(requestedFile[quieueuId].getfSize());
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
		
		waitingThreads[quieueuId]++;
		
		//System.out.println(waitingThreads[quieueuId] + " >= " + threads);
		if ( waitingThreads[quieueuId] >= threads )
		{	
			filesGenerated++;
			waitingThreads[filesGenerated % 2] = 0;
			notifyAll();
			//System.out.println("notifyAll "+Arrays.toString(waitingThreads) + "\n\n");
		}
		else
		{
			//System.out.println("sleep:" + waitingThreads[quieueuId]);
			try{
				wait();				
				//System.out.println("wokenUP:" + Arrays.toString(waitingThreads) + " | q: " + quieueuId);
			}
			catch( InterruptedException e ){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		return requestedFile[quieueuId];
	}

	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#resetQueue()
	 */
	@Override
	public void resetQueue() {
		fileQueue.resetQueue();
	}

	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#setInfo(java.lang.String)
	 */
	@Override
	public void setInfo(String info) {
		fileQueue.setInfo(info);
	}

	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.dataAccess.IFileQueue#cleanUp()
	 */
	@Override
	public void cleanUp() {
		fileQueue.cleanUp();
	}

}
