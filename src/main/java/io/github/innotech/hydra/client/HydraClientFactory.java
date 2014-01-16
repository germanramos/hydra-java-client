package io.github.innotech.hydra.client;

import java.util.LinkedHashSet;

/**
 * Singleton class for creation of a hydra client.
 */
public class HydraClientFactory {

	private static HydraClientFactory hydraClientFactory = new HydraClientFactory();
	
	private HydraClient hydraClient;
	
	/**
	 * Default constructor private according the pattern.
	 */
	private HydraClientFactory(){
	}

	public static HydraClientFactory getInstance() {
		return hydraClientFactory;
	}

	/**
	 * Assign the servers for hydra initial search. Create the instance of hydra client,
	 * refresh the hydra server list querying the seed servers. 
	 */
	public void config(LinkedHashSet<String> seedHydraServers) {
		if (hydraClient != null){
			return;
		}
		
		hydraClient = new HydraClient(seedHydraServers);
	}

	public HydraClient hydraClient() {
		return hydraClient;
	}
}
