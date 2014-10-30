package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import io.github.innotech.hydra.client.HydraRequester.DummySSLSocketFactory;
import io.github.innotech.hydra.client.exceptions.InaccessibleServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HydraRequester.class,DefaultHttpClient.class,KeyStore.class})
public class HydraRequesterTest {

	@Before
	public void configureHttpClient() throws Exception, KeyStoreException {
		PowerMockito.whenNew(DefaultHttpClient.class).withAnyArguments().thenReturn(defaultHttpClient);
		PowerMockito.whenNew(HttpGet.class).withArguments(TEST_HYDRA_SERVER + "/" + APP_ID).thenReturn(httpGet);
		PowerMockito.mockStatic(KeyStore.class);
		when(KeyStore.getInstance(KeyStore.getDefaultType())).thenReturn(keyStore);
		PowerMockito.whenNew(DummySSLSocketFactory.class).withAnyArguments().thenReturn(dummySSLSocketFactory);
	}
	
	@Test
	public void shouldReturnAListOfServers() throws Exception {
		when(defaultHttpClient.execute(httpGet)).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(200);

		when(httpResponse.getEntity()).thenReturn(httpEntity);
		when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(TEST_HYDRA_SERVERS.getBytes()));
		
		HydraRequester hydraServersRequester = new HydraRequester();
		Set<String> candidateServers = hydraServersRequester.getServicesById(TEST_HYDRA_SERVER, APP_ID);
		
		assertNotNull("The candidate servers must be not null",candidateServers);
		assertEquals("The number of elements must be the expected",3,candidateServers.size());
	}
	
	@Test(expected=InaccessibleServer.class)
	public void shouldFailWhenReturnTheServerList() throws Exception {
		when(defaultHttpClient.execute(httpGet)).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(400);
		when(httpResponse.getEntity()).thenReturn(httpEntity);
		
		HydraRequester hydraServersRequester = new HydraRequester();
		hydraServersRequester.getServicesById(TEST_HYDRA_SERVER, APP_ID);
	}
	
	@Test(expected=InaccessibleServer.class)
	public void shouldFailWhenExecuteTheRequest() throws Exception {
		when(defaultHttpClient.execute(httpGet)).thenThrow(new IOException());

		HydraRequester hydraServersRequester = new HydraRequester();
		hydraServersRequester.getServicesById(TEST_HYDRA_SERVER, APP_ID);
	}

	@Mock
	private HttpClient httpClient;
	
	@Mock
	private DefaultHttpClient defaultHttpClient;

	@Mock
	private HttpResponse httpResponse;

	@Mock
	private StatusLine statusLine;

	@Mock
	private HttpEntity httpEntity;

	@Mock
	private HttpGet httpGet;
	
	@Mock
	private KeyStore keyStore;
	
	@Mock
	private DummySSLSocketFactory dummySSLSocketFactory;
	
	private static String TEST_HYDRA_SERVER = "http://localhost:8080/hydra-server";
	
	private static String TEST_HYDRA_SERVERS = "[\"http://localhost:8080/hydra-server\",\"http://localhost:8080/hydra-server2\",\"http://localhost:8080/hydra-server3\"]";
	
	private static String APP_ID = "testAppId";

}
