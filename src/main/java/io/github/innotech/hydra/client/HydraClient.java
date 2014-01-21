package io.github.innotech.hydra.client;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * The client of hydra only expose the get method.
 */
public class HydraClient {

	private HydraServersRequester hydraServerRequester = new HydraServersRequester();

	private final static String HYDRA_APP_ID = "hydra";

	private LinkedHashSet<String> hydraServers;

	private Map<String, LinkedHashSet<String>> appServersCache = new HashMap<String, LinkedHashSet<String>>();

	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private ReentrantReadWriteLock hydraServersReadWriteLock = new ReentrantReadWriteLock();
	
	/**
	 * The constructor have default visibility because only the factory can
	 * create hydra clients.
	 */
	HydraClient(LinkedHashSet<String> seedHydraServers) {
		this.hydraServers = seedHydraServers;
	}

	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete
	 * application. This method must use the cache.
	 */
	public LinkedHashSet<String> get(String appId) {
		return get(appId, false);
	}

	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete
	 * application. This method can shortcut the cache.
	 */
	public LinkedHashSet<String> get(String appId, boolean shortcutCache) {
		ReadLock readLock = readWriteLock.readLock();

		try {
			readLock.lock();
			if (appServersCache.containsKey(appId) && !shortcutCache) {
				return appServersCache.get(appId);
			} else {
				return requestCandidateServers(appId);
			}
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * Use a read lock to ensure that not use hydra servers when write. 
	 */
	private String getActiveHydraServer() {
		ReadLock readLock = hydraServersReadWriteLock.readLock();
		
		try{
			readLock.lock();
			return hydraServers.iterator().next();
		} finally{
			readLock.unlock();
		}
	}

	void reloadHydraServers() {
		LinkedHashSet<String> newHydraServers = requestCandidateServers(HYDRA_APP_ID);
		WriteLock writeLock = hydraServersReadWriteLock.writeLock();
		
		try{
			writeLock.lock();
			hydraServers = newHydraServers;
		} finally{
			writeLock.unlock();
		}
	}
	
	/**
	 * This method reload the data of the app cache. First requests the hydra server for the new information
	 * for the register apps. Then refresh the data of all the app. This process is made by other thread to synchronize 
	 * with the real time operation we use a write/read lock. Only a thread in write mode is allowed to actualize the 
	 * cache.
	 */
	void reloadApplicationCache() {
		refreshAppCache(retrieveNewServerConfiguration());
	}

	private Map<String, LinkedHashSet<String>> retrieveNewServerConfiguration() {
		Set<String> applications = getApplicationIds();
		Map<String, LinkedHashSet<String>> newAppServerCache = new HashMap<String, LinkedHashSet<String>>();

		for (String applicationId : applications) {
			LinkedHashSet<String> newAppServers = requestCandidateServers(applicationId);
			newAppServerCache.put(applicationId, newAppServers);
		}

		return newAppServerCache;
	}

	private Set<String> getApplicationIds() {
		ReadLock readLock = readWriteLock.readLock();
		try {
			readLock.lock();
			return appServersCache.keySet();
		} finally {
			readLock.unlock();
		}
	}

	private void refreshAppCache(Map<String, LinkedHashSet<String>> newAppServerCache) {
		WriteLock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		appServersCache = newAppServerCache;
		writeLock.unlock();
	}
	
	private LinkedHashSet<String> requestCandidateServers(String appId) {
		return hydraServerRequester.getCandidateServers(getActiveHydraServer(), appId);
	}
}
