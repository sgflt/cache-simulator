package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 * 
 * 
 */
public class WLFU_SS6 extends LFU_SS implements ICache {

	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
	 */
	@Override
	public void removeFile() {
		
		double cacheMaxSize = 0.0;
		
		for (Pair<Double, FileOnClient> pair : list)
		{
			if ( pair.getSecond().getFileSize() > cacheMaxSize)
				cacheMaxSize = pair.getSecond().getFileSize();
		}
		
		for (Pair<Double, FileOnClient> pair : list)
		{
			pair.setFirst(pair.getFirst() - pair.getFirst() * pair.getSecond().getFileSize() / cacheMaxSize );
		}
		
		needSort = true;
		super.removeFile();
	}
	
	@Override
	public String toString(){
		return "WLFU-SS6";
	}
	
	@Override
	public String cacheInfo(){
		return "WLFU_SS6;WLFU-SS6";
	}
}
