# Setup developer environment including Android building stuff

## Java

(Oracle is shitty -take OpenJDK)
- I took JDK 11

**WRONG DAVE - YOU NEED 8 - >9 FUNKTIONIERT NICHT - MACHS NOCHMAL!!!**

## OSGeo4A
I copied it from the latest `opengisch/qfield-sdk` docker:
```
    docker run opengisch/qfield-sdk:latest bash
    docker ps -a # CONTAINER ID container on top
    docker cp [CONTAINER ID]:/home/osgeo4a /home/david/dev/osgeo4a
```

## Android SDK and NDK
### Android Studio (SDK)
Got it here https://developer.android.com/studio
Unzip it into the /opt directory.
```
sudo unzip android-studio-ide-*-linux.zip -d /opt/
```
You should now have your android-studio directory unzipped in the /opt directory.
To run android studio, go to the bin directory in the unzipped android studio directory and run the
```
studio.sh file:
cd /opt/android-studio/bin
./studio.sh
```
It should run fine, however close the launched application—do not proceed with the setup just yet.  You can symlink the studio.sh file to the /bin directory, so you can simply run android studio from any directory on the commandline.
You can do that with the command below:
```
sudo ln -sf /opt/android-studio/bin/studio.sh /bin/android-studio
```
Then I can run it just with `android-studio`

Cite: <cite>https://linuxhint.com/install_android_studio_ubuntu1804/</cite>


### Android Studio (NDK)
Got it here https://www.crystax.net/en/download
```
tar -xf crystax-ndk-10.3.2-linux-x86_64.tar.xz /opt/
```
Or unpack and copy there...

## Qt and QtCreator
### QT 5.11.1
To install the QT I downloaded the "run" file of online installer from official QT page. I had to set it to executable `chmod ugo+rwx qt-unified-linux-x64-3.0.6-online.run ` and then I ran `./qt...run`
Fine. I had it then in the folder `home/david/Qt/5.11.3/` (Login with david@opengis.ch)

## Config Qt Creator

Tools->Options->Build & Run schauen ob Kits für Android vorhanden:

*Android for armeabi-v7a...* etc.

Tools->Options->Devices:

JDK location: `/usr/lib/jvm/java-1.8.0-openjdk`

Android SDK location: `/home/david/Android/Sdk`

Android NDK location: `/home/david/Android/crystax-ndk-10.3.2`


# More Info
**Kuhns Vorgaben**
* x86 (anstatt arm)

* im BIOS Virtualisierig aktiviere (falls /dev/kvm nöd existiert)

* neuers Gradle bruche (siehe Pull https://github.com/opengisch/QField/pull/252)

* Zum direkt im QtCreator z'kompiliere

  Build chain installiere (android sdk / android ndk /qt 5.9.4)

  OSGeo4A libs direkt us em docker kopiere:
```
    docker run opengisch/qfield-sdk:20190123 bash
    docker ps -a # CONTAINER ID vom oberste Container
    docker cp [CONTAINER ID]:/home/osgeo4a /home/david/dev/osgeo4a
```

PS. Qt hab ich auch so kopiert:
```
docker cp 425aeba35544:/opt/Qt/5.11.1 /home/david/Qt/5.11.1
```

**Installation**

Man braucht
- Qt (habe ich über den Webinstaller in meinem home dir installiert) habe dort einen Folder namens Qt. Im Subfolder Tools/QtCreator/bin liegt dann auch 
- qtcreator
- Android SDK (das wo man halt findet)
- Android NDK (wobei hier das crystax verwendet werden kann)
- JDK (Hatte ich zuerst Oracle Java 10 aber nicht sicher ob das geht. Später mit 1.8.0 openjdk gehts)

android studio like this: https://developer.android.com/studio/install

Und QT Creator mit QT 5.9.4 und von dort aus starten:
`Qt/Tools/QtCreator/bin/qtcreator`...


**Einstellung in QtCreator:**

Tools->Options->Build & Run schauen ob Kits für Android vorhanden:

*Android for armeabi-v7a...* etc.

Tools->Options->Devices:

JDK location: `/usr/lib/jvm/java-1.8.0-openjdk`

Android SDK location: `/home/david/Android/Sdk`

Android NDK location: `/home/david/Android/crystax-ndk-10.3.2`



**Zusätzliche Info:**

(Java path findet man so raus:)
```
[david@localhost ~]$  which javac
/usr/bin/javac
cd /usr/bin/javac
```
und
```
[david@localhost bin]$ ls -l javac
lrwxrwxrwx. 1 root root 23 Mar 26 17:48 javac -> /etc/alternatives/javac
[david@localhost bin]$ ls -l  /etc/alternatives/javac
lrwxrwxrwx. 1 root root 26 Mar 26 17:48 /etc/alternatives/javac -> /usr/java/jdk-10/bin/javac
[david@localhost bin]$ ls -l /usr/java/jdk-10/bin/javac
-rwxr-xr-x. 1 root root 9906 Mar  8 03:17 /usr/java/jdk-10/bin/javac
```
(Version so)
```
java -version
```

**Problem beim erstellen eines Devices**
```
Exception in thread "main" java.lang.NoClassDefFoundError: javax/xml/bind/annotation/XmlSchema
	at com.android.repository.api.SchemaModule$SchemaModuleVersion.<init>(SchemaModule.java:156)
	at com.android.repository.api.SchemaModule.<init>(SchemaModule.java:75)
	at com.android.sdklib.repository.AndroidSdkHandler.<clinit>(AndroidSdkHandler.java:81)
	at com.android.sdklib.tool.AvdManagerCli.run(AvdManagerCli.java:213)
	at com.android.sdklib.tool.AvdManagerCli.main(AvdManagerCli.java:200)
Caused by: java.lang.ClassNotFoundException: javax.xml.bind.annotation.XmlSchema
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:582)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:190)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:499)
	... 5 more
```
Lösung:
Ich hatte die Java Version 10 eingestellt:
`java -version`
Dann habe ich aber umgeschaltet auf 1.8... von openJDK mit dem Command:
`sudo update-alternatives --config java`

**Problem, dass es QGIS Klassen nicht gefunden hat**
```
In file included from ../../QFieldWork_camfix01/src/androidplatformutilities.h:22:0,
                 from ../../QFieldWork_camfix01/src/androidplatformutilities.cpp:19:
../../QFieldWork_camfix01/src/platformutilities.h:23:22: fatal error: qgsfield.h: No such file or directory
 #include <qgsfield.h>
```
Lösung:
im config.pri muss der Pfad zum osgeo4a richtig sein...
entweder /home/osgeo4a oder /home/david/dev/osgeo4a oder whatever.

## Problem sdk
```
Failed to install the following Android SDK packages as some licences have not been accepted.
     build-tools;28.0.2 Android SDK Build-Tools 28.0.2
```
check mal `/home/david/Android/Sdk` und sehe du hast kein 28...

I start the studio:
```
/home/david/Android/android-studio/bin/studio.sh
```
Dort auf SDK Manager


# Testen auf Gerät
Builden und dann:

`[david@localhost QFieldWork_camfix01]$ adb install ../build-QField-Android_for_armeabi_v7a_GCC_4_9_Qt_5_9_4_for_Android_armv7-Debug/android-build/build/outputs/apk/android-build-debug.apk`

# Issues and sollutions

Beim Compilen auf Android kam der Fehler:
```
/home/david/Android/crystax-ndk-10.3.2/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/../lib/gcc/arm-linux-androideabi/4.9/../../../../arm-linux-androideabi/bin/ld: error: cannot find -lcrystax
```

Da waren zwei Warnings:

```
:-1: warning: "/home/david/Android/crystax-ndk-10.3.2/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc" is used by qmake, but "/home/david/Android/crystax-ndk-10.3.2/toolchains/llvm-3.6/prebuilt/linux-x86_64/bin/clang" is configured in the kit.
Please update your kit (Android for armeabi-v7a (Clang Qt 5.11.3 for Android ARMv7)) or choose a mkspec for qmake that matches your target environment better.
```
```
:-1: warning: "/home/david/Android/crystax-ndk-10.3.2/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-g++" is used by qmake, but "/home/david/Android/crystax-ndk-10.3.2/toolchains/llvm-3.6/prebuilt/linux-x86_64/bin/clang++" is configured in the kit.
Please update your kit (Android for armeabi-v7a (Clang Qt 5.11.3 for Android ARMv7)) or choose a mkspec for qmake that matches your target environment better.
```

Die kann man aber irgenwie gar nicht einstellen, deshalb hab ich mir neues manuelles Build&Run gemacht "Android ARM"

Dort aber weiss ich nicht was fehlt...


# So geht das

Copy everything from the docker.

- SDK
- NDK (including compilers)
- QT
- Libraries (osgeo4a)

## Search the docker image
```
sudo docker run -t -d opengisch/qfield-sdk:20190528
sudo docker exec -it dazzling_hellman bash
```
it's in `/opt`:
```
root@92c7d0ba116f:/# ls -l /opt/
total 12
drwxr-xr-x  1 root root 4096 May 28 09:14 Qt
drwxr-xr-x 13 root root 4096 Jan 14 18:28 android-ndk
drwxr-xr-x  9 root root 4096 Feb 19 17:56 android-sdk
```

## Copy the stuff

```
    sudo docker cp dazzling_hellman:/home/osgeo4a /home/david/dev/osgeo4a
    sudo docker cp -a dazzling_hellman:/opt/. /home/david/dev/Android_Env/
```

## Click your Android Kit together in Qt Creator

- create compilers and add
- add QT
- 