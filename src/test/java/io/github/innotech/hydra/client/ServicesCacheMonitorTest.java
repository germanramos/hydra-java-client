package io.github.innotech.hydra.client;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServicesCacheMonitorTest {
	
	@Test
	public void shouldRefreshTheMongoCache(){
		ServicesCacheMonitor hydraClientCacheMonitor = new ServicesCacheMonitor(hydraClient);
		
		hydraClientCacheMonitor.run();
		
		verify(hydraClient).reloadServicesCache();
	} 

	@Mock
	private HydraClient hydraClient;
}
