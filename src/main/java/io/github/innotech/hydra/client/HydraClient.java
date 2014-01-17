package io.github.innotech.hydra.client;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * The client of hydra only expose the get method.
 */
public class HydraClient {

	private HydraServersRequester hydraServerRequester = new HydraServersRequester();
	
	private final static String HYDRA_APP_ID = "hydra";
	
	private LinkedHashSet<String> hydraServers;
	
	private Map<String,LinkedHashSet<String>> appServersCache = new HashMap<String,LinkedHashSet<String>>();
	
	//Only the factory can create hydra clients.
	HydraClient(LinkedHashSet<String> seedHydraServers) {
		this.hydraServers = seedHydraServers;
	}

	public LinkedHashSet<String> get(String appId) {
		return get(appId,false);
	}

	public LinkedHashSet<String> get(String appId, boolean shortcutCache) {
		if (appServersCache.containsKey(appId) && !shortcutCache){
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
}

