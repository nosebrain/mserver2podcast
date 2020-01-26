# MServer 2 Podcast

This project provides a service which generates a feed for Podcast apps (e.g. iTunes) from crawled meta data by [MServer](https://github.com/mediathekview/MServer).

## Usage

Subscribe to `http://localhost:8080/mserver2podcast/TOPIC` (replace `localhost:8080` with your installation)

`TOPIC` is equal to the `Thema` you can select in the GUI `MediathekView`.

e.g. for `Tatort`: `http://localhost:8080/mserver2podcast/Tatort`


## Setup MServer

Follow the instructions on [MServer](https://github.com/mediathekview/MServer). Add a cronjob to run MServer on a daily basis:

```
4 4 * * * cd /$PATH_TO_MSERVER/MServer && ./gradlew run
```

## Setup Service

Build the service using Maven (`mvn clean install`). Deploy the war to Tomcat.

### Configuration

Create a file `$CATALINA_HOME/conf/mserver2podcast-service/mserver2podcast-service.properties` and add the path pointing to the mserver cache to the properties file:

```
mserver.basePath = /$PATH_TO_USER_HOME_RUNNING_MSERVER/.mserver/filmlisten
```