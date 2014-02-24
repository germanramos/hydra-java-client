package io.github.innotech.hydra.client.balancing.policies;

import java.util.LinkedHashSet;

/**
 * This is the default police delegate the responsibility of sort result to the hydra server.
 */
public class DelegatedPolicy implements BalancingPolicy{

	@Override
	public LinkedHashSet<String> balance(LinkedHashSet<String> servers) {
		return servers;
	}

}
