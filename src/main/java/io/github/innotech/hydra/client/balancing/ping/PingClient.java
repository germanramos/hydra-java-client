package io.github.innotech.hydra.client.balancing.ping;

import io.github.innotech.hydra.client.balancing.ping.exceptions.UnknownHostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingClient {

	private static final String PING_COMMAND = "ping -c 1 -w 1 ";

	private static final Double HIGH_LATENCY_OR_UNREACHABLE = 1000.0;
	
	private ExecutorService executor = Executors.newFixedThreadPool(3);

	public Future<Double> getLatency(final String host) {
		FutureTask<Double> futureTask = new FutureTask<Double>(
				new Callable<Double>() {

					@Override
					public Double call() throws IOException,UnknownHostException {
						return ping(host);
					}
				});

		executor.execute(futureTask);

		return futureTask;
	}

	private Double ping(String host) throws IOException,UnknownHostException {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(PING_COMMAND + host);
			process.waitFor();

			switch (process.exitValue()) {
				case 0:
					return processResult(process.getInputStream());
				case 1:
					return HIGH_LATENCY_OR_UNREACHABLE;
				default:
					throw new UnknownHostException();
			}
		} catch(InterruptedException e){
			throw new IllegalStateException("Thread interrupted.",e);
		} 
		
		finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	private Double processResult(InputStream stream) throws NumberFormatException,IOException {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));

		String result = null;
		Double latency = 0.0;
		Integer pingLines = 0;
		while ((result = buffer.readLine()) != null) {
			Pattern pattern = Pattern.compile(".*time=(\\d+\\.?\\d+).*");
			Matcher matcher = pattern.matcher(result);

			if (matcher.matches()) {
				String match = matcher.group(1);
				latency += Double.valueOf(match);
				pingLines++;
			}
		}
		buffer.close();
		return latency / pingLines;
	}
}
