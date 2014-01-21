package io.github.innotech.hydra.client;

import java.util.TimerTask;

/**
 * Invalidate hydra servers.
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
