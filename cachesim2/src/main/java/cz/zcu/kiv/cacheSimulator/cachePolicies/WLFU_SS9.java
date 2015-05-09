package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 * 
 * Opačný WLFU_SS8
 */
public class WLFU_SS9 extends LFU_SS {

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
			double penalty = pair.getFirst() * pair.getSecond().getFileSize() / cacheMaxSize;
			
			
			pair.setFirst(pair.getFirst() - 1 + penalty );
		}
		
		needSort = true;
		super.removeFile();
	}
	
	@Override
	public String toString(){
		return "WLFU-SS9";
	}
	
	@Override
	public String cacheInfo(){
		return "WLFU_SS9;WLFU-SS9";
	}
}