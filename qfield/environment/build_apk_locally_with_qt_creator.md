## 1. Getting the stuff

### OSGeo4A
Find the version in https://github.com/opengisch/QField/sdk.conf (`osgeo4a_version`) ...

... and get the correct package here https://github.com/opengisch/OSGeo4A/releases according to the version.

### Android SDK, Android NDK and QT
Find the correct version in https://github.com/opengisch/OSGeo4A/Dockerfile (`FROM opengisch/qt-ndk:5.13.1-1`) ...

... and get the correct package here https://github.com/opengisch/docker-qt-ndk/releases according to the version.

Put everything to one folder e.g. looking like this:
```
$ ls -l qfield_devenv/
total 16
drwxr-xr-x 13 david david 4096 Jan 14  2019 android-ndk
drwxr-xr-x  9 david david 4096 Sep 30 13:14 android-sdk
drwxr-xr-x  6 david david 4096 Dez  3 20:08 osgeo4a
drwxr-xr-x  3 david david 4096 Sep 30 13:13 Qt
```

## 2. Configuration in Qt Creator

### JDK, NDK and SDK

Options -> Devices -> 

Like e.g.:
- JDK Location:  `/usr/lib/jvm/java-1.8.0-openjdk-amd64`
- Android SDK Location:  `/home/david/qfield_devenv/android-sdk`
- Android NDK Location:  `/home/david/qfield_devenv/android-ndk`

### Qt Version

Options -> Kits -> Qt Versions -> 

- qmake location:  `/home/david/qfield_devenv/Qt/5.13.1/android_arm64_v8a/bin/qmake`

### Clang Compilers

Options -> Kits -> Compilers ->

Add C Clang Compiler manually with settings like e.g.:
- Compiler path:  `/home/david/qfield_devenv/android-ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/clang`
- Plattform codegen flags:  `-target aarch64-linux-android`
- Plattform linker flags:  `-target aarch64-linux-android`

Add C++ Clang Compiler manually with settings like e.g.:
- Compiler path:  `/home/david/qfield_devenv/android-ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/clang++`
- Plattform codegen flags:  `-target aarch64-linux-android`
- Plattform linker flags:  `-target aarch64-linux-android`

### Kit Configuration

Create your Kit accordingly if not created automatically.

![image](/uploads/4dafe5387f04cf7864f07840b376b900/image.png)


## 3. Debugging

When debugging most possibly a SIGILL signal is received what stops the process.

Deactivate it:

Options -> Debugger -> GDB -> 

- Additional Startup Commands:  `handle SIGILL nostop`

Info from here: https://stackoverflow.com/questions/25708907/ssl-library-init-cause-sigill-when-running-under-gdb 