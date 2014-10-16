package io.github.innotech.hydra.client;

import io.github.innotech.hydra.client.balancing.policies.BalancingPolicy;
import io.github.innotech.hydra.client.balancing.policies.DelegatedPolicy;
import io.github.innotech.hydra.client.exceptions.InaccessibleServer;
import io.github.innotech.hydra.client.exceptions.NoneServersAccessible;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * The client of hydra only expose the get method.
 */
public class HydraClient {

	private HydraServersRequester hydraServerRequester = new HydraServersRequester();

	private final static String HYDRA_APP_ID = "hydra";
	
	private final static String APP_ROOT = "/app";

	private LinkedHashSet<String> hydraServers;


	private Map<String, LinkedHashSet<String>> appServersCache = new HashMap<String, LinkedHashSet<String>>();

	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private ReentrantReadWriteLock hydraServersReadWriteLock = new ReentrantReadWriteLock();

	private Integer maxNumberOfRetries = 0;

	private Integer waitBetweenAllServersRetry = 0;

	private ExecutorService executor = Executors.newFixedThreadPool(3);
	
	private BalancingPolicy policy = new DelegatedPolicy();

	private AtomicBoolean hydraAvailable = new AtomicBoolean(false);

	/**
	 * The constructor have default visibility because only the factory can
	 * create hydra clients.
	 */
	HydraClient(LinkedHashSet<String> seedHydraServers) {
		this.hydraServers = seedHydraServers;
	}

	/**
	 * Return a future with the server request. Avoid the interaction of the calling thread with the network.
	 */
	public Future<LinkedHashSet<String>> getAsync(final String appId) {
		if (!isHydraAvailable()) {
			throw new NoneServersAccessible();
		}
		
		FutureTask<LinkedHashSet<String>> futureTask = new FutureTask<LinkedHashSet<String>>(
				new Callable<LinkedHashSet<String>>() {

					@Override
					public LinkedHashSet<String> call() {
						return get(appId);
					}
				});

		executor.execute(futureTask);

		return futureTask;
	}

	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete
	 * application. This method must use the cache.
	 */
	public LinkedHashSet<String> get(String appId) {
		return get(appId, false);
	}
	
	/**
	 * Remove a server from a application, this method was called normally if the server fails. 
	 */
	public void removeServer(String appId,String server) {
		WriteLock writeLock = readWriteLock.writeLock();
		try {
			writeLock.lock();
			LinkedHashSet<String> appServers = appServersCache.get(appId);
			appServers.remove(server);
			
			if (appServers.isEmpty()){
				appServersCache.remove(appId);
			}
			
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete
	 * application. This method can shortcut the cache.
	 * In android this method must be called in a async task to avoid the interaction of the main thread 
	 * with the network, use getAsync instead.
	 */
	public LinkedHashSet<String> get(String appId, boolean shortcutCache) {
		if (appId == null) {
			throw new IllegalArgumentException();
		}

		if (appId.trim().isEmpty()) {
			throw new IllegalArgumentException();
		}

		ReadLock readLock = readWriteLock.readLock();
		try {
			readLock.lock();
			if (appServersCache.containsKey(appId) && !shortcutCache) {
				return appServersCache.get(appId);
			}
		} finally {
			readLock.unlock();
		}

		return requestCandidateRefreshingCache(appId);
	}

	public Boolean isHydraAvailable() {
		return hydraAvailable.get();
	}

	private LinkedHashSet<String> requestCandidateRefreshingCache(String appId) {
		LinkedHashSet<String> servers = requestCandidateServers(appId);
		refreshCache(appId, servers);

		return servers;
	}

	private void refreshCache(String appId, LinkedHashSet<String> servers) {
		WriteLock writeLock = readWriteLock.writeLock();

		try {
			writeLock.lock();
			appServersCache.put(appId, servers);
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Use a read lock to ensure that not use hydra servers when write.
	 */
	private String getCurrentHydraServer() {
		ReadLock readLock = hydraServersReadWriteLock.readLock();

		try {
			readLock.lock();
			return hydraServers.iterator().next();
		} finally {
			readLock.unlock();
		}
	}
	
	/**
	 * The reload hydra server is executed in isolated thread, this is for android use of the client
	 * avoiding the iteration of the main thread with the network.
	 */
	void reloadHydraServers() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LinkedHashSet<String> newHydraServers = requestCandidateServers(HYDRA_APP_ID);

					WriteLock writeLock = hydraServersReadWriteLock.writeLock();
					try {
						writeLock.lock();
						hydraServers = newHydraServers;
						hydraAvailable.set(true);
					} finally {
						writeLock.unlock();
					}
				} catch(NoneServersAccessible e){
					hydraAvailable.set(false);
				}
			}
		});
	}

	/**
	 * This method reload the data of the app cache. First requests the hydra
	 * server for the new information for the register apps. Then refresh the
	 * data of all the app. This process is made by other thread to synchronize
	 * with the real time operation we use a write/read lock. Only a thread in
	 * write mode is allowed to actualize the cache.
	 */
	void reloadApplicationCache() {
		refreshAppCache(retrieveNewServerConfiguration());
	}

	private Map<String, LinkedHashSet<String>> retrieveNewServerConfiguration() {
		Set<String> applications = getApplicationIds();
		Map<String, LinkedHashSet<String>> newAppServerCache = new HashMap<String, LinkedHashSet<String>>();

		for (String applicationId : applications) {
			LinkedHashSet<String> newAppServers = requestCandidateServers(applicationId);
			newAppServerCache.put(applicationId, newAppServers);
		}

		return newAppServerCache;
	}

	private Set<String> getApplicationIds() {
		ReadLock readLock = readWriteLock.readLock();
		try {
			readLock.lock();
			return appServersCache.keySet();
		} finally {
			readLock.unlock();
		}
	}

	private void refreshAppCache(Map<String, LinkedHashSet<String>> newAppServerCache) {
		WriteLock writeLock = readWriteLock.writeLock();
		try {
			writeLock.lock();
			appServersCache = newAppServerCache;
		} finally {
			writeLock.unlock();
		}
	}

	// This method contains a mutual exclusion section because access the hydra
	// server set
	// to reorder it. Must be called outside any other hydraServersReadWriteLock
	// mutual exclusion
	// region.
	private LinkedHashSet<String> requestCandidateServers(String appId) {
		
		LinkedHashSet<String> newCandidateServers = new LinkedHashSet<String>();
		Integer retries = 0;
		Integer numberOfHydraServers = getNumberOfHydraServers();
		Integer totalNumberOfRetries = maxNumberOfRetries * numberOfHydraServers;

		// Infinite loop if maxNumberOfRetries is set to 0. In this case
		// retries can overflow it value, java automatically set to the integer
		// minimum value
		// an the loop goes on
		while (maxNumberOfRetries == 0 || retries < totalNumberOfRetries) {
			String currentHydraServer = getCurrentHydraServer();
			try {
				newCandidateServers = hydraServerRequester.getCandidateServers(currentHydraServer + APP_ROOT, appId);
				if(newCandidateServers == null || newCandidateServers.isEmpty()) throw new InaccessibleServer();				
				return policy.balance(newCandidateServers);
			} catch (InaccessibleServer e) {
				reorderServers(currentHydraServer);
				retries++;
			}

			if (retries % numberOfHydraServers == 0) {
				waitUntilTheNextRetry();
			}
		}

		throw new NoneServersAccessible();
	}

	private void waitUntilTheNextRetry() {
		try {
			TimeUnit.MILLISECONDS.sleep(waitBetweenAllServersRetry);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	// Rotate the elements put the first elements at the end of the hydra server
	// list.
	private void reorderServers(String currentHydraServer) {
		WriteLock writeLock = hydraServersReadWriteLock.writeLock();

		try {
			writeLock.lock();
			hydraServers.remove(currentHydraServer);
			hydraServers.add(currentHydraServer);
		} finally {
			writeLock.unlock();
		}
	}

	private int getNumberOfHydraServers() {
		ReadLock readLock = hydraServersReadWriteLock.readLock();

		try {
			readLock.lock();
			return hydraServers.size();
		} finally {
			readLock.unlock();
		}
	}

	void setMaxNumberOfRetries(Integer numberOfRetries) {
		this.maxNumberOfRetries = numberOfRetries;
	}

	/**
	 * The time that the client wait between two retries when try in all hydra
	 * servers.
	 */
	void setWaitBetweenAllServersRetry(int millisecondsToRetry) {
		this.waitBetweenAllServersRetry = millisecondsToRetry;
	}

	LinkedHashSet<String> getHydraServers() {
		return hydraServers;
	}

	void setBalancingPolicy(BalancingPolicy policy) {
		this.policy = policy;
	}

	void setConnectionTimeout(Integer connectionTimeout) {
		hydraServerRequester.setConnectionTimeout(connectionTimeout);
	}
}
