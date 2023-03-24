https://github.com/jleetutorial/dockerapp

### VMs vs Docker
Hypervisor-based Virtualization -> VMware, Virtual Box, AWS etc.
vs
Container-based Virtualization -> Docker

Only one kernel in Container Engine - Containers share the OS.

### Why Docker
- Runtime Isolation: 
Zbs. run different JREs on different Containers.
- Less CPU used:
more runnig at the same time
- Fast Deployment

### Containers Vs. Image
If an image is a class, then a container is an instance of a class - a runtime object 

DockerHub is a Registry where everybody can store the public images. The images are stored in the registry as repository.

### Magical info in german
Vom Dockerhub werden Images heruntergeladen und davon ein Container gestartet.
Oder man hat selbst alles in einem Ordner mit Dockerfile etc. (gleich wie Dockerhub), allerdings buildet man dort das Image erst und dann startet man einen Container davon.
Solange man den Container nicht absichtlich entfernt oder das ganze "rebuildet" (build parameter in docker-compose zBs. erstellt den Container neu), kann man den Container immer wieder stoppen und starten....

### Interaktive Container
` docker run -i -t busybox:1.24`
oder auch -it
`exit` um zu shut downen.

### Run in the background
In detached mode `run -d`

Auflisten der Dockers `docker ps` oder auch die geschlossenen `docker ps -a` aber nicht entfernten...

Oder mit `-rem` um den Docker entfernen wenn er geschlossen wird...

`--name` um einen Namen geben.

### Docker inspect
```
[david@localhost ~]$ docker run -d busybox:1.24 sleep 1000
a467327f9b538c1f28355eb8b5a47667dafc830c03a39d3377dc9a092223747a
[david@localhost ~]$ docker inspect a467327f9b538c1f28355eb8b5a47667dafc830c03a39d3377dc9a092223747a
```

### Map Ports
`docker run -it --rm -p 8888:8080 tomcat:8.0`
8888 containerport
8080 hostport

http://localhost:8888
  
### Change Docker image on Repository
Get image from the Docker-Hub.
Install something.

```
docker commit container_id repository_name
```
Repositoryname wäre signedav/debian:1.00 oder ähnlich. Muss noch einen Account erstellen.

### Dockerfile 
```
touch Dockerfile
vim Dockerfile
```
```
FROM debian:jessie #the base image <- these are instructions
RUN apt-get update #execute command
RUN apt-get install -y git
RUN apt-get install -y vim
```
Erstellen:
```
docker build -t signedav/debian .
```
oder 
```
docker build -t meindocker .
```
-t for the folowing tagname und following the path to the dockerfile

*checken ob erstellt:*
```
docker image ls
```
*Und starten so:*
```
docker run friendlyhello
```

### Restart single docker container from docker-compose
1. Change stuff in docker-compose
2. `docker-compose up -d --no-deps --build test_qgisserver`

### Kill everything
Irgendwie so:
```
docker system prune -a
```
Hab ich mal gemacht. 37.26 GB wurde frei - k.A. ob das gut ist

