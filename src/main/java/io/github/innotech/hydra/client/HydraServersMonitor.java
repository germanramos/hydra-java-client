package io.github.innotech.hydra.client;

import java.util.TimerTask;

/**
 * Timer task that reload hydra server cache.
 */
class HydraServersMonitor extends TimerTask {

	private HydraClient hydraClient;
	
	public HydraServersMonitor(HydraClient hydraClient) {
		this.hydraClient = hydraClient;
	}

	@Override
	public void run() {
		hydraClient.reloadHydraServers();
	}
}
