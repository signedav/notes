Tests werden gemacht im Build-Folder:
```
[david@localhost build-QGIS-Desktop-Debug]$ ctest -R translatepro -V
```
Hierbei wird `TestQgsTranslateProject` gestartet.

Falls die Tests nicht kompiliert werden, muss dies aktiviert werden:
```
cmake -DENABLE_TESTS=true ../QGIS
```

oder für python tests im QGIS - Folder:

ctest -R PostgresProvider

Bei diesen Tests, muss die Test-DB aufgsetzt werden. Bevor der Test gestartet wird noch : export QGIS_PGTEST_DB="dbname='qgis_test' user='postgres' password='postgres'"



oder so...
