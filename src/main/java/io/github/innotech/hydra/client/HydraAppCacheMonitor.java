package io.github.innotech.hydra.client;

import java.util.TimerTask;


/**
 * Invalidate application's cache in the hydra client.
 */
class HydraAppCacheMonitor extends TimerTask {

	private HydraClient hydraClient;
	
	public HydraAppCacheMonitor(HydraClient hydraClient) {
		this.hydraClient = hydraClient;
	}

	@Override
	public void run() {
		hydraClient.reloadApplicationCache();
	}
}
