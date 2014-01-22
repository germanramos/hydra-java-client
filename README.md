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
  HydraClient hydraClient = HydraClientFactory.getIntance().config();
  
  LinkedHashSet<String> candidateServers = hydraClient.get(applicationId);
  
  //Some network call using the first of the candidate servers.

```

