package cz.zcu.kiv.cacheSimulator.cachePolicies;

import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 */
public class WLFU_SS8X extends WLFU_SS8 implements ICache {

	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
	 */
	@Override
	public void insertFile(FileOnClient f) {
		if ( f.getFileSize() > getCacheCapacity() / 2 )
			return;
					
		super.insertFile(f);;
	}
	
	@Override
	public String toString(){
		return "WLFU-SS8X";
	}
	
	@Override
	public String cacheInfo(){
		return "WLFU_SS8X;WLFU-SS8X";
	}
}
