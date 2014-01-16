package io.github.innotech.hydra.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HydraClientFactory.class)
public class HydraClientFactoryTest {

	private static final String SEED_SERVER = "http://localhost:8080";

	LinkedHashSet <String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(SEED_SERVER);
		}
	};
	
	@Mock
	private HydraClient hydraClient;
	
	@After
	public void reset(){
		HydraClientFactory.getInstance().reset();
	}
	
	
	@Test
	public void shouldReturnAnUniqueInstanceOfAFactory(){
		HydraClientFactory firstInstance =  HydraClientFactory.getInstance();
		HydraClientFactory secondInstance =  HydraClientFactory.getInstance();
		
		assertSame("The first instance must be equals to second instance",firstInstance,secondInstance);
	}

	@Test
	public void shouldGetHydraUniqueClient() throws Exception{
		PowerMockito.whenNew(HydraClient.class).withAnyArguments().thenReturn(hydraClient);
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = HydraClientFactory.getInstance().hydraClient();
		HydraClient otherHydraClient = HydraClientFactory.getInstance().hydraClient();
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("The second client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
	
	@Test
	public void shouldGetHydraUniqueClientWhenCallConfigManyTimes() throws Exception{
		PowerMockito.whenNew(HydraClient.class).withAnyArguments().thenReturn(hydraClient);
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = HydraClientFactory.getInstance().hydraClient();
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		HydraClient otherHydraClient = HydraClientFactory.getInstance().hydraClient();
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("The second client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
	
	@Test
	public void shouldCallToRefreshHydraServerMethods() throws Exception{
		PowerMockito.whenNew(HydraClient.class).withAnyArguments().thenReturn(hydraClient);
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);

		verify(hydraClient).reloadHydraServers();
	}
}
