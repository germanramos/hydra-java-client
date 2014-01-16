package io.github.innotech.hydra.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HydraServersRequester.class,HttpClientBuilder.class})
public class HydraServersRequesterTest {

	@Mock
	private CloseableHttpClient httpClient;

	@Mock
	private CloseableHttpResponse httpResponse;

	@Mock
	private StatusLine statusLine;

	@Mock
	private HttpEntity httpEntity;

	@Mock
	private HttpGet httpGet;
	
	@Mock
	private HttpClientBuilder httpClientBuilder;
	
	private static String TEST_HYDRA_SERVER = "http://localhost:8080/hydra-server";
	
	private static String TEST_HYDRA_SERVERS = "[\"http://localhost:8080/hydra-server\",\"http://localhost:8080/hydra-server2\",\"http://localhost:8080/hydra-server3\"]";
	
	private static String APP_ID = "testAppId";

	@Test
	public void shouldReturnAListOfServers() throws Exception {
		PowerMockito.whenNew(HttpGet.class).withArguments(TEST_HYDRA_SERVER + "/" + APP_ID).thenReturn(httpGet);
		PowerMockito.mockStatic(HttpClientBuilder.class);
		
		when(HttpClientBuilder.create()).thenReturn(httpClientBuilder);
		when(httpClientBuilder.build()).thenReturn(httpClient);
		
		when(httpClient.execute(httpGet)).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(200);

		when(httpResponse.getEntity()).thenReturn(httpEntity);
		when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(TEST_HYDRA_SERVERS.getBytes()));
		
		HydraServersRequester hydraServersRequester = new HydraServersRequester();
		Set<String> candidateServers = hydraServersRequester.getCandidateServers(TEST_HYDRA_SERVER, APP_ID);
		
		assertNotNull("The candidate servers must be not null",candidateServers);
		assertEquals("The number of elements must be the expected",3,candidateServers.size());
	}
}
