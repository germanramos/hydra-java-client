package io.github.innotech.hydra.client;

import java.util.LinkedHashSet;
import static org.junit.Assert.*;

/**
 * Test hydra client against a mock configured in port 5000 with lucius.public.database app.
 *
 */
public class HydraIntegratedTest {

	public static void main(String[] args) {
		LinkedHashSet<String> hydraSeed = new LinkedHashSet<String>();
		
		hydraSeed.add("https://23.236.63.64");
		HydraClient hydraClient = HydraClientFactory.config(hydraSeed).build();
		
		LinkedHashSet<String> linkedHashSet = hydraClient.get("hydra");
		
		System.out.println(linkedHashSet);
		
		assertNotNull("The list of servers must be not null",linkedHashSet);
		assertTrue("The list of servers must have more than one element",0 != linkedHashSet.size());
	}

}
