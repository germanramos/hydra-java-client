package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HydraServiceCacheTest {

	@Test
	public void shouldReturnTheCachedServices(){
		HydraServiceCache hydraServiceCache = new HydraServiceCache(TEST_HYDRA_SERVERS);
		
		assertNotNull("Must return a not null set of servers",hydraServiceCache.getHydraServers());
		assertEquals("The server are not the expected",TEST_HYDRA_SERVERS,hydraServiceCache.getHydraServers());
	}
	
	@Test
	public void shouldReturnTheCachedServicesWhenRefresh(){
		HydraServiceCache hydraServiceCache = new HydraServiceCache(TEST_HYDRA_SERVERS);
		
		hydraServiceCache.refresh(TEST_NEW_HYDRA_SERVERS);
		
		assertNotNull("Must return a not null set of servers",hydraServiceCache.getHydraServers());
		assertEquals("The server are not the expected",TEST_NEW_HYDRA_SERVERS,hydraServiceCache.getHydraServers());
	}
	
	@Test
	public void shouldReturnTheCachedServicesAndSeedServicesIfNewAreEmpty(){
		HydraServiceCache hydraServiceCache = new HydraServiceCache(TEST_HYDRA_SERVERS);
		
		hydraServiceCache.refresh(TEST_NEW_HYDRA_SERVERS);
		hydraServiceCache.refresh(new LinkedHashSet<String>());
		
		assertNotNull("Must return a not null set of servers",hydraServiceCache.getHydraServers());
		
		LinkedHashSet<String> allServers = new LinkedHashSet<String>();
		allServers.addAll(TEST_HYDRA_SERVERS);
		allServers.addAll(TEST_NEW_HYDRA_SERVERS);
		assertEquals("The server are not the expected",allServers,hydraServiceCache.getHydraServers());
	}
	
	private LinkedHashSet<String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_HYDRA_SERVER_URL);
		}
	};

	private LinkedHashSet<String> TEST_NEW_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(ANOTHER_TEST_HYDRA_SERVER_URL);
		}
	};

	
	private static String TEST_HYDRA_SERVER_URL = "http://localhost:8080";

	private static String ANOTHER_TEST_HYDRA_SERVER_URL = "http://localhost:8081";
}
