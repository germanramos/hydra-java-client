package io.github.innotech.hydra.client.balancing.policies;

import io.github.innotech.hydra.client.balancing.ping.PingClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

/**
 * Sort the servers return by hydra by it's ping response time.
 */
public class NearestPolicy implements BalancingPolicy {
	
	private PingClient pingClient = new PingClient();

	@Override
	public LinkedHashSet<String> balance(LinkedHashSet<String> servers) {
		Map<String, Double> sortedServersWithPing = getLatenciesForServers(servers);
		return convertSortedMapToHashSet(sortedServersWithPing);
	}
	
	private Map<String,Double> getLatenciesForServers(LinkedHashSet<String> servers) {
		HashMap<String, Double> map = new HashMap<String, Double>();
		ValueComparator comparator = new ValueComparator(map);
		TreeMap<String, Double> sortedServersByPing = new TreeMap<String, Double>(comparator);
		
		for(String server: servers){
			
			try {
				URI serverUri = new URI(server);
				Double latency = pingClient.getLatency(serverUri.getHost()).get();
				map.put(server, latency);	
			} catch (InterruptedException e) {
				//If someone interrupt the ping the system can be in erroneous state.
				throw new IllegalStateException("Ping process was interrupted inpropedly",e);
			} catch (ExecutionException e) {
				//If there are an error in the ping execution the system continue to the next result.
				continue;
			} catch (URISyntaxException e) {
				// An error on the url
				continue;
			}
		}			
		
		sortedServersByPing.putAll(map);
		
		return sortedServersByPing;
	}
	
	private LinkedHashSet<String> convertSortedMapToHashSet(Map<String, Double> sortedServersHash){
		LinkedHashSet<String> servers = new LinkedHashSet<String>();

		for(String server: sortedServersHash.keySet()){
			servers.add(server);
		}
		
		return servers;
	}
}
