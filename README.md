# CO2Hints - a question / suggestion service

CO2hints is a service which asks questions and present suggestions for reducing CO2 footprint based on the answers.

The entire focus of this service is inspire people on suggestions that they can do in order to reduce a persons CO2 footprint. The service will present the suggestions based on how easy it is to implement and how effective it is to reduce the CO2 footprint. The top suggestions should be the one most suitable for a person based on the answers the person has given.

The service is based on Spring boot and Elasticsearch.

The service is provided through a REST interface. An experimental java script
GUI is  also provided.

## Build

```bash
mvn clean install
```

## Package as docker image

```bash
mvn package docker:build
```

## Start

```bash
java -jar target/app.jar
```

## Start elesticsearch as a docker container

```bash
sudo sysctl -w vm.max_map_count=262144
docker run --rm -ti -p 9200:9200 -p 9300:9300 elasticsearch
```

## Setup elasticsearch

```bash
cd scripts
./setup-elasticsearch-index.sh
```

## Docker

You will find an image with the service on the following address on docker hub:
https://hub.docker.com/r/cycledev/co2hints/

## Try it out with docker

There's a [docker compose file](/src/main/docker/docker-compose.yml) file
which sets up both the service and elasticsearch.

The elasticsearch index needs to be set up once the two containers is up and
running.

```bash
sudo sysctl -w vm.max_map_count=262144
cd src/main/docker
docker-compose
```

Make sure that you are using docker-compose version 1.x or feel free to commit
a docker-compose file which works for version 2.x

Access the GUI on port 8080.

## License
[MIT](LICENSE)
