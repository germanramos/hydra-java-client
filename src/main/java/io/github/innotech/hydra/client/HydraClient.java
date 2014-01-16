package io.github.innotech.hydra.client;

import java.util.LinkedHashSet;
import java.util.Set;

public class HydraClient {

	private HydraServersRequester hydraServerRequester = new HydraServersRequester();
	
	private LinkedHashSet<String> hydraServers;
	
	public HydraClient(LinkedHashSet<String> seedHydraServers) {
		this.hydraServers = seedHydraServers;
	}

	public Set<String> get(String appId) {
		return hydraServerRequester.getCandidateServers(hydraServers.iterator().next(), appId);
	}
}

