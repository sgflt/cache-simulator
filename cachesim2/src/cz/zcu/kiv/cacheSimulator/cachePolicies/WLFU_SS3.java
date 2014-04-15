package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 * 
 * Algoritmus stejně jako WLFU_SS a WLFUSS2 se snaží ponechat velké soubory v cache.
 * 
 * Tentokrát se k hitům přičítá pouze váha.
 */
public class WLFU_SS3 extends LFU_SS implements ICache {

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
			pair.setFirst(pair.getFirst() + pair.getSecond().getFileSize() / cacheMaxSize );
		}
		
		needSort = true;
		super.removeFile();
	}
	
	@Override
	public String toString(){
		return "WLFU-SS3";
	}
	
	@Override
	public String cacheInfo(){
		return "WLFU_SS3;WLFU-SS3";
	}
}
