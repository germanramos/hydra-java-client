package io.github.innotech.hydra.client;

import io.github.innotech.hydra.client.exceptions.InaccessibleServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Make the http request to the hydra server for obtain the candidate servers.
 * This class has default scope because is used only by hydra client.
 */
class HydraServersRequester {

	private HttpClient httpClient;

	private ObjectMapper mapper = new ObjectMapper();

	private JavaType type = mapper.getTypeFactory().constructCollectionType(LinkedHashSet.class, String.class);

	public HydraServersRequester() {
		HttpParams params = new BasicHttpParams();
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		
		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(params, registry);
		httpClient = new DefaultHttpClient(threadSafeClientConnManager, params);
	}

	/**
	 * Return the candidate url's of the servers sorted by the hydra active
	 * algorithm.
	 */
	public LinkedHashSet<String> getCandidateServers(String hydraServerUrl, String appId) throws InaccessibleServer {
		try {
			return requestServers(hydraServerUrl, appId);
		} catch (IOException e) {
			throw new InaccessibleServer(e);
		}
	}

	private LinkedHashSet<String> requestServers(String hydraServerUrl, String appId) throws IOException,
			InaccessibleServer {
		HttpGet httpGet = new HttpGet(hydraServerUrl + "/" + appId);
		HttpResponse response = httpClient.execute(httpGet);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new InaccessibleServer();
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

	private LinkedHashSet<String> parseJsonResponse(String response) throws IOException {
		return mapper.readValue(response, type);
	}
}
