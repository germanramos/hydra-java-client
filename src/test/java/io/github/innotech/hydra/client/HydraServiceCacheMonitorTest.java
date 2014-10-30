package io.github.innotech.hydra.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HydraServiceCacheMonitorTest {

	@Mock
	private HydraClient hydraClient;
	
	@Test
	public void shouldRefreshTheMongoCache(){
		HydraServiceCacheMonitor hydraServersMonitor = new HydraServiceCacheMonitor(hydraClient);
		
		hydraServersMonitor.run();
		
		verify(hydraClient).initHydraService();
	} 
}
