package io.github.innotech.hydra.client;

import java.util.LinkedHashSet;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class for creation of a hydra client.
 */
public class HydraClientFactory {

	private static final Long DEFAULT_HYDRA_SERVER_REFRESH = TimeUnit.SECONDS.toMillis(60l);

	private static final Long DEFAULT_HYDRA_APPS_REFRESH = TimeUnit.SECONDS.toMillis(20l);

	private static final Integer DEFAULT_RETRIES_NUMBER = 10;

	private static HydraClientFactory hydraClientFactory = new HydraClientFactory();

	private HydraClient hydraClient;

	private Long hydraServerRefreshTime = DEFAULT_HYDRA_SERVER_REFRESH;

	private Long hydraAppsRefreshTime = DEFAULT_HYDRA_APPS_REFRESH;

	private Timer hydraTimer;

	private Timer appsTimer;

	private Integer numberOfRetries = DEFAULT_RETRIES_NUMBER;

	private Integer millisecondsToRetry = 0;

	private LinkedHashSet<String> hydraServers = new LinkedHashSet<String>();

	/**
	 * Default constructor private according the pattern.
	 */
	private HydraClientFactory() {
	}

	public static HydraClientFactory config(LinkedHashSet<String> hydraServerUrls) {
		if (hydraServerUrls == null){
			throw new IllegalArgumentException();
		}
		
		if (hydraServerUrls.isEmpty()){
			throw new IllegalArgumentException();
		}
		
		for (String hydraServerUrl : hydraServerUrls) {
			hydraClientFactory.hydraServers.add(hydraServerUrl);
		}
		
		return hydraClientFactory;
	}

	/**
	 * Assign the servers for hydra initial search. Create the instance of hydra
	 * client, refresh the hydra server list querying the seed servers.
	 */
	public HydraClient build() {
		if (hydraClient != null) {
			return hydraClient;
		}

		hydraClient = new HydraClient(hydraServers);
		hydraClient.setMaxNumberOfRetries(numberOfRetries);
		hydraClient.setWaitBetweenAllServersRetry(millisecondsToRetry);
		hydraClient.reloadHydraServers();
		
		configureCacheRefreshTimers();

		return hydraClient;
	}

	private void configureCacheRefreshTimers() {
		hydraTimer = new Timer(true);
		appsTimer = new Timer(true);

		hydraTimer.schedule(new HydraServersMonitor(hydraClient), 0, hydraServerRefreshTime);
		appsTimer.schedule(new HydraAppCacheMonitor(hydraClient), 0, hydraAppsRefreshTime);
	}

	public static HydraClient hydraClient() {
		return hydraClientFactory.getHydraClient();
	}

	// Method that reset the client this methods is needed only for tests.
	static void reset() {
		hydraClientFactory.hydraClient = null;
	}

	public HydraClientFactory withHydraCacheRefreshTime(Long timeOutSeconds) {
		if (timeOutSeconds == null) {
			throw new IllegalArgumentException();
		}

		hydraServerRefreshTime = TimeUnit.SECONDS.toMillis(timeOutSeconds);
		return this;
	}

	public HydraClientFactory withAppsCacheRefreshTime(Long timeOutSeconds) {

		if (timeOutSeconds == null) {
			throw new IllegalArgumentException();
		}

		hydraAppsRefreshTime = TimeUnit.SECONDS.toMillis(timeOutSeconds);
		return this;
	}

	public HydraClientFactory andAppsCacheRefreshTime(Long timeOutSeconds) {
		return withAppsCacheRefreshTime(timeOutSeconds);
	}

	public HydraClientFactory andHydraRefreshTime(Long timeOutSeconds) {
		return withHydraCacheRefreshTime(timeOutSeconds);
	}

	public HydraClientFactory withNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
		return this;
	}

	public HydraClientFactory andNumberOfRetries(int numberOfRetries) {
		return withNumberOfRetries(numberOfRetries);
	}

	public HydraClientFactory waitBetweenAllServersRetry(int millisecondsToRetry) {
		this.millisecondsToRetry = millisecondsToRetry;
		return this;
	}

	private HydraClient getHydraClient() {
		return hydraClient;
	}

}
