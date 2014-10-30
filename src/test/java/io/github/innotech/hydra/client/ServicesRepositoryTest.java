package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.github.innotech.hydra.client.exceptions.HydraNotAvailable;
import io.github.innotech.hydra.client.exceptions.InaccessibleServer;
import io.github.innotech.hydra.client.exceptions.IncorrectServerResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
public class ServicesRepositoryTest {
	
	@Before
	public void setup() throws Exception {
		PowerMockito.whenNew(HydraRequester.class).withNoArguments().thenReturn(hydraServersRequester);
	}
	
	@Test
	@PrepareForTest(ServicesRepository.class)
	public void shouldReturnTheServicesFirstServiceResponseById() throws InaccessibleServer, IncorrectServerResponse, HydraNotAvailable{
		ServicesRepository servicesRepository = new ServicesRepository();
		
		when(hydraServersRequester.getServicesById(TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenReturn(TEST_SERVICES);
		LinkedHashSet<String> services = servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS);
		
		assertNotNull("The returned services must be not null",services);
		assertEquals("The returned services must be the expected",TEST_SERVICES,services);
	}
	
	@Test
	@PrepareForTest(ServicesRepository.class)
	public void shouldReturnTheServicesSecondServiceResponseById() throws InaccessibleServer, IncorrectServerResponse, HydraNotAvailable{
		ServicesRepository servicesRepository = new ServicesRepository();
		
		when(hydraServersRequester.getServicesById(TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenThrow(new IncorrectServerResponse());
		when(hydraServersRequester.getServicesById(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenReturn(TEST_SERVICES);
		
		LinkedHashSet<String> services = servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS);
		
		assertNotNull("The returned services must be not null",services);
		assertEquals("The returned services must be the expected",TEST_SERVICES,services);
	}
	
	@Test
	@PrepareForTest(ServicesRepository.class)
	public void shouldReturnEmptyListIfNoneServicesResponse() throws InaccessibleServer, IncorrectServerResponse, HydraNotAvailable{
		ServicesRepository servicesRepository = new ServicesRepository();
		servicesRepository.setMaxNumberOfRetries(1);
		
		when(hydraServersRequester.getServicesById(TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenThrow(new IncorrectServerResponse());
		when(hydraServersRequester.getServicesById(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenThrow(new IncorrectServerResponse());
		
		LinkedHashSet<String> services = servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS);
		
		assertNotNull("The returned services must be not null",services);
		assertEquals("The returned services must be the expected",new LinkedHashSet<String>(),services);
	}
	
	@Test (expected = HydraNotAvailable.class)
	@PrepareForTest(ServicesRepository.class)
	public void shouldReturnHydraNoAvailableExpection() throws InaccessibleServer, IncorrectServerResponse, HydraNotAvailable{
		ServicesRepository servicesRepository = new ServicesRepository();
		servicesRepository.setMaxNumberOfRetries(1);
		
		when(hydraServersRequester.getServicesById(TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenThrow(new InaccessibleServer());
		when(hydraServersRequester.getServicesById(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenThrow(new InaccessibleServer());
		
		servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS);
	}
	
	@Test
	@PrepareForTest(ServicesRepository.class)
	public void shouldRetryNoneServicesResponse() throws InaccessibleServer, IncorrectServerResponse, HydraNotAvailable{
		ServicesRepository servicesRepository = new ServicesRepository();
		servicesRepository.setMaxNumberOfRetries(2);
		
		when(hydraServersRequester.getServicesById(TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenThrow(new IncorrectServerResponse());
		when(hydraServersRequester.getServicesById(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenThrow(new IncorrectServerResponse());
		
		servicesRepository.findById(SERVICE_ID, TEST_HYDRA_SERVERS);
		
		verify(hydraServersRequester,times(2)).getServicesById(TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID);
		verify(hydraServersRequester,times(2)).getServicesById(ANOTHER_TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID);
	}
	
	@Test
	@PrepareForTest(ServicesRepository.class)
	public void shouldReturnAListOfServicesForAnId() throws InaccessibleServer, IncorrectServerResponse, HydraNotAvailable{
		ServicesRepository servicesRepository = new ServicesRepository();
		
		when(hydraServersRequester.getServicesById(TEST_HYDRA_SERVER_URL + APP_ROOT , SERVICE_ID)).thenReturn(TEST_SERVICES);
		HashSet<String> serviceIds = new HashSet<String>();
		serviceIds.add(SERVICE_ID);

		Map<String,LinkedHashSet<String>> services = servicesRepository.findByIds(serviceIds, TEST_HYDRA_SERVERS);
				
		Map<String,LinkedHashSet<String>> expectedServices = new HashMap<String, LinkedHashSet<String>>();
		expectedServices.put(SERVICE_ID, TEST_SERVICES);
		
		assertNotNull("The returned services must be not null",services);
		assertEquals("The returned services must be the expected",expectedServices,services);
	}
	
	@Mock
	private HydraRequester hydraServersRequester;
	
	private LinkedHashSet<String> TEST_SERVICES = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_APP_SERVER);
			this.add(TEST_APP_SERVER_SECOND);
		}
	};
	
	private LinkedHashSet<String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;
	
		{
			this.add(TEST_HYDRA_SERVER_URL);
			this.add(ANOTHER_TEST_HYDRA_SERVER_URL);
		}
	};

	private static String TEST_HYDRA_SERVER_URL = "http://localhost:8080";

	private static String ANOTHER_TEST_HYDRA_SERVER_URL = "http://localhost:8081";
	
	private static String TEST_APP_SERVER = "http://localhost:8080/app-server";

	private static String TEST_APP_SERVER_SECOND = "http://localhost:8080/app-server-second";
	
	private static String SERVICE_ID = "testAppId";
	
	private final static String APP_ROOT = "/app";
}
