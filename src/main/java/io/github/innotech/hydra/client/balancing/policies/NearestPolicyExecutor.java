package io.github.innotech.hydra.client.balancing.policies;

import io.github.innotech.hydra.client.balancing.utils.ping.PingClient;
import io.github.innotech.hydra.client.balancing.utils.ping.comparator.ValueComparator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;


public class NearestPolicyExecutor implements BalancingPolicyExecutor {
	
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
				Double latency = pingClient.getLatency(server).get();
				map.put(server, latency);				
			} catch(Exception e){
				e.printStackTrace();
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
