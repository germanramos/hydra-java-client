package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.github.innotech.hydra.client.balancing.policies.DelegatedPolicy;
import io.github.innotech.hydra.client.exceptions.NoneServersAccessible;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
@PrepareForTest({ HydraClient.class, ServicesRepository.class })
public class HydraClientTest {

	@Before
	public void setup() throws Exception {
		PowerMockito.whenNew(ServicesRepository.class).withNoArguments().thenReturn(servicesRepository);
		PowerMockito.whenNew(ServicesCache.class).withNoArguments().thenReturn(servicesCache);
		PowerMockito.whenNew(HydraServiceCache.class).withAnyArguments().thenReturn(hydraServiceCache);
	}

	@Test
	public void shouldReturnServicesFromHydraWhenNoServicesCached() throws Exception {
		when(servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS)).thenReturn(TEST_SERVICES);
		when(servicesCache.exists(SERVICE_ID)).thenReturn(false);
		when(hydraServiceCache.getHydraServers()).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);

		LinkedHashSet<String> candidateServices = hydraClient.get(SERVICE_ID);

		assertNotNull("The list of string with the candidate services url", candidateServices);
		assertEquals("The list candidate services urls is not the expected", TEST_SERVICES, candidateServices);

		verify(servicesCache).putService(SERVICE_ID, candidateServices);
	}

	@Test
	public void shouldCallShortcuttingTheCache() throws Exception {
		when(servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS)).thenReturn(TEST_SERVICES);
		when(hydraServiceCache.getHydraServers()).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(SERVICE_ID, true);

		// Call twice to ensure that the second call hit the cache.
		hydraClient.get(SERVICE_ID, true);

		verify(servicesCache, times(2)).putService(SERVICE_ID, TEST_SERVICES);
		verify(servicesRepository, times(2)).findById(SERVICE_ID, TEST_HYDRA_SERVERS);
	}

	@Test
	public void shouldCallUsingTheCache() throws Exception {
		when(servicesCache.exists(SERVICE_ID)).thenReturn(true);
		when(servicesCache.findById(SERVICE_ID)).thenReturn(TEST_SERVICES);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(SERVICE_ID, true);

		// Call twice to ensure that the second call hit the cache.
		LinkedHashSet<String> candidateServices = hydraClient.get(SERVICE_ID);

		assertNotNull("The list of string with the candidate services url", candidateServices);
		assertEquals("The list candidate services urls is not the expected", TEST_SERVICES, candidateServices);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAcceptNullAplications() throws Exception {
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAcceptEmptyAplications() throws Exception {
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAcceptWhiteSpaceOnlyAplications() throws Exception {
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.get("      ");
	}

	@Test
	public void shouldReturnTheListOfServersAsync() throws Exception {
		when(servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS)).thenReturn(TEST_SERVICES);
		when(servicesCache.exists(SERVICE_ID)).thenReturn(false);
		when(hydraServiceCache.getHydraServers()).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setHydraAvailable(true);
		
		Future<LinkedHashSet<String>> async = hydraClient.getAsync(SERVICE_ID);

		Set<String> candidateUrls = async.get();

		assertNotNull("The list of string with the candidate urls", candidateUrls);
		assertEquals("The list candidate server is not the expected", TEST_SERVICES, candidateUrls);
	}

	@Test(expected = NoneServersAccessible.class)
	public void shouldThrowIfHydraIsNotAvailable() throws Exception {
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setHydraAvailable(false);
		Future<LinkedHashSet<String>> async = hydraClient.getAsync(SERVICE_ID);

		async.get();
	}

	@Test
	public void shouldReloadHydraServers() throws Exception {
		when(servicesRepository.findById(HYDRA, TEST_HYDRA_SERVERS)).thenReturn(TEST_HYDRA_SERVERS);
		when(hydraServiceCache.getHydraServers()).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);

		hydraClient.reloadHydraServiceCache();

		verify(hydraServiceCache).refresh(TEST_HYDRA_SERVERS);
	}
	
	@Test
	public void shouldInitHydraServers() throws Exception {
		when(servicesRepository.findById(HYDRA, TEST_HYDRA_SERVERS)).thenReturn(TEST_HYDRA_SERVERS);
		when(hydraServiceCache.getHydraServers()).thenReturn(TEST_HYDRA_SERVERS);

		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);

		hydraClient.initHydraService();

		verify(hydraServiceCache,timeout(3000)).refresh(TEST_HYDRA_SERVERS);
	}

	@Test
	public void shouldSetConnectionTimeoutForRequester() throws Exception {
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.setConnectionTimeout(CONNECTION_TIMEOUT);

		verify(servicesRepository).setConnectionTimeout(CONNECTION_TIMEOUT);
	}

	@Test
	public void shouldReloadTheServiceCache() throws Exception {
		LinkedHashSet<String> appIds = new LinkedHashSet<String>();
		appIds.add(SERVICE_ID);
		
		Map<String,LinkedHashSet<String>> services = new HashMap<String, LinkedHashSet<String>>();
		services.put(SERVICE_ID, TEST_SERVICES);
		
		when(servicesRepository.findByIds(appIds, TEST_HYDRA_SERVERS)).thenReturn(services);
		when(servicesCache.getIds()).thenReturn(appIds);
		when(hydraServiceCache.getHydraServers()).thenReturn(TEST_HYDRA_SERVERS);
		
		HydraClient hydraClient = new HydraClient(TEST_HYDRA_SERVERS);
		hydraClient.reloadServicesCache();

		verify(servicesCache).refresh(services);
	}

	private static final String HYDRA = "hydra";

	private static final Integer CONNECTION_TIMEOUT = 1000;

	private static String TEST_HYDRA_SERVER_URL = "http://localhost:8080";

	private static String ANOTHER_TEST_HYDRA_SERVER_URL = "http://localhost:8081";

	private static String TEST_APP_SERVER = "http://localhost:8080/app-server";

	private static String TEST_APP_SERVER_SECOND = "http://localhost:8080/app-server-second";

	private static String SERVICE_ID = "testAppId";

	private LinkedHashSet<String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_HYDRA_SERVER_URL);
			this.add(ANOTHER_TEST_HYDRA_SERVER_URL);
		}
	};

	private LinkedHashSet<String> TEST_SERVICES = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_APP_SERVER);
			this.add(TEST_APP_SERVER_SECOND);
		}
	};

	@Mock
	private HydraRequester hydraServersRequester;

	@Mock
	private DelegatedPolicy delegatedPolicyExecutor;

	@Mock
	private ServicesRepository servicesRepository;

	@Mock
	private ServicesCache servicesCache;

	@Mock
	private HydraServiceCache hydraServiceCache;
}
