package io.github.innotech.hydra.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import io.github.innotech.hydra.client.balancing.policies.BalancingPolicyExecutor;

import java.util.LinkedHashSet;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HydraClientFactory.class)
public class HydraClientFactoryTest {

	private static final String SEED_SERVER = "http://localhost:8080";

	LinkedHashSet <String> TEST_HYDRA_SERVERS = new LinkedHashSet<String>() {

		private static final long serialVersionUID = 1L;

		{
			this.add(SEED_SERVER);
		}
	};
	
	@Mock
	private HydraClient hydraClient;
	
	@Mock(name="libertyTimer")
	private Timer timer;
	
	@Mock(name="appTimer")
	private Timer appTimer;
	
	@Mock
	private HydraAppCacheMonitor hydraClientCacheMonitor;
	
	@Mock
	private HydraServersMonitor hydraServersMonitor;
	
	@Mock
	private BalancingPolicyExecutor policyExecutor;
	
	@After
	public void resetMock(){
		HydraClientFactory.reset();
	}

	@Before
	public void before() throws Exception{
		hydraClientFactoryTimersFixture();
	}
	
	@Test
	public void shouldSetTheClientSideBalancingPolicyWithMethod() throws Exception {
		HydraClientFactory.config(TEST_HYDRA_SERVERS).withBalancingPolicy(policyExecutor).build();

		verify(hydraClient).setBalancingPolicy(policyExecutor);
	}
	
	@Test
	public void shouldSetTheClientSideBalancingPolicyAndMethod() throws Exception {
		HydraClientFactory.config(TEST_HYDRA_SERVERS).andBalancingPolicy(policyExecutor).build();

		verify(hydraClient).setBalancingPolicy(policyExecutor);
	}
	
	@Test
	public void shouldGetHydraUniqueClient() throws Exception{
		HydraClient hydraClient =  HydraClientFactory.config(TEST_HYDRA_SERVERS).build();
		HydraClient otherHydraClient = HydraClientFactory.hydraClient();
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("The second client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
	
	@Test
	public void shouldGetHydraUniqueClientWhenCallConfigManyTimes() throws Exception{
		HydraClient hydraClient = HydraClientFactory.config(TEST_HYDRA_SERVERS).build();
		HydraClient otherHydraClient =  HydraClientFactory.config(TEST_HYDRA_SERVERS).build();
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("The second client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
	
	@Test
	public void shouldCallToRefreshHydraServerMethods() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).build();

		verify(hydraClient).reloadHydraServers();
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshHydraServersDefaultTimeOut() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).build();
		
		verify(timer).schedule(hydraServersMonitor, 0, TimeUnit.SECONDS.toMillis(60));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshHydraServersWithTimeOut() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).withHydraCacheRefreshTime(10l).build();
		
		verify(timer).schedule(hydraServersMonitor, 0, TimeUnit.SECONDS.toMillis(10));
	}

	@Test 
	public void shouldAddATimerJobForRefreshHydraServersAndTimeOut() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).andHydraRefreshTime(10l).build();
		
		verify(timer).schedule(hydraServersMonitor, 0, TimeUnit.SECONDS.toMillis(10));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshAppServersDefaultTimeOut() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.config(TEST_HYDRA_SERVERS).build();
		
		verify(appTimer).schedule(hydraClientCacheMonitor, 0, TimeUnit.SECONDS.toMillis(20));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshAppServersWithTimeOut() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).withAppsCacheRefreshTime(90l).build();
		
		verify(appTimer).schedule(hydraClientCacheMonitor, 0, TimeUnit.SECONDS.toMillis(90));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshAppServersAndTimeOut() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).andAppsCacheRefreshTime(90l).build();
		
		verify(appTimer).schedule(hydraClientCacheMonitor, 0, TimeUnit.SECONDS.toMillis(90));
	}
	
	@Test 
	public void shouldAddNumberOfRetries() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).withNumberOfRetries(30).build();
		
		verify(hydraClient).setMaxNumberOfRetries(30);
		
	}
	
	@Test 
	public void shouldAddNumberOfRetriesAnd() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).andNumberOfRetries(30).build();
		
		verify(hydraClient).setMaxNumberOfRetries(30);
	}
	
	@Test
	public void shouldSetTheNumberOfMillisecondAllServerCallRetry() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).waitBetweenAllServersRetry(30).build();
		
		verify(hydraClient).setWaitBetweenAllServersRetry(30);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void shouldReturnIllegalArgumentExceptionWhenNullAppTimeOut() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).andAppsCacheRefreshTime(null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void shouldReturnIllegalArgumentExceptionWhenNullHydraTimeOut() throws Exception{
		HydraClientFactory.config(TEST_HYDRA_SERVERS).andHydraRefreshTime(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotCreateAClientWithNullSeedServer() throws Exception{
		HydraClientFactory.config(null).build();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotCreateAClientWithoutSeedServer() throws Exception{
		HydraClientFactory.config(new LinkedHashSet<String>()).build();
	}
	
	private void hydraClientFactoryTimersFixture() throws Exception {
		PowerMockito.whenNew(Timer.class).withAnyArguments().thenReturn(timer,appTimer);
		PowerMockito.whenNew(HydraAppCacheMonitor.class).withAnyArguments().thenReturn(hydraClientCacheMonitor);
		PowerMockito.whenNew(HydraServersMonitor.class).withAnyArguments().thenReturn(hydraServersMonitor);
		PowerMockito.whenNew(HydraClient.class).withAnyArguments().thenReturn(hydraClient);
	}
}
