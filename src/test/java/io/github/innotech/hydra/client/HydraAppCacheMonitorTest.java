package io.github.innotech.hydra.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HydraAppCacheMonitorTest {

	@Mock
	private HydraClient hydraClient;
	
	@Test
	public void shouldRefreshTheMongoCache(){
		HydraAppCacheMonitor hydraClientCacheMonitor = new HydraAppCacheMonitor(hydraClient);
		
		hydraClientCacheMonitor.run();
		
		verify(hydraClient).reloadApplicationCache();
	} 
}
