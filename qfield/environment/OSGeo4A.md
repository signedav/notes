### What is builded
The opengisch/qfield-test-docker on docker hub what is the QGIS build used in the QField workflows for tests. It's **Build OSGeo4A**
The opengisch/qfield-sdk on docker hub to be able to create QField for Android. It's **Build QGIS**

### Test everything locally
Get the qt-ndk docker container

### Adding a library
Check out the lines in .github/workflows/docker.yml

```
sudo docker build --cache-from opengisch/qt-ndk:5.15.2 --build-arg QT_VERSION=5.15.2 -t qt-ndk .docker/qt-ndk
```
and then uncomment the last line with the big build in the Dockerfile and create local container
```
sudo docker build .
```
und dann
```
sudo docker run -d -it 3cc9397bda86 bash
```
Then you are in.

Now you can set the `export ARCH=<your arch>`

and then int the docker make the distribute stuff for qgis or the single libraries.
```
./distribute.sh -m libzstd

```

To add a library the reciep needs to be added and the dependencie set in the qgis recipe.


And then run the container of OSGeo4A: 


### Build with new QGIS:

1. osgeo4a uschecke (master)
2. scripts/update-qgis.sh `commit number of current QGIS`
3. git commit
4. git push
5. create osgeo4a release
6. wait for the build
7. make a qfield pr with travis updated to the tag of the osgeo4a release