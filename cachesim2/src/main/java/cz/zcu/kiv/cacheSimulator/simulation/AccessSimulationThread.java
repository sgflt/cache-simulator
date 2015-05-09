/**
 * AccessSimulationThread.java
 */
package cz.zcu.kiv.cacheSimulator.simulation;

import cz.zcu.kiv.cacheSimulator.cachePolicies.ICache;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LFU_SS;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS;
import cz.zcu.kiv.cacheSimulator.consistency.IConsistencySimulation;
import cz.zcu.kiv.cacheSimulator.dataAccess.RequstedFile;
import cz.zcu.kiv.cacheSimulator.server.FileOnServer;
import cz.zcu.kiv.cacheSimulator.server.Server;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import cz.zcu.kiv.cacheSimulator.shared.Triplet;

/**
 * AccessSimulationThread.java
 *		12. 4. 2014
 *
 * @author Lukáš Kvídera, A11B0421P
 *
 */
public class AccessSimulationThread implements Runnable {
	
	private AccessSimulation simulation;
	private int tid; /* thread id */
	private FileFactorySync fileFactory;	
	
	/**
	 * promenna pro praci s konzistentnosti
	 */
	private IConsistencySimulation consistency;
	
	/**
	 * promenna pro uchovani odkazu na server
	 */
	private Server server = Server.getInstance();
	
	public AccessSimulationThread(AccessSimulation simulation, FileFactorySync fileFactory,
			IConsistencySimulation consistency, int threadId)
	{
		this.tid = threadId;
		this.simulation = simulation;
		this.fileFactory = fileFactory;
		this.consistency = consistency;
	}
	
	private void discardThread()
	{
		//System.out.println("thread is going away");
		simulation.discardThread();
		fileFactory.discardThread();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		SimulatedUser user;
		RequstedFile file;
		//int fileN = 0;
		do {
			file = fileFactory.getNextServerFile();
			if (file == null)
				break;
		
			user = simulation.getUser(file.getUserID());
			
			if ( tid >= user.getCaches().size() )
			{
				discardThread();
				return;
			}
			
			//fileN++;
			FileOnServer fOnServer = server.existFileOnServer(file.getFname());
			
			
			// zvysime pocet pristupovanych souboru jen jednou, ostatní vlákna počet sdílejí			
			if ( file.isRead() && tid == 0 ) {
				user.incereaseFileAccess();
				user.increaseTotalNetworkBandwidth(fOnServer.getFileSize());
			}

			
			//iterování přes algoritmy
			//for (Triplet<ICache[], Long[], Long[]> cache : user.getCaches()) {
						
			for (int alg = tid; alg < user.getCaches().size(); alg += simulation.threads) {				
				Triplet<ICache[], Long[], Long[]> cache = user.getCaches().get(alg);
		
				for (int i = 0; i < cache.getFirst().length; i++) {
       				// soubor je jiz v cache, aktualizujeme pouze statistiky
    				FileOnClient fOnClient = cache.getFirst()[i].getFileFromCache(file.getFname());
    				
    				//System.out.println("alg: " + alg + " | size: " + cache.getFirst()[i].getCacheCapacity()/(1024*1024) + " | file: " + file.getFname() + " | hits: " + cache.getSecond()[i] + " | tid: " + tid + " | fN: " + fileN);
    				if (fOnClient != null) {
    					//soubor je cten, zvysime read hit ratio
    					if (file.isRead()){
    						cache.getSecond()[i]++;
    						cache.getThird()[i] += fOnClient.getFileSize();
    
    						//kontrola konzistentnosti
    						if (consistency != null){
    							consistency.updateActualReadFile(cache.getFirst()[i], file.getUserID(),fOnClient, fOnServer);
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
    							server.getFileRead(file.getFname(),
    									cache.getFirst()[i]);
    					}
    					//soubor byl zapisovan, je treba resit konzistenci
    					else{
    //						if (consistency != null){
    //							consistency.updateConsistencyWrite(cache.getFirst()[i], file.getUserID(),fOnClient, fOnServer);
    //						}
    //						//pokud neni kontrola konzistentnosti, tak alespon upravime velikost souboru
    //						else{
    //							fOnClient.updateSize(fOnServer);
    //						}
    						fOnServer.increaseWriteHit(cache.getFirst()[i].hashCode());
    		
    					}
    				}
    				// soubor neni v cache, musi se pro nej vytvorit zaznam
    				else {
    					// ukladame jen soubory, ktere jsou ctene a zvysujeme statistiky
    					if (file.isRead()){
    					cache.getFirst()[i].insertFile(new FileOnClient(server
    							.getFileRead(file.getFname(),
    									cache.getFirst()[i]),
    							cache.getFirst()[i], file.getAccessTime()));
    					}
    				}
    			}		
			}
		} while ( file != null && simulation.doSimulation() );
		fileFactory.cleanUp();
	}

}
