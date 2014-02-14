package io.github.innotech.hydra.client.balancing.utils.ping;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PingClient.class)
public class PingClientTest {

	PingClient pingClient = new PingClient();

	@Test
	public void shouldReturnLowLatency() throws InterruptedException, ExecutionException, IOException {
		String host = "localhost";
		Double latency = pingClient.getLatency(host).get();

		Assert.assertTrue("Latency should be inferior to 1 ms", latency < 1.0);
	}

	@Test
	public void shouldReturnHighLatency() throws InterruptedException, ExecutionException, IOException {
		String host = "www.ull.es";
		Double latency = pingClient.getLatency(host).get();

		Assert.assertTrue("Latency should be equal to 1000 ms", latency == 1000.0);
	}

	@Test(expected = ExecutionException.class)
	public void shouldThrowExecutionException() throws InterruptedException, ExecutionException  {
		String host = "unknowhost";
		pingClient.getLatency(host).get();
	}

}
