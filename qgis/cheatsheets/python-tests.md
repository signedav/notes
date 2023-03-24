Ausführen direkt im Build-Folder:
build-QGIS-Desktop-Debug

Wenns nicht geht muss man meistens nochmals builden - keine Ahnung weshalb.

Die Tests liegen in QGIS/tests/src/python

```
ctest -V -R PyQgsVectorLayerUtils
```

```
ctest -V -R PyQgsPostgresProvider
```

Für Plugins müssen die Env-Vars definiert werden:
```
export QGIS_PREFIX_PATH=/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output

export LD_LIBRARY_PATH=/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output/lib
export PYTHONPATH=/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output/python/:/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output/python/plugins:/home/dave/dev/qgis/QGIS/tests/src/python:

nosetests3 test_export.py 
```