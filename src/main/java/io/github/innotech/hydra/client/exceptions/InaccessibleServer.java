package io.github.innotech.hydra.client.exceptions;

import java.io.IOException;


/**
 * Checked exception occurs when hydra can access to a server. This can be occurs because the server 
 * don't answer of answer with an error.
 */
public class InaccessibleServer extends Exception{

	private static final long serialVersionUID = 4565799029211501184L;
	
	public InaccessibleServer(IOException e) {
		super(e);
	}

	public InaccessibleServer(){
	}
}
