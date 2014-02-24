package io.github.innotech.hydra.client.balancing.policies;

import java.util.LinkedHashSet;

public interface BalancingPolicy {

	LinkedHashSet<String> balance(LinkedHashSet<String> servers);

}
