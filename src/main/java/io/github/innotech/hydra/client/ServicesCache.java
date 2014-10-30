package io.github.innotech.hydra.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

class ServicesCache {
		
	LinkedHashSet<String> findById(String serviceId){
		ReadLock readLock = readWriteLock.readLock();
	
		try {
			readLock.lock();
			if (servicesCache.containsKey(serviceId)) {
				return new LinkedHashSet<String>(servicesCache.get(serviceId));
			}
		} finally {
			readLock.unlock();
		}
		
		return new LinkedHashSet<String>();
	}
	
	Boolean exists (String serviceId){
		ReadLock readLock = readWriteLock.readLock();
		
		try {
			readLock.lock();
			return servicesCache.containsKey(serviceId);
		} finally {
			readLock.unlock();
		}
	}
	
	Set<String> getIds() {
		ReadLock readLock = readWriteLock.readLock();
		try {
			readLock.lock();
			return new HashSet<String>(servicesCache.keySet());
		} finally {
			readLock.unlock();
		}
	}
	
	void refresh(Map<String, LinkedHashSet<String>> newAppServerCache) {
		WriteLock writeLock = readWriteLock.writeLock();
		try {
			writeLock.lock();
			servicesCache = newAppServerCache;
		} finally {
			writeLock.unlock();
		}
	}
	
	void putService(String appId, LinkedHashSet<String> servers) {
		WriteLock writeLock = readWriteLock.writeLock();
		
		try {
			writeLock.lock();
			servicesCache.put(appId, servers);
		} finally {
			writeLock.unlock();
		}
	}
	
	/**
	 * Remove a server from a application, this method was called normally if the server fails. 
	 */
	void removeService(String servicesId,String server) {
		WriteLock writeLock = readWriteLock.writeLock();
		try {
			writeLock.lock();
			LinkedHashSet<String> services = servicesCache.get(servicesId);
			services.remove(server);
			
			if (services.isEmpty()){
				servicesCache.remove(servicesId);
			}
			
		} finally {
			writeLock.unlock();
		}
	}
	
	private Map<String, LinkedHashSet<String>> servicesCache = new HashMap<String, LinkedHashSet<String>>();

	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
}
