package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.Collections;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 */
public class QLFU_SS extends LFU_SS {

	@Override
	public void insertFile(FileOnClient f) {    
		
		if ( f.getFileSize() > this.getCacheCapacity() )
			return;
        	
        //pokud se soubor vejde, fungujeme spravne
        while (freeCapacity() < f.getFileSize()) {
        	removeFile();
        }       
       
        
        double readHits = 1;
        list.add(new Pair<Double, FileOnClient>(new Double(readHits), f));      
        
        needSort = true;
	}
	
	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
	 */
	@Override
	public void removeFile() {		
		
		double localReadCount = 0.0;
		
		for (Pair<Double, FileOnClient> files : list) {
			localReadCount += files.getFirst();
		}
		
		for (Pair<Double, FileOnClient> pair : list)
		{     		
    		double penalty = ((double) pair.getSecond().getReadHit() - (double) pair.getSecond().getWriteHit())
    	    		/ (double) globalReadCount * (double) localReadCount;
    		
			pair.setFirst(pair.getFirst() + penalty );
		}
		
		if (needSort) {
			Collections.sort(list, new PairCompare());
		}
		
		needSort = false;
		
		if (list.size() > 0)
			list.remove(0);

		if (list.size() > 2) {
			if ((list.get(list.size() - 1)).getFirst() > 15) {
				for (Pair<Double, FileOnClient> f : list) {
					f.setFirst(f.getFirst() / 2);
				}
			}
		}
	}
	
	@Override
	public String toString(){
		return "QLFU-SS";
	}
	
	@Override
	public String cacheInfo(){
		return "QLFU_SS;QLFU-SS";
	}
}
