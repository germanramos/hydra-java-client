package io.github.innotech.hydra.client;

import java.util.TimerTask;

/**
 * Invalidate services cache in the hydra client.
 */
class ServicesCacheMonitor extends TimerTask {

	public ServicesCacheMonitor(HydraClient hydraClient) {
		this.hydraClient = hydraClient;
	}

	@Override
	public void run() {
		hydraClient.reloadServicesCache();
	}

	private HydraClient hydraClient;
}
