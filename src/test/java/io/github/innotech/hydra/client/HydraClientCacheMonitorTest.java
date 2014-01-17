package io.github.innotech.hydra.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HydraClientCacheMonitorTest {

	@Mock
	private HydraClient hydraClient;
	
	@Test
	public void shouldRefreshTheMongoCache(){
		HydraClientCacheMonitor hydraClientCacheMonitor = new HydraClientCacheMonitor(hydraClient);
		
		hydraClientCacheMonitor.run();
		
		verify(hydraClient).invalidateAppCache();
	} 
}
