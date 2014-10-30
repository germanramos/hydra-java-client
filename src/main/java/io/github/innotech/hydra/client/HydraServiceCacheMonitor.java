package io.github.innotech.hydra.client;

import java.util.TimerTask;

/**
 * Timer task that reload hydra server cache.
 */
class HydraServiceCacheMonitor extends TimerTask {
	
	public HydraServiceCacheMonitor(HydraClient hydraClient) {
		this.hydraClient = hydraClient;
	}

	@Override
	public void run() {
		hydraClient.initHydraService();
	}

	private HydraClient hydraClient;
}
