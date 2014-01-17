package io.github.innotech.hydra.client;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The client of hydra only expose the get method.
 */
public class HydraClient {

	private HydraServersRequester hydraServerRequester = new HydraServersRequester();
	
	private final static String HYDRA_APP_ID = "hydra";
	
	private LinkedHashSet<String> hydraServers;
	
	private Map<String,LinkedHashSet<String>> appServersCache = new HashMap<String,LinkedHashSet<String>>();
	
	/**
	 * Use an atomic boolean because the timer thread can invalidate the cache in the same moment
	 * that other threads can use the variable.
	 */
	private AtomicBoolean validCache = new AtomicBoolean(true);
	
	/**
	 * The constructor have default visibility because only the factory can create hydra clients.
	 */
	HydraClient(LinkedHashSet<String> seedHydraServers) {
		this.hydraServers = seedHydraServers;
	}
	
	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete application. 
	 * This method must use the cache. 
	 */
	public LinkedHashSet<String> get(String appId) {
		return get(appId,false);
	}

	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete application. 
	 * This method can shortcut the cache. 
	 */
	public LinkedHashSet<String> get(String appId, boolean shortcutCache) {
		if (appServersCache.containsKey(appId) && !shortcutCache && validCache.get()){
			return appServersCache.get(appId);
		} else {
			LinkedHashSet<String> candidateServers = hydraServerRequester.getCandidateServers(getActiveHydraServer(), appId);
			appServersCache.put(appId, candidateServers);
			return candidateServers;
		}
	}
	
	private String getActiveHydraServer() {
		return hydraServers.iterator().next();
	}

	void reloadHydraServers() {
		hydraServers = get(HYDRA_APP_ID,true);
	}

	void invalidateAppCache() {
		validCache.set(false);
	}
}

