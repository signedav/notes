```
rm -rf build-QGIS-Desktop-Debug
mkdir build-QGIS-Desktop-Debug
cd build-QGIS-Desktop-Debug
export CC=/lib64/ccache/clang
export CXX=/lib64/ccache/clang++

cmake -DCMAKE_CXX_COMPILER=/usr/lib64/ccache/clang++ ../QGIS
cmake -DCMAKE_CC_COMPILER=/usr/lib64/ccache/clang ../QGIS

cmake -DCMAKE_BUILD_TYPE=Debug ../QGIS
cmake -DENABLE_TESTS=true ../QGIS
cmake -DWITH_ASTYLE=true ../QGIS
cmake -GNinja ../QGIS


rm -rf build-QGIS_tr-Desktop-Debug
mkdir build-QGIS_tr-Desktop-Debug
cd build-QGIS_tr-Desktop-Debug
export CC=/lib64/ccache/clang
export CXX=/lib64/ccache/clang++
cmake -GNinja ../QGIS_tr
cmake -DCMAKE_BUILD_TYPE=Debug ../QGIS_tr
cmake -DENABLE_TESTS=true ../QGIS_tr
cmake -DWITH_ASTYLE=true ../QGIS_tr


rm -rf build-QGIS_offed-Desktop-Debug
mkdir build-QGIS_offed-Desktop-Debug
cd build-QGIS_offed-Desktop-Debug
export CC=/lib64/ccache/clang
export CXX=/lib64/ccache/clang++
cmake -GNinja ../QGIS_offed
cmake -DCMAKE_BUILD_TYPE=Debug ../QGIS_offed
cmake -DENABLE_TESTS=true ../QGIS_offed
cmake -DWITH_ASTYLE=true ../QGIS_offed
cmake -DENABLE_TESTS=true ../QGIS_offed


rm -rf build-QGIS_bugfix-Desktop-Debug
mkdir build-QGIS_bugfix-Desktop-Debug
cd build-QGIS_bugfix-Desktop-Debug
export CC=/lib64/ccache/clang
export CXX=/lib64/ccache/clang++
cmake -GNinja ../QGIS_bugfix
cmake -DCMAKE_BUILD_TYPE=Debug ../QGIS_bugfix
cmake -DENABLE_TESTS=true ../QGIS_bugfix
cmake -DWITH_ASTYLE=true ../QGIS_bugfix
cmake -DENABLE_TESTS=true ../QGIS_bugfix

rm -rf build-QGIS_master-Desktop-Debug
mkdir build-QGIS_master-Desktop-Debug
cd build-QGIS_master-Desktop-Debug
export CC=/lib64/ccache/clang
export CXX=/lib64/ccache/clang++
cmake -GNinja ../QGIS_master
cmake -DCMAKE_BUILD_TYPE=Debug ../QGIS_master
cmake -DENABLE_TESTS=true ../QGIS_master
cmake -DWITH_ASTYLE=true ../QGIS_master
```

oder einfach:
```
bash rebuild-build QGIS_master
```

1. 
```
In file included from /usr/include/qt5/QtCore/qobject.h:56,
                 from /usr/include/qt5/QtCore/QObject:1,
                 from /home/signedav/dev/qgis/QGIS_master/src/core/qgscoordinatereferencesystem.h:31,
                 from /home/signedav/dev/qgis/QGIS_master/src/core/qgscoordinatetransform.h:25,
                 from /home/signedav/dev/qgis/QGIS_master/src/core/geometry/qgsabstractgeometry.h:24,
                 from /home/signedav/dev/qgis/QGIS_master/src/core/simplify/effectivearea.h:25,
                 from /home/signedav/dev/qgis/QGIS_master/src/core/simplify/effectivearea.cpp:25:
/usr/include/qt5/QtCore/qobject_impl.h:43:2: error: #error Do not include qobject_impl.h directly
 #error Do not include qobject_impl.h directly
  ^~~~~

```

oder auch mit:
```
rm -rf build-QGIS_master-Desktop-Debug
mkdir build-QGIS_master-Desktop-Debug
cd build-QGIS_master-Desktop-Debug
export CC=/usr/lib64/ccache/x86_64-redhat-linux-gcc
export CXX=/usr/lib64/ccache/x86_64-redhat-linux-g++
cmake ../QGIS_master
cmake -DCMAKE_BUILD_TYPE=Debug ../QGIS_master
cmake -DENABLE_TESTS=true ../QGIS_master
cmake -DWITH_ASTYLE=true ../QGIS_master
```

```
In file included from /usr/include/qt5/QtGui/qwindowdefs.h:44,
                 from /usr/include/qt5/QtGui/qpaintdevice.h:44,
                 from /usr/include/qt5/QtGui/qpixmap.h:44,
                 from /usr/include/qt5/QtGui/qicon.h:46,
                 from /usr/include/qt5/QtGui/QIcon:1,
                 from /home/signedav/dev/qgis/QGIS_master/src/core/layertree/qgslayertreemodellegendnode.h:22,
                 from /home/signedav/dev/qgis/QGIS_master/src/core/layertree/qgslayertreemodellegendnode.cpp:19:
/home/signedav/dev/qgis/QGIS_master/src/core/qgsproject.h:91:5: internal compiler error: Segmentation fault
     Q_OBJECT
     ^~~~~~~~
Please submit a full bug report,
with preprocessed source if appropriate.
See <http://bugzilla.redhat.com/bugzilla> for instructions.
The bug is not reproducible, so it is likely a hardware or OS problem.
```