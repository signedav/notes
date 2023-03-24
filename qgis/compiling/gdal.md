# compile with new gdal

## install gdal
```
git clone git@github.com:OSGeo/gdal.git
cd gdal/gdal
./configure --prefix=$HOME/dev/gdal-install (oder en andere prefix path...)
make -j8 install
```

## build
```
rm -rf build-QGIS-gdalmaster
mkdir build-QGIS-gdalmaster
cd build-QGIS-gdalmaster
cmake -GNinja ../QGIS_action
cmake -DCMAKE_BUILD_TYPE=Debug ../QGIS_action/
cmake -DGDAL_CONFIG=/home/david/dev/gdal-install/bin/gdal-config -DGDAL_INCLUDE_DIR=/home/david/dev/gdal-install/include -DGDAL_LIBRARY=/home/david/dev/gdal-install/lib(libgdal.so ../QGIS_action/
ninja
```