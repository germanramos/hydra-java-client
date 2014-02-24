package io.github.innotech.hydra.client.balancing.ping;

import static org.mockito.Mockito.when;

import io.github.innotech.hydra.client.balancing.ping.PingClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PingClient.class, Runtime.class })
public class PingClientTest {

	private static final String TEST_HOST = "host";
	private static final String DECIMAL_LATENCY = "64 bytes from mad01s14-in-f24.1e100.net (173.194.41.24): icmp_req=1 ttl=56 time=39.1 ms";
	private static final String INTEGER_LATENCY = "64 bytes from mad01s14-in-f24.1e100.net (173.194.41.24): icmp_req=1 ttl=56 time=250 ms";

	@InjectMocks
	private PingClient pingClient = new PingClient();

	@Mock
	private Runtime runtime;

	@Mock
	private Process process;

	@Mock
	private InputStream inputStream;

	@Mock
	private InputStreamReader streamReader;

	@Mock
	private BufferedReader buffer;

	@Before
	public void setup() throws Exception {
		PowerMockito.mockStatic(Runtime.class);
		when(Runtime.getRuntime()).thenReturn(runtime);
		when(runtime.exec(Mockito.anyString())).thenReturn(process);
		when(process.waitFor()).thenReturn(0);
		when(process.getInputStream()).thenReturn(inputStream);
		PowerMockito.whenNew(InputStreamReader.class).withArguments(inputStream).thenReturn(streamReader);
		PowerMockito.whenNew(BufferedReader.class).withArguments(streamReader).thenReturn(buffer);
	}

	@Test
	public void shouldSucceedAndReturnDecimalLatency() throws Exception {
		when(process.exitValue()).thenReturn(0);
		when(buffer.readLine()).thenReturn(DECIMAL_LATENCY).thenReturn(null);

		Double latency = pingClient.getLatency(TEST_HOST).get();

		Assert.assertTrue("Latency should be equal to 39.1 ms", latency == 39.1);
	}

	@Test
	public void shouldSucceedAndReturnIntegerLatency() throws Exception {
		when(process.exitValue()).thenReturn(0);
		when(buffer.readLine()).thenReturn(INTEGER_LATENCY).thenReturn(null);

		Double latency = pingClient.getLatency(TEST_HOST).get();

		Assert.assertTrue("Latency should be equal to 250.0 ms", latency == 250.0);
	}

	@Test
	public void shouldNotReachServerAndReturnHighLatency() throws Exception {
		when(process.exitValue()).thenReturn(1);

		Double latency = pingClient.getLatency(TEST_HOST).get();

		Assert.assertTrue("Latency should be equal to 1000.0 ms", latency == 1000.0);
	}

	@Test(expected = ExecutionException.class)
	public void shouldThrowExecutionException() throws InterruptedException, ExecutionException {
		when(process.exitValue()).thenReturn(2);

		pingClient.getLatency(TEST_HOST).get();
	}

}
