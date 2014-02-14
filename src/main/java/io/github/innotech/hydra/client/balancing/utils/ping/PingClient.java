package io.github.innotech.hydra.client.balancing.utils.ping;

import io.github.innotech.hydra.client.balancing.utils.ping.exception.HostUnreachableException;

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

	private ExecutorService executor = Executors.newFixedThreadPool(3);

	public Future<Double> getLatency(final String host) {
		FutureTask<Double> futureTask = new FutureTask<Double>(
				new Callable<Double>() {

					@Override
					public Double call() throws HostUnreachableException {
						return ping(host);
					}
				});

		executor.execute(futureTask);

		return futureTask;
	}

	Double ping(String host) throws HostUnreachableException {
		try {
			String command = "ping -c 1 " + host; // + " | grep \"time=\" | cut -d\" \" -f8 | cut -d\"=\" -f2";
			
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			
			if (process.exitValue() == 0) {
				return processResult(process.getInputStream());
			}
			throw new HostUnreachableException();
		} catch (Exception e) {
			throw new HostUnreachableException();
		}
	}
	
	Double processResult(InputStream stream) throws NumberFormatException, IOException {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
		buffer.readLine();
		String result = buffer.readLine();
		
		Pattern pattern = Pattern.compile(".*time=(\\d+\\.\\d+).*");
		Matcher matcher = pattern.matcher(result);
		matcher.matches();
		String match = matcher.group(1);
		
		return Double.valueOf(match);
		
	}

}
