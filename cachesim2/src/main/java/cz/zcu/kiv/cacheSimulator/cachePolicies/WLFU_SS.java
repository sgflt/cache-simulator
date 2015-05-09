package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 * 
 *  Algoritmus mění váhu hitů podle jeho velikosti.
 *  
 *  Vetší soubory mají větší váhu a spíše se z cache vyhodí malý.
 */
public class WLFU_SS extends LFU_SS {

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
			pair.setFirst(pair.getFirst() * pair.getSecond().getFileSize() / cacheMaxSize );
		}
		
		needSort = true;
		super.removeFile();
	}
	
	@Override
	public String toString(){
		return "WLFU-SS";
	}
	
	@Override
	public String cacheInfo(){
		return "WLFU_SS;WLFU-SS";
	}
}
