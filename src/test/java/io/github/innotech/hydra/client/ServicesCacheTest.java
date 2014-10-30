package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServicesCacheTest {

	@Test
	public void shouldReturnTheServicesStoredByAnId(){
		servicesCache.putService(SERVICE_ID, TEST_SERVICES);
		
		LinkedHashSet<String> services = servicesCache.findById(SERVICE_ID);
		
		assertNotNull("The returned services must be not null",services);
		assertEquals("The returned services must be the expected",TEST_SERVICES,services);
	}
	
	@Test
	public void shouldReturnAnEmptyListIfServiceDontExists(){
		LinkedHashSet<String> services = servicesCache.findById(SERVICE_ID);
		
		assertNotNull("The returned services must be not null",services);
		assertEquals("The returned services must be the expected",new LinkedHashSet<String>(),services);
	}
	
	@Test
	public void shouldReturnTheTrueServicesWhenExist(){
		servicesCache.putService(SERVICE_ID, TEST_SERVICES);
		
		assertTrue ("Return true when service exists",servicesCache.exists(SERVICE_ID));
	}
	
	@Test
	public void shouldReturnTheServiceIdsStored(){
		servicesCache.putService(SERVICE_ID, TEST_SERVICES);
		
		Set<String> serviceIds = servicesCache.getIds();
		
		assertNotNull("The returned services must be not null",serviceIds);
		assertTrue("The returned services must be the expected",serviceIds.contains(SERVICE_ID));
	}
	
	@Test
	public void shouldRefreshWholeCache(){
		Map<String,LinkedHashSet<String>> services = new HashMap<String, LinkedHashSet<String>>();
		services.put(SERVICE_ID, TEST_SERVICES);
		
		servicesCache.refresh(services);
		
		assertNotNull("The returned services must be not null",servicesCache.findById(SERVICE_ID));
		assertEquals("The returned services must be the expected",TEST_SERVICES,servicesCache.findById(SERVICE_ID));
	}
	
	@InjectMocks
	private ServicesCache servicesCache;
	
	private final static String SERVICE_ID = "service";
	
	private LinkedHashSet<String> TEST_SERVICES = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_APP_SERVER);
			this.add(TEST_APP_SERVER_SECOND);
		}
	};
	
	private static String TEST_APP_SERVER = "http://localhost:8080/app-server";

	private static String TEST_APP_SERVER_SECOND = "http://localhost:8080/app-server-second";

}
