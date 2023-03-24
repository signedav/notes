```
ARCH=arm64-v8a
QFIELD_SDK_VERSION=20200828

sudo docker run -it -v $(pwd):/usr/src/qfield -e "BUILD_FOLDER=build-${ARCH}" -e ARCH -e STOREPASS -e KEYNAME -e KEYPASS -e PKG_NAME -e APP_NAME -e APP_ICON -e "APP_VERSION=1.6.0" -e "APP_VERSION_CODE=161" -e APP_VERSION_STR opengisch/qfield-sdk:${QFIELD_SDK_VERSION} /usr/src/qfield/scripts/docker-build.sh

> Android package built successfully in 61.482 ms.
>   -- File: /usr/src/qfield/build-armeabi-v7a/android-build//build/outputs/apk/debug/android-build-debug.apk

find . -type f -name \*.apk

adb install ./build-arm64-v8a/android-build/build/outputs/apk/debug/android-build-debug.apk

```

or bump a new QGIS:
```
~/dev/opengisch/OSGeo4A$ git checkout -b qgisupdate

~/dev/opengisch/OSGeo4A$ ./scripts/update_qgis.sh b6a9cced10406f653fa5767d0674aef2adc94efe
```
Commit aus dem QGIS.
Dann pushen und auf GH in Actions ist es schon für den Branch ein osgeo4a am generieren und den docker am hochladen.
Dieses kann man dann testen indem man als QFIELD_SDK_VERSION den branchname angibt.




