package io.github.innotech.hydra.client.balancing;

import static org.junit.Assert.*;
import io.github.innotech.hydra.client.balancing.policies.BalancingPolicyExecutor;
import io.github.innotech.hydra.client.balancing.policies.DelegatedPolicyExecutor;
import io.github.innotech.hydra.client.balancing.policies.NearestPolicyExecutor;
import io.github.innotech.hydra.client.balancing.utils.ping.PingClient;

import java.util.LinkedHashSet;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BalancingPolicyExecutor.class)
public class BalancingPolicyExecutorTest {
	
	@InjectMocks
	private BalancingPolicyExecutor policyExecutor = new NearestPolicyExecutor();
	
	@Mock
	private PingClient pingClient;
	
	@Mock(name="futureServer1")
	Future<Double> futureServer1;
	
	@Mock(name="futureServer2")
	Future<Double> futureServer2;
	
	private static String TEST_APP_SERVER = "www.google.es";
	private static String TEST_APP_SERVER_SECOND = "localhost";
	
	LinkedHashSet <String> TEST_APP_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_APP_SERVER);
			this.add(TEST_APP_SERVER_SECOND);
		}
	};
	
	LinkedHashSet <String> NEAREST_APP_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(TEST_APP_SERVER_SECOND);
			this.add(TEST_APP_SERVER);
		}
	};
	
	@Test
	public void shouldReturnSameListWithDelegatedPolicy() throws Exception {
		BalancingPolicyExecutor policyExecutor = new DelegatedPolicyExecutor();
		
		LinkedHashSet <String> balancedServers = policyExecutor.balance(TEST_APP_SERVERS);
		
		assertNotNull("Balanced servers set should not be null", balancedServers);
		assertSame("Input and output sets should be the same and on the same order", TEST_APP_SERVERS, balancedServers);
	}
	
	@Test
	public void shouldReturnOrderedListByLatencyWithNearestPolicy() throws Exception {
		PowerMockito.whenNew(PingClient.class).withNoArguments().thenReturn(pingClient);
		when(pingClient.getLatency(Mockito.anyString())).thenReturn(futureServer1).thenReturn(futureServer2);
		when(futureServer1.get()).thenReturn(0.5);
		when(futureServer2.get()).thenReturn(1.0);
		
		LinkedHashSet <String> balancedServers = policyExecutor.balance(TEST_APP_SERVERS);
		
		assertNotNull("Balanced servers set should not be null", balancedServers);
		assertEquals("Input and output sets should be the same and on the same order", NEAREST_APP_SERVERS, balancedServers);
	}

}
