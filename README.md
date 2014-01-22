#hydra-java-client

Client of Hydra development in java.

##Obtain client

###Maven

```
    <dependency>
       <groupId>io.github.innotech.hydra</groupId>
       <artifactId>client</artifactId>
       <version>0.0.1</version>
    </dependency>
```

###From source code 

```
    git clone https://github.com/innotech/hydra-java-client.git
    mvn clean install
```

Take the generated jar in target directory.

##Hydra client basic usage

The basic way to connect to hydra using the java client is:

```
    HydraClient hydraClient = HydraClientFactory.getIntance().config(hydraServerSeed);
    LinkedHashSet<String> candidateServers = hydraClient.get(applicationId);
  
    //Some network call using the first of the candidate servers.

```

To obtain the client after the configuration:

```
    HydraClient hydraClient = HydraClientFactory.getIntance().hydraClient();
    LinkedHashSet<String> candidateServers = hydraClient.get(applicationId);
  
    //Some network call using the first of the candidate servers.
```

###Hydra cache

The Hydra client cache ...

```
    LinkedHashSet<String> candidateServers = hydraClient.get(applicationId,true);
  
    //Some network call using the first of the candidate servers.
```

This call shortcut the internal cache, requests the candidate servers and refresh the internal cache.

##Advanced configuration

###App cache refresh time

The time is expressed seconds.

```
    HydraClient hydraClient = HydraClientFactory.getIntance().
            withAppsTimeOut(10l).
        config(hydraServerSeed);
```

###Hydra servers cache refresh time

The time is expressed in seconds.

```
    HydraClient hydraClient = HydraClientFactory.getIntance().
            config(hydraServerSeed).
            withHydraTimeOut(90l).
        build();
```

###Hydra servers number of retries

```
    HydraClient hydraClient = HydraClientFactory.getIntance().
            config(hydraServerSeed).
            withNumberOfRetries(3).
        build();
```

###Wait times between retries

The time is expressed in milliseconds

```
    HydraClient hydraClient = HydraClientFactory.getIntance().
            config(hydraServerSeed).
            waitBetweenAllServersRetry(300).
        build();
```

###A complex configuration example

```
    HydraClient hydraClient = HydraClientFactory.getIntance().
            config(hydraServerSeed).
            withAppsTimeOut(10l).
            andHydraTimeOut(90l).
            andNumberOfRetries(3).
            waitBetweenAllServersRetry(300).
        build();
```
