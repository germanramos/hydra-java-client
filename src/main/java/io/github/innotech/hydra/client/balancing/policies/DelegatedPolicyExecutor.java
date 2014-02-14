package io.github.innotech.hydra.client.balancing.policies;

import java.util.LinkedHashSet;


public class DelegatedPolicyExecutor implements BalancingPolicyExecutor{

	@Override
	public LinkedHashSet<String> balance(LinkedHashSet<String> servers) {
		return servers;
	}

}
