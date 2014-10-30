package io.github.innotech.hydra.client;

import io.github.innotech.hydra.client.exceptions.InaccessibleServer;
import io.github.innotech.hydra.client.exceptions.IncorrectServerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Make the http request to the hydra server for obtain the candidate servers.
 * This class has default scope because is used only by hydra client.
 */
class HydraRequester {

	public HydraRequester() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpConnectionParams.setSoTimeout(params, connectionTimeout);

		SchemeRegistry registry = new SchemeRegistry();

		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", buildSSLSocketFactory(), 443));

		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(params, registry);

		httpClient = new DefaultHttpClient(threadSafeClientConnManager, params);
	}

	/**
	 * Return the candidate url's of the servers sorted by the hydra active
	 * algorithm.
	 */
	public LinkedHashSet<String> getServicesById(String hydraServerUrl, String appId) throws InaccessibleServer,IncorrectServerResponse {
		try {
			return requestServers(hydraServerUrl, appId);
		} catch (IOException e) {
			throw new InaccessibleServer(e);
		}
	}

	void setConnectionTimeout(Integer timeout) {
		connectionTimeout = timeout;
	}

	private LinkedHashSet<String> requestServers(String hydraServerUrl, String appId) throws IOException,IncorrectServerResponse {
		HttpGet httpGet = new HttpGet(hydraServerUrl + "/" + appId);
		HttpResponse response = httpClient.execute(httpGet);
		
		try{
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new IncorrectServerResponse();
			}
			
			return parseJsonResponse(entityAsString(response));
			
		}finally{
			response.getEntity().consumeContent();
		}
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

	private SSLSocketFactory buildSSLSocketFactory() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory socketFactory = new DummySSLSocketFactory(trustStore);
			socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return socketFactory;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (KeyStoreException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (CertificateException e) {
			throw new IllegalStateException(e);
		} catch (KeyManagementException e) {
			throw new IllegalStateException(e);
		} catch (UnrecoverableKeyException e) {
			throw new IllegalStateException(e);
		}
	}
	/**
	 * Inner class implements a socket factory all certifies implements the trust manager check methods void. 
	 *
	 */
	class DummySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public DummySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
	
	private static final Integer DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 1000;

	private HttpClient httpClient;

	private ObjectMapper mapper = new ObjectMapper();

	private JavaType type = mapper.getTypeFactory().constructCollectionType(LinkedHashSet.class, String.class);

	private Integer connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;

}
