package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.github.innotech.hydra.client.exceptions.InaccessibleServer;
import io.github.innotech.hydra.client.exceptions.NoneServersAccessible;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HydraClient.class)
public class HydraClientTest {

	private static final String APP_ROOT = "/app";

	private static final String HYDRA = "hydra";

	private static String TEST_HYDRA_SERVER_URL = "http://localhost:8080/";
	
	private static String ANOTHER_TEST_HYDRA_SERVER_URL = "http://localhost:8081";
	
	private static String TEST_HYDRA_SERVER = TEST_HYDRA_SERVER_URL;
	
	private static String ANOTHER_TEST_HYDRA_SERVER = ANOTHER_TEST_HYDRA_SERVER_URL + "app/hydra";
	
	private static String TEST_APP_SERVER = "http://localhost:8080/app-server";
	
	private static String TEST_APP_SERVER_SECOND = "http://localhost:8080/app-server-second";
	
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
			this.add(TEST_APP_SERVER_SECOND);
		}
	};
	
	@Mock
	private HydraServersRequester hydraServersRequester;

	@Test
	public void shouldReturnTheListOfServers() throws Exception {		
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Set<String> candidateUrls = hydraClient.get(APP_ID);

		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_APP_SERVERS,candidateUrls);
	}
	
	@Test
	public void shouldRemoveAServerThatFails() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_APP_SERVERS);
		hydraClient.get(APP_ID);
		
		//Call twice to ensure that the second call hit the cache. 
		hydraClient.removeServer(APP_ID,TEST_APP_SERVER_SECOND);
		Set<String> candidateAfterRemoveUrls = hydraClient.get(APP_ID);
		
		assertTrue("The list of string with the candidate urls", !candidateAfterRemoveUrls.contains(TEST_APP_SERVER_SECOND));
	}
	
	@Test
	public void shouldRemoveAppIfAllServerFails() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(APP_ID);
		
		//Call twice to ensure that the second call hit the cache. 
		hydraClient.get(APP_ID);
		hydraClient.removeServer(APP_ID,TEST_APP_SERVER_SECOND);
		hydraClient.removeServer(APP_ID,TEST_APP_SERVER);
		
		Set<String> candidateAfterRemoveUrls = hydraClient.get(APP_ID);
		
		assertNotNull("The list of string with the candidate urls", candidateAfterRemoveUrls);
		assertEquals("The list candidate server is not the expected", TEST_APP_SERVERS,candidateAfterRemoveUrls);

	}
	
	@Test
	public void shouldReturnTheListOfServersAsync() throws Exception {		
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Future<LinkedHashSet<String>> async = hydraClient.getAsync(APP_ID);
		
		Set<String> candidateUrls = async.get();
		
		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_APP_SERVERS,candidateUrls);
	}
	
	@Test
	public synchronized void shouldReloadHydraServers() throws Exception {		
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.reloadHydraServers();
		
		wait(1000);
		
		verify(hydraServersRequester).getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,HYDRA);
	}
	
	@Test
	public void shouldCallShortcuttingTheCache() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(APP_ID,true);
		
		//Call twice to ensure that the second call hit the cache. 
		hydraClient.get(APP_ID,true);
		
		verify(hydraServersRequester,times(2)).getCandidateServers(TEST_HYDRA_SERVER +APP_ROOT,APP_ID);
	}
	
	@Test
	public void shouldCallShortcuttingTheCacheTheFirstServerFails() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenReturn(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Set<String> candidateUrls = hydraClient.get(APP_ID,true);
		
		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_HYDRA_SERVERS,candidateUrls);
	}
	
	@Test(expected=NoneServersAccessible.class)
	public void shouldCallShortcuttingTheCacheTheSecondServerFails() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER + APP_ROOT,APP_ID)).thenThrow(new InaccessibleServer());
		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setMaxNumberOfRetries(1);
		hydraClient.get(APP_ID,true);
	}
	
	@Test
	public void shouldReloadTheAppCache() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER + APP_ROOT,APP_ID))
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
