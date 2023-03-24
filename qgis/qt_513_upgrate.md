First I built QCA with `/home/david/dev/Android_Env/Qt/5.13.1/5.13.1/gcc_64/`

Installed it on `/home/david/qfield_devenv/5.13/`

Then created new cmake on QGIS:
```
ccmake -G Ninja -DCMAKE_PREFIX_PATH=/home/david/dev/Android_Env/Qt/5.13.1/5.13.1/gcc_64/  ../QGIS_2/
```

And there I set the `QCA` pathes to `/home/david/dev/Android_Env/Qt/5.13.1/5.13.1/gcc_64/`

and the `CMAKE_INSTALL_PREFIX_PATH` to `/home/david/dev/Android_Env/Qt/5.13.1/5.13.1/gcc_64/`


And then on QField build folder:

```
cmake -G Ninja -DCMAKE_PREFIX_PATH=/home/david/dev/Android_Env/Qt/5.13.1/5.13.1/gcc_64/ -DQGIS_INCLUDE_DIR=/home/david/qfield_devenv/5.13/include/qgis/ -DQGIS_CORE_LIBRARY=/home/david/qfield_devenv/5.13/lib/libqgis_core.so  ../QField/
```

But because it needs the correct library path this variable has to be defined (Run Environment on QtCreator):
```
export LD_LIBRARY_PATH=/home/david/qfield_devenv/5.13/lib/:/home/david/dev/Android_Env/Qt/5.13.1/5.13.1/gcc_64/lib/
```