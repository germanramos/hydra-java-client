package io.github.innotech.hydra.client;

import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

class HydraServiceCache {

	HydraServiceCache(LinkedHashSet<String> hydraServers){
		this.hydraSeedServers = hydraServers;
		this.hydraServers = hydraServers; 
	} 

	LinkedHashSet<String> getHydraServers() {
		return new LinkedHashSet<String>(hydraServers);
	}
	
	void refresh(LinkedHashSet<String> newHydraServers) {
		if (!newHydraServers.isEmpty()){
			WriteLock writeLock = hydraServersReadWriteLock.writeLock();
			try {
				writeLock.lock();
				hydraServers = newHydraServers;
			} finally {
				writeLock.unlock();
			}
		}
		else{
			hydraServers.addAll(hydraSeedServers);
		}
	}
	
	private LinkedHashSet<String> hydraServers;

	private LinkedHashSet<String> hydraSeedServers;
	
	private ReentrantReadWriteLock hydraServersReadWriteLock = new ReentrantReadWriteLock();
}
