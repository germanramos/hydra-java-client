package io.github.innotech.hydra.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

//This class has default scope because is used only by hydra client.
class HydraServersRequester {

	private HttpClient httpClient = HttpClientBuilder.create().build();
	
	private ObjectMapper mapper = new ObjectMapper();

	private JavaType type = mapper.getTypeFactory().constructCollectionType(LinkedHashSet.class, String.class);
	
	public Set<String> getCandidateServers(String hydraServerUrl, String appId) {
		try {
			return requestServers(hydraServerUrl, appId);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> requestServers(String hydraServerUrl, String appId) throws IOException, ClientProtocolException {
		HttpGet httpGet = new HttpGet(hydraServerUrl + "/" + appId);
		HttpResponse response = httpClient.execute(httpGet);

		if (response.getStatusLine().getStatusCode() != 200) {
		}

		return parseJsonResponse(entityAsString(response));
	}

	private String entityAsString(HttpResponse response) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
		    builder.append(line).append("\n");
		}
		
		return builder.toString();
	}
	
	private Set<String> parseJsonResponse(String response) throws IOException{
		return mapper.readValue(response,type);
	}
}
