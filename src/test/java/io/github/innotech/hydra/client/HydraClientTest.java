package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import io.github.innotech.hydra.client.balancing.policies.DelegatedPolicy;
import io.github.innotech.hydra.client.exceptions.InaccessibleServer;
import io.github.innotech.hydra.client.exceptions.NoneServersAccessible;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.junit.Before;
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

	private static final Integer CONNECTION_TIMEOUT = 1000;

	private static String TEST_HYDRA_SERVER_URL = "http://localhost:8080";
	
	private static String ANOTHER_TEST_HYDRA_SERVER_URL = "http://localhost:8081";
	
	private static String TEST_HYDRA_SERVER = TEST_HYDRA_SERVER_URL + "/app/hydra";
	
	private static String TEST_APP_SERVER = "http://localhost:8080/app-server";
	
	private static String TEST_APP_SERVER_SECOND = "http://localhost:8080/app-server-second";
	
	private static String APP_ID = "testAppId";

	private LinkedHashSet <String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_HYDRA_SERVER_URL);
			this.add(ANOTHER_TEST_HYDRA_SERVER_URL);
		}
	};

	private LinkedHashSet <String> TEST_APP_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_APP_SERVER);
			this.add(TEST_APP_SERVER_SECOND);
		}
	};
	
	private LinkedHashSet <String> TEST_EMPTY_SERVERS = new LinkedHashSet<String>();
	
	@Mock
	private HydraServersRequester hydraServersRequester;
	
	@Mock
	private DelegatedPolicy delegatedPolicyExecutor;

	@Before
	public void setup() throws Exception{
		PowerMockito.whenNew(HydraServersRequester.class).withNoArguments().thenReturn(hydraServersRequester);
		PowerMockito.whenNew(DelegatedPolicy.class).withNoArguments().thenReturn(delegatedPolicyExecutor);
		when(delegatedPolicyExecutor.balance(TEST_APP_SERVERS)).thenReturn(TEST_APP_SERVERS);
		when(delegatedPolicyExecutor.balance(TEST_HYDRA_SERVERS)).thenReturn(TEST_HYDRA_SERVERS);
	}

	@Test
	public void shouldReturnTheListOfServers() throws Exception {		
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Set<String> candidateUrls = hydraClient.get(APP_ID);

		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_APP_SERVERS,candidateUrls);
		
		verify(delegatedPolicyExecutor).balance(TEST_APP_SERVERS);
	}
	
	@Test
	public void shouldRemoveAServerThatFails() throws Exception{
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(APP_ID);
		
		//Call twice to ensure that the second call hit the cache. 
		hydraClient.removeServer(APP_ID,TEST_APP_SERVER_SECOND);
		Set<String> candidateAfterRemoveUrls = hydraClient.get(APP_ID);
		
		assertTrue("The list of string with the candidate urls", !candidateAfterRemoveUrls.contains(TEST_APP_SERVER_SECOND));
	}
	
	@Test(expected=NoneServersAccessible.class)
	public void shouldRemoveAppIfAllServerFails() throws Exception{		
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setMaxNumberOfRetries(10);
		hydraClient.get(APP_ID);
		
		//Call twice to ensure that the second call hit the cache. 
		hydraClient.get(APP_ID);
		hydraClient.removeServer(APP_ID,TEST_APP_SERVER_SECOND);
		hydraClient.removeServer(APP_ID,TEST_APP_SERVER);
		
		hydraClient.get(APP_ID);
	}
	
	@Test
	public void shouldReturnTheListOfServersAsync() throws Exception {		
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient = PowerMockito.spy(hydraClient);
		
		when(hydraClient.isHydraAvailable()).thenReturn(true);
		Future<LinkedHashSet<String>> async = hydraClient.getAsync(APP_ID);
		
		Set<String> candidateUrls = async.get();
		
		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_APP_SERVERS,candidateUrls);
		
		verify(delegatedPolicyExecutor).balance(TEST_APP_SERVERS);
	}
	
	@Test(expected=NoneServersAccessible.class)
	public synchronized void shouldThrowIfHydraIsNotAvailable() throws Exception {
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenThrow(new NoneServersAccessible());

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Future<LinkedHashSet<String>> async = hydraClient.getAsync(APP_ID);
		
		async.get();
	}
	
	@Test
	public synchronized void shouldReloadHydraServers() throws Exception {
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT, HYDRA)).thenReturn(TEST_HYDRA_SERVERS);
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.reloadHydraServers();
		
		wait(1000);
		
		verify(hydraServersRequester).getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,HYDRA);
		verify(delegatedPolicyExecutor).balance(TEST_HYDRA_SERVERS);
	}
	
	@Test
	public synchronized void shouldFailToReloadCacheHydraReturnEmptyArray() throws Exception {
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT, HYDRA)).thenReturn(TEST_EMPTY_SERVERS);
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT, HYDRA)).thenReturn(TEST_EMPTY_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		
		hydraClient.setMaxNumberOfRetries(1);
		hydraClient.setConnectionTimeout(100);
		
		hydraClient.reloadHydraServers();
		
		wait(1000);
		
		assertTrue("Hydra should be marked as unavailable", !hydraClient.isHydraAvailable());
		assertEquals("Hydraservers should be the hydraservers before the test",TEST_HYDRA_SERVERS ,hydraClient.getHydraServers());
	}
	
	@Test
	public void shouldCallShortcuttingTheCache() throws Exception{		
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenReturn(TEST_APP_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(APP_ID,true);
		
		//Call twice to ensure that the second call hit the cache. 
		hydraClient.get(APP_ID,true);
		
		verify(hydraServersRequester,times(2)).getCandidateServers(TEST_HYDRA_SERVER_URL +APP_ROOT,APP_ID);
		verify(delegatedPolicyExecutor,times(2)).balance(TEST_APP_SERVERS);
	}
	
	@Test
	public void shouldCallShortcuttingTheCacheTheFirstServerFails() throws Exception{		
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenReturn(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		Set<String> candidateUrls = hydraClient.get(APP_ID,true);
		
		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_HYDRA_SERVERS,candidateUrls);
	}
	
	@Test
	public void shouldSetConnectionTimeoutForRequester() throws Exception{
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setConnectionTimeout(CONNECTION_TIMEOUT);

		verify(hydraServersRequester).setConnectionTimeout(CONNECTION_TIMEOUT);
	}

	@Test(expected=NoneServersAccessible.class)
	public void shouldCallShortcuttingTheCacheTheSecondServerFails() throws Exception{
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID)).thenThrow(new InaccessibleServer());

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setMaxNumberOfRetries(1);
		hydraClient.get(APP_ID,true);
	}
	
	@Test
	public synchronized void shouldSucceedToReloadCacheAndSetHydraAvailable() throws Exception{
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT, HYDRA)).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setMaxNumberOfRetries(1);
		hydraClient.setConnectionTimeout(100);
		hydraClient.reloadHydraServers();
		
		wait(1000);
		
		assertTrue("Hydra should be marked as available", hydraClient.isHydraAvailable());
	}
	
	@Test
	//@Ignore
	public synchronized void shouldFailToReloadCacheAndSetHydraNotAvailable() throws Exception{
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,HYDRA)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getCandidateServers(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT,HYDRA)).thenThrow(new InaccessibleServer());
		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setMaxNumberOfRetries(1);
		hydraClient.setConnectionTimeout(100);
		
		hydraClient.reloadHydraServers();
		
		wait(1000);
		
		assertTrue("Hydra should be marked as unavailable", !hydraClient.isHydraAvailable());
	}
	
	@Test
	public void shouldReloadTheAppCache() throws Exception{
		when(hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER_URL + APP_ROOT,APP_ID))
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
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAcceptEmptyAplications() throws Exception{		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get("");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotAcceptWhiteSpaceOnlyAplications() throws Exception{
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get("      ");
	}
}
