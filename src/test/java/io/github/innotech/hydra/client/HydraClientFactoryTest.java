package io.github.innotech.hydra.client;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;

import org.junit.Test;

public class HydraClientFactoryTest {

	private static final String SEED_SERVER = "http://localhost:8080";

	LinkedHashSet <String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(SEED_SERVER);
		}
	};
	
	@Test
	public void shouldReturnAnUniqueInstanceOfAFactory(){
		HydraClientFactory firstInstance =  HydraClientFactory.getInstance();
		HydraClientFactory secondInstance =  HydraClientFactory.getInstance();
		
		assertSame("The first instance must be equals to second instance",firstInstance,secondInstance);
	}

	@Test
	public void shouldGetHydraUniqueClient(){
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = HydraClientFactory.getInstance().hydraClient();
		HydraClient otherHydraClient = HydraClientFactory.getInstance().hydraClient();
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("Client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
	
	@Test
	public void shouldGetHydraUniqueClientWhenCallConfigManyTimes(){
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = HydraClientFactory.getInstance().hydraClient();
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		HydraClient otherHydraClient = HydraClientFactory.getInstance().hydraClient();
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("Client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
}
