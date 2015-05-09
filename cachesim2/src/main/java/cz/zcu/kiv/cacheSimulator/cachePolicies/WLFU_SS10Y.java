package cz.zcu.kiv.cacheSimulator.cachePolicies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import cz.zcu.kiv.cacheSimulator.shared.Pair;
import cz.zcu.kiv.cacheSimulator.simulation.FileOnClient;

/**
 * @author Lukáš Kvídera, A11B0421P
 * @version 0.0
 * 
 * Tento algoritmus je upně stejnž, jako WLFU_SS5, ale používá magickou jednotku.
 * 
 * Tedy ve výsledku se neodečítá váha hitů, ale přičítá se 1 - váha souboru.
 * 
 * Velkým souborům se hodnota hitů nepatrně zvětší, malé soubory mohou mít výsledné hity až o jedna větší. * 
 */
public class WLFU_SS10Y extends LFU_SS implements ICache {

	/* (non-Javadoc)
	 * @see cz.zcu.kiv.cacheSimulator.cachePolicies.ICache#removeFile()
	 */
	@Override
	public void removeFile() {
		
		double cacheMaxSize = 0.0;		
		ArrayList<Pair<Double, FileOnClient>> priorityQueue = new ArrayList<Pair<Double,FileOnClient>>(list.size());

		for (Pair<Double, FileOnClient> pair : list)
		{
			if ( pair.getSecond().getFileSize() > cacheMaxSize)
				cacheMaxSize = pair.getSecond().getFileSize();
		}
		
		for (Pair<Double, FileOnClient> pair : list)
		{
			double penalty = pair.getSecond().getFileSize() / cacheMaxSize;			
			pair = new Pair<Double, FileOnClient>(pair.getFirst() + 1 - penalty, pair.getSecond());
			priorityQueue.add(pair);
		}
		
		needSort = false;
		Collections.sort(priorityQueue, new PairCompare());
		
		if (list.size() > 0)
			list.remove(priorityQueue.remove(0));

		if (list.size() > 2)
		if ((list.get(list.size() - 1)).getFirst() > 15) {
			for (Pair<Double, FileOnClient> f : list) {
				f.setFirst(f.getFirst() / 2);
			}
		}
		
	}
	
	@Override
	public String toString(){
		return "WLFU-SS10Y";
	}
	
	@Override
	public String cacheInfo(){
		return "WLFU_SS10Y;WLFU-SS10Y";
	}
}
