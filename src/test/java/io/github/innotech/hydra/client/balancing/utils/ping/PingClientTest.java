package io.github.innotech.hydra.client.balancing.utils.ping;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PingClient.class)
public class PingClientTest {
	
	@InjectMocks
	PingClient pingClient = new PingClient();
	
	@Mock
	Runtime runtime;
	
	@Mock
	Process process;
	
	@Test(timeout=200)
	public void shouldReturnLowLatency() throws InterruptedException, ExecutionException, IOException{
		String host = "localhost";
		pingClient.getLatency(host).get();
	}

}
