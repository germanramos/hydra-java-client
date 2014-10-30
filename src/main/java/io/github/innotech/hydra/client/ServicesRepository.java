package io.github.innotech.hydra.client;

import io.github.innotech.hydra.client.balancing.policies.BalancingPolicy;
import io.github.innotech.hydra.client.balancing.policies.DelegatedPolicy;
import io.github.innotech.hydra.client.exceptions.InaccessibleServer;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class ServicesRepository {

	LinkedHashSet<String> findById(String serviceId,LinkedHashSet<String> hydraServers) {
		LinkedHashSet<String> newCandidateServers = new LinkedHashSet<String>();

		Integer retries = 0;
		
		// Infinite loop if maxNumberOfRetries is set to 0.
		// In this case retries can overflow it value, java automatically set to
		// the integer minimum value an the loop goes on
		while (maxNumberOfRetries == 0 || retries < maxNumberOfRetries) {
			for (String hydraServer : hydraServers) {
				try {
					newCandidateServers = hydraServerRequester.getServicesById(hydraServer + APP_ROOT, serviceId);
					return policy.balance(newCandidateServers);
				} catch (InaccessibleServer e) {
					continue;
				}
			}
			
			retries++;
			waitUntilTheNextRetry();
		}

		return newCandidateServers;
	}

	Map<String, LinkedHashSet<String>> findByIds(Set<String> applications,LinkedHashSet<String> hydraServers) {
		Map<String, LinkedHashSet<String>> newAppServerCache = new HashMap<String, LinkedHashSet<String>>();
		
		for (String applicationId : applications) {
			LinkedHashSet<String> newAppServers = findById(applicationId,hydraServers);
			newAppServerCache.put(applicationId, newAppServers);
		}
		
		return newAppServerCache;
	}
	
	private void waitUntilTheNextRetry() {
		try {
			TimeUnit.MILLISECONDS.sleep(waitBetweenAllServersRetry);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
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

	void setBalancingPolicy(BalancingPolicy policy) {
		this.policy = policy;
	}

	void setConnectionTimeout(Integer connectionTimeout) {
		hydraServerRequester.setConnectionTimeout(connectionTimeout);
	}

	ServicesRepository() {
	}
	
	private HydraRequester hydraServerRequester = new HydraRequester();
	
	private BalancingPolicy policy = new DelegatedPolicy();

	private final static String APP_ROOT = "/app";

	private Integer maxNumberOfRetries = 0;

	private Integer waitBetweenAllServersRetry = 0;

}
