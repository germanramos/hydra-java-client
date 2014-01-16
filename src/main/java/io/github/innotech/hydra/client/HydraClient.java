package io.github.innotech.hydra.client;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The client of hydra only expose the get method.
 */
public class HydraClient {

	private HydraServersRequester hydraServerRequester = new HydraServersRequester();
	
	private final static String HYDRA_APP_ID = "hydra";
	
	private LinkedHashSet<String> hydraServers;
	
	//Only the factory can create hydra clients.
	HydraClient(LinkedHashSet<String> seedHydraServers) {
		this.hydraServers = seedHydraServers;
	}

	public Set<String> get(String appId) {
		return hydraServerRequester.getCandidateServers(hydraServers.iterator().next(), appId);
	}

	public void reloadHydraServers() {
		hydraServers = hydraServerRequester.getCandidateServers(hydraServers.iterator().next(), HYDRA_APP_ID);
	}
}

