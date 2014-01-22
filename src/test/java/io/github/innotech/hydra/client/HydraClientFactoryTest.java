package io.github.innotech.hydra.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;

import java.util.LinkedHashSet;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.junit.After;
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
	
	@After
	public void reset(){
		HydraClientFactory.getInstance().reset();
	}
	
	@Test
	public void shouldReturnAnUniqueInstanceOfAFactory(){
		HydraClientFactory firstInstance =  HydraClientFactory.getInstance();
		HydraClientFactory secondInstance =  HydraClientFactory.getInstance();
		
		assertSame("The first instance must be equals to second instance",firstInstance,secondInstance);
	}

	@Test
	public void shouldGetHydraUniqueClient() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClient hydraClient =  HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		HydraClient otherHydraClient = HydraClientFactory.getInstance().hydraClient();
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("The second client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
	
	@Test
	public void shouldGetHydraUniqueClientWhenCallConfigManyTimes() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClient hydraClient = HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		HydraClient otherHydraClient =  HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		assertNotNull("Client must be not null",hydraClient);
		assertNotNull("The second client must be not null",otherHydraClient);
		assertSame("Client must be the same",hydraClient,otherHydraClient);
	}
	
	@Test
	public void shouldCallToRefreshHydraServerMethods() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);

		verify(hydraClient).reloadHydraServers();
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshHydraServersDefaultTimeOut() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		verify(timer).schedule(hydraServersMonitor, 0, TimeUnit.SECONDS.toMillis(60));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshHydraServersWithTimeOut() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().withHydraTimeOut(10l).config(TEST_HYDRA_SERVERS);
		
		verify(timer).schedule(hydraServersMonitor, 0, TimeUnit.SECONDS.toMillis(10));
	}

	@Test 
	public void shouldAddATimerJobForRefreshHydraServersAndTimeOut() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().andHydraTimeOut(10l).config(TEST_HYDRA_SERVERS);
		
		verify(timer).schedule(hydraServersMonitor, 0, TimeUnit.SECONDS.toMillis(10));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshAppServersDefaultTimeOut() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().config(TEST_HYDRA_SERVERS);
		
		verify(appTimer).schedule(hydraClientCacheMonitor, 0, TimeUnit.SECONDS.toMillis(20));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshAppServersWithTimeOut() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().withAppsTimeOut(90l).config(TEST_HYDRA_SERVERS);
		
		verify(appTimer).schedule(hydraClientCacheMonitor, 0, TimeUnit.SECONDS.toMillis(90));
	}
	
	@Test 
	public void shouldAddATimerJobForRefreshAppServersAndTimeOut() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().andAppsTimeOut(90l).config(TEST_HYDRA_SERVERS);
		
		verify(appTimer).schedule(hydraClientCacheMonitor, 0, TimeUnit.SECONDS.toMillis(90));
	}
	
	@Test 
	public void shouldAddNumberOfRetries() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().withNumberOfRetries(30).config(TEST_HYDRA_SERVERS);
		
		verify(hydraClient).setMaxNumberOfRetries(30);
		
	}
	
	@Test 
	public void shouldAddNumberOfRetriesAnd() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().andNumberOfRetries(30).config(TEST_HYDRA_SERVERS);
		
		verify(hydraClient).setMaxNumberOfRetries(30);
	}
	
	@Test
	public void shouldSetTheNumberOfMillisecondAllServerCallRetry() throws Exception{
		hydraClientFactoryTimersFixture();
		
		HydraClientFactory.getInstance().waitBetweenAllServersRetry(30).config(TEST_HYDRA_SERVERS);
		
		verify(hydraClient).setWaitBetweenAllServersRetry(30);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void shouldReturnIllegalArgumentExceptionWhenNullAppTimeOut() throws Exception{
		HydraClientFactory.getInstance().andAppsTimeOut(null);
	}
	
	@Test(expected=IllegalArgumentException.class) 
	public void shouldReturnIllegalArgumentExceptionWhenNullHydraTimeOut() throws Exception{
		HydraClientFactory.getInstance().andHydraTimeOut(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotCreateAClientWithNullSeedServer() throws Exception{
		HydraClientFactory.getInstance().config(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldNotCreateAClientWithoutSeedServer() throws Exception{
		HydraClientFactory.getInstance().config(new LinkedHashSet<String>());
	}
	
	private void hydraClientFactoryTimersFixture() throws Exception {
		PowerMockito.whenNew(Timer.class).withAnyArguments().thenReturn(timer,appTimer);
		PowerMockito.whenNew(HydraAppCacheMonitor.class).withAnyArguments().thenReturn(hydraClientCacheMonitor);
		PowerMockito.whenNew(HydraServersMonitor.class).withAnyArguments().thenReturn(hydraServersMonitor);
		PowerMockito.whenNew(HydraClient.class).withAnyArguments().thenReturn(hydraClient);
	}
}
