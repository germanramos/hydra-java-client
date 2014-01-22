package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import io.github.innotech.hydra.client.exceptions.InaccessibleServer;
import io.github.innotech.hydra.client.exceptions.NoneServersAccessible;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HydraClient.class)
public class HydraClientTest {

	private static final String HYDRA = "hydra";

	private static String TEST_HYDRA_SERVER = "http://localhost:8080/hydra-server";
	
	private static String ANOTHER_TEST_HYDRA_SERVER = "http://localhost:8080/another-hydra-server";
	
	private static String TEST_APP_SERVER = "http://localhost:8080/app-server";

	private static String APP_ID = "testAppId";

	LinkedHashSet <String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_HYDRA_SERVER);
			this.add(ANOTHER_TEST_HYDRA_SERVER);
		}
	};

	LinkedHashSet <String> TEST_APP_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_APP_SERVER);
		}
	};

	
	@Mock
	private HydraServersRequester hydraServersRequester;

	@Test
	public void shouldReturnTheListOfServers() throws Exception {		
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER,APP_ID)).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Set<String> candidateUrls = hydraClient.get(APP_ID);

		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_HYDRA_SERVERS,candidateUrls);
	}
	
	@Test
	public void shouldReloadHydraServers() throws Exception {		
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.reloadHydraServers();

		verify(hydraServersRequester).getCandidateServers(TEST_HYDRA_SERVER,HYDRA);
	}
	
	@Test
	public void shouldCallShortcuttingTheCache() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER,APP_ID)).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(APP_ID,true);
		
		//Call twice to ensure that the second call hit the cache. 
		hydraClient.get(APP_ID,true);
		
		verify(hydraServersRequester,times(2)).getCandidateServers(TEST_HYDRA_SERVER,APP_ID);
	}
	
	@Test
	public void shouldCallShortcuttingTheCacheTheFirstServerFails() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER,APP_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER,APP_ID)).thenReturn(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Set<String> candidateUrls = hydraClient.get(APP_ID,true);
		
		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_HYDRA_SERVERS,candidateUrls);
	}
	
	@Test(expected=NoneServersAccessible.class)
	public void shouldCallShortcuttingTheCacheTheSecondServerFails() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER,APP_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER,APP_ID)).thenThrow(new InaccessibleServer());
		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(APP_ID,true);
		
		verify(hydraServersRequester,times(10)).getCandidateServers(TEST_HYDRA_SERVER,APP_ID);
		verify(hydraServersRequester,times(10)).getCandidateServers(ANOTHER_TEST_HYDRA_SERVER,APP_ID);
	}
	
	@Test
	public void shouldReloadTheAppCache() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER,APP_ID))
			.thenReturn(TEST_HYDRA_SERVERS)
			.thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(APP_ID);
		
		hydraClient.reloadApplicationCache();
		
		LinkedHashSet<String> resultServer = hydraClient.get(APP_ID);
		
		assertEquals("The server must be the reloaded",TEST_APP_SERVERS,resultServer);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAcceptNullAplications() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAcceptEmptyAplications() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get("");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAcceptWhiteSpaceOnlyAplications() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get("      ");
	}
}
