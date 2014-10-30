package io.github.innotech.hydra.client;

import io.github.innotech.hydra.client.balancing.policies.BalancingPolicy;
import io.github.innotech.hydra.client.exceptions.HydraNotAvailable;

import java.util.LinkedHashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The client of hydra only expose the get method.
 */
public class HydraClient {
	
	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete
	 * application. This method must use the cache.
	 */
	public LinkedHashSet<String> get(String serviceId) {
		return get(serviceId, false);
	}
	
	/**
	 * Retrieve a list of servers sorted by hydra available for a concrete
	 * application. This method can shortcut the cache.
	 * In android this method must be called in a async task to avoid the interaction of the main thread 
	 * with the network, use getAsync instead.
	 */
	public LinkedHashSet<String> get(String serviceId, boolean shortcutCache) {
		if (serviceId == null) {
			throw new IllegalArgumentException();
		}
		
		if (serviceId.trim().isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		if (!shortcutCache && servicesCache.exists(serviceId)){
			return servicesCache.findById(serviceId);
		}  
		
		LinkedHashSet<String> servers = new LinkedHashSet<String>();
		try {
			servers = servicesRepository.findById(serviceId,hydraServiceCache.getHydraServers());
			servicesCache.putService(serviceId, servers);
		} catch (HydraNotAvailable e) {
			hydraAvailable.set(false);
		}

		return servers;
	}
		
	/**
	 * Return a future with the server request. Avoid the interaction of the calling thread with the network.
	 */
	public Future<LinkedHashSet<String>> getAsync(final String serviceId) {
		FutureTask<LinkedHashSet<String>> futureTask = new FutureTask<LinkedHashSet<String>>(
			new Callable<LinkedHashSet<String>>() {
				
				@Override
				public LinkedHashSet<String> call() {
					return get(serviceId);
				}
			});
		
		executor.execute(futureTask);
		return futureTask;
	}

	public Boolean isHydraAvailable() {
		return hydraAvailable.get();
	}
	
	/**
	 * The init hydra service is executed in isolated thread, this is for android use of the client
	 * avoiding the iteration of the main thread with the network.
	 */
	void initHydraService() {
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				reloadHydraServiceCache();
			}
		});
	}
	
	void reloadHydraServiceCache() {
		try {
			LinkedHashSet<String> newHydraServers = servicesRepository.findById(HYDRA_APP_ID,
					hydraServiceCache.getHydraServers());
			
			hydraServiceCache.refresh(newHydraServers);
			hydraAvailable.set(!newHydraServers.isEmpty());
		} catch (HydraNotAvailable e) {
			hydraAvailable.set(false);
		}
	}

	/**
	 * This method reload the data of the app cache. First requests the hydra
	 * server for the new information for the register apps. Then refresh the
	 * data of all the app. This process is made by other thread to synchronize
	 * with the real time operation we use a write/read lock. Only a thread in
	 * write mode is allowed to actualize the cache.
	 */
	void reloadServicesCache() {
		if (!hydraAvailable.get()){
			return;
		}
		
		try {
			servicesCache.refresh(servicesRepository.findByIds(servicesCache.getIds(),
					hydraServiceCache.getHydraServers()));
		} catch (HydraNotAvailable e) {
			hydraAvailable.set(false);
		}
	}
	
	/**
	 * The constructor have default visibility because only the factory can
	 * create hydra clients.
	 */
	HydraClient(LinkedHashSet<String> seedHydraServers) {
		hydraServiceCache = new HydraServiceCache(seedHydraServers);
	}

	void setMaxNumberOfRetries(Integer numberOfRetries) {
		servicesRepository.setMaxNumberOfRetries(numberOfRetries);
	}
	
	void setWaitBetweenAllServersRetry(Integer millisecondsToRetry) {
		servicesRepository.setWaitBetweenAllServersRetry(millisecondsToRetry);
	}
	
	void setBalancingPolicy(BalancingPolicy policy) {
		servicesRepository.setBalancingPolicy(policy);
	}
	
	void setConnectionTimeout(Integer connectionTimeout) {
		servicesRepository.setConnectionTimeout(connectionTimeout);
	}

	LinkedHashSet<String> getHydraServers() {
		return hydraServiceCache.getHydraServers();
	}
	
	void setHydraAvailable(boolean available){
		hydraAvailable.set(available);
	}
	
	private AtomicBoolean hydraAvailable = new AtomicBoolean(false);
	
	private final static String HYDRA_APP_ID = "hydra";
	
	private ExecutorService executor = Executors.newFixedThreadPool(3);
	
	private ServicesCache servicesCache = new ServicesCache();
	
	private HydraServiceCache hydraServiceCache;
	
	private ServicesRepository servicesRepository = new ServicesRepository();
}
