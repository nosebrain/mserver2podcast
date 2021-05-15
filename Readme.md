# MServer 2 Podcast

This project provides a service which generates a feed for Podcast apps (e.g. iTunes) from crawled meta data by [MServer](https://github.com/mediathekview/MServer).

## Usage

Subscribe to `http://localhost:8080/mserver2podcast/TOPIC` (replace `localhost:8080` with your installation)

`TOPIC` is equal to the `Thema` you can select in the GUI `MediathekView`.

e.g. for `Tatort`: `http://localhost:8080/mserver2podcast/Tatort`

## Download MServer files

Download the movie/show list using the provided bash script in `misc/download.sh`. Copy the script and setup a cron job:

```
4 4 * * * PATH_TO_SCRIPT/download.sh / PATH_FOLDER
```

## Setup Service

Build the service using Maven (`mvn clean install`). Deploy the war to Tomcat.

### Configuration

Create a file `$CATALINA_HOME/conf/mserver2podcast-service/mserver2podcast-service.properties` and add the path pointing to the mserver file to the properties file:

```
mserver.filePath = PATH_FOLDER/filmliste-act
```