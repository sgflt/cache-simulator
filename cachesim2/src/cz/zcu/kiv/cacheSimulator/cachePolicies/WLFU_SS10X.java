package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 */
public class WLFU_SS10X extends WLFU_SS10 {
	
	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
	 */
	@Override
	public void insertFile(FileOnClient f) {		

		if ( f.getFileSize() > this.getCacheCapacity() / 2 )
			return;
       
		super.insertFile(f);
	}

	
	@Override
	public String toString(){
		return "WLFU-SS10X";
	}
	
	@Override
	public String cacheInfo(){
		return "WLFU_SS10X;WLFU-SS10X";
	}
}
