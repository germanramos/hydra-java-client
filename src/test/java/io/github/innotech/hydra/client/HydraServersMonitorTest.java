package io.github.innotech.hydra.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HydraServersMonitorTest {

	@Mock
	private HydraClient hydraClient;
	
	@Test
	public void shouldRefreshTheMongoCache(){
		HydraServersMonitor hydraServersMonitor = new HydraServersMonitor(hydraClient);
		
		hydraServersMonitor.run();
		
		verify(hydraClient).reloadHydraServers();
	} 
}
