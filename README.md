#hydra-java-client

Client of Hydra development in java. Hydra is a multi-cloud broker system.Provides a multi-cloud application discovery, management and balancing service. Hydra attempts to ease the routing and balancing burden from servers and delegate it on the client 

For a complete information about the project visit http://innotech.github.io/hydra/.

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
    HydraClient hydraClient = HydraClientFactory.config(hydraServerUrls).build();
    LinkedHashSet<String> candidateServers = hydraClient.get(applicationId);
  
    //Some network call using the first of the candidate servers.

```

The config method take only one parameter the hydraServerUrls, this is a LinkedHashSet of String contains the initial urls where hydra client search the hydra server. Once the server is discovered the client automatically refresh the list of the available serves.

```
    LinkedHashSet<String> hydraServerUrls = new LinkedHashSet<String>();
    hydraServerUrls.add("http://localhost:8080");
```

The previous code fragment configure the client to search hydra server in localhost.

In this case the rest of config parameters are configured using the following default values.

###Configuration parameters
Name | Default value | Description 
:---  | :--- | :---
AppsCacheRefreshTime | 60 seconds | The time period that the cache that store the candidate servers for applications is invalidated.
HydraCacheRefreshTime| 20 seconds | The time period that the cache that store the hydra servers is invalidated.
NumberOfRetries| 10 | The client try this number of times to connect to all the register hydra servers.
WaitBetweenAllServersRetry| 300 milliseconds | The time between all hydra servers are tried and the next retry.

To obtain the client after the configuration:

```
    HydraClient hydraClient = HydraClientFactory.hydraClient();
    LinkedHashSet<String> candidateServers = hydraClient.get(applicationId);
  
    //Some network call using the first of the candidate servers.
```

###Hydra cache

The Hydra client, in order to reduce the network traffic and improve the overall system performance, cache two resources:

+ Hydra servers, the list of available hydra server.
+ Application servers, the list of current server available for an application id.

You can shortcut the available applications server cache when use hydra client:

```
    LinkedHashSet<String> candidateServers = hydraClient.get(applicationId,true);
  
    //Some network call using the first of the candidate servers.
```

This call shortcut the application servers internal cache, requests the candidate servers and refresh the internal cache.

##Advanced configuration

You can cache all the default configuration values using the following methods.

###App cache refresh time

```
    HydraClient hydraClient = HydraClientFactory.
            config(hydraServerUrls).
            withAppsCacheRefreshTime(10l).
        build();
```

###Hydra servers cache refresh time

```
    HydraClient hydraClient = HydraClientFactory.
            config(hydraServerUrls).
            withHydraCacheRefreshTime(90l).
        build();
```

###Hydra servers number of retries

```
    HydraClient hydraClient = HydraClientFactory.
            config(hydraServerUrls).
            withNumberOfRetries(3).
        build();
```

###Wait times between retries

```
    HydraClient hydraClient = HydraClientFactory.
            config(hydraServerUrls).
            waitBetweenAllServersRetry(300).
        build();
```

###A complex configuration example

You can change all the parameters when configure the client.

```
    HydraClient hydraClient = HydraClientFactory.
            config(hydraServerUrls).
            withAppsTimeOut(10l).
            andHydraTimeOut(90l).
            andNumberOfRetries(3).
            waitBetweenAllServersRetry(300).
        build();
```
