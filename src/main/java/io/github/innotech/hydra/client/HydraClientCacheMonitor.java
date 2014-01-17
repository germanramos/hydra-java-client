package io.github.innotech.hydra.client;

import java.util.TimerTask;


/**
 * Invalidate application's cache in the hydra client.
 */
public class HydraClientCacheMonitor extends TimerTask {

	private HydraClient hydraClient;
	
	public HydraClientCacheMonitor(HydraClient hydraClient) {
		this.hydraClient = hydraClient;
	}

	@Override
	public void run() {
		hydraClient.invalidateAppCache();
	}
}
