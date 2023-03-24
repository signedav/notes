> Chwäiss dänk scho isch komplett unterschiedlich, trotzem no da zämegfasst...

### What is a Dockerfile:
Called `Dockerfile`
Looks like this:
```
FROM ubuntu:bionic
MAINTAINER David Signer

RUN apt update \
	&& yes | apt install nodejs
```
See my own here: https://github.com/signedav/docker-gitbook
-> can then be published on dockerhub etc.

`docker build -t "Dockerfile" .`


### What is a Docker-Compose File:
Called `docker-compose.yml`

To deploy it.
Is to start the docker defined in the Dockerfile

> Remember, docker-compose.yml files are used for defining and running multi-container Docker applications, whereas Dockerfiles are simple text files that contain the commands to assemble an image that will be used to deploy containers. ... Deploy the entire stack with the docker compose command.

### Other infos
Recreate single docker container from docker-compose

Change stuff in docker-compose
```
docker-compose up -d --no-deps --build test_qgisserver
```
Attention: depending images are built as well.

And first remove it with `docker remove`

Otherwise I guess it's just `docker stop -` and `- start containter`

