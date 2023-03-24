Information from here: http://qgep.github.io/docs/installation-guide/
I just take the users postgres. And because the dump used here creates a db qgep_prod anyway we use this instad of qgep.

## pg_service.conf
```
sudo vim ~/.pg_service.conf
```
```
[pg_qgep]
host=127.0.0.1
port=5432
dbname=qgep
user=postgres
```

## pgpass
```
sudo vim ~/.pgpass
```
```
127.0.0.1:5432:*:postgres:postgres
```

## Group roles we skip because we take postgres/postgres

## Create DB qgep and extensions
```
psql -c "CREATE DATABASE qgep;" -U postgres
psql -c "CREATE EXTENSION postgis;" -U postgres -d qgep
psql -c "CREATE EXTENSION hstore;" -U postgres -d qgep
```

## Run the script
```
./scripts/db_setup.sh
```

## Get Demo stuff
From https://github.com/QGEP/datamodel/releases/latest I download `qgep_vx.y.z_structure_and_demo_data.backup`

## Restore the demo data
```
pg_restore --host 127.0.0.1 --port 5432 --username "postgres" --dbname "qgep" --no-password  --create --clean qgep_v1.1.1_structure_and_demo_data.backup
```
Because of this error:
```
pg_restore: [archiver (db)] Error while PROCESSING TOC:
pg_restore: [archiver (db)] Error from TOC entry 7165; 0 41575 MATERIALIZED VIEW DATA vw_network_segment postgres
pg_restore: [archiver (db)] could not execute query: ERROR:  function st_curvetoline(public.geometry, integer) does not exist
LINE 1: SELECT ST_CurveToLine($1, 32)
               ^
HINT:  No function matches the given name and argument types. You might need to add explicit type casts.
QUERY:  SELECT ST_CurveToLine($1, 32)
CONTEXT:  SQL function "st_curvetoline" during inlining
    Command was: REFRESH MATERIALIZED VIEW qgep_od.vw_network_segment;
```
If there is more problem, do restore in PGAdmin III 

I created this two views manually like mentioned here: https://github.com/QGEP/QGEP/issues/409
https://github.com/QGEP/datamodel/blob/master/07_views_for_network_tracking.sql


## QGIS Demo Project

Open the project from the project/folder (qgep_en.qgs) from the QGEP/QGEP repo.

Or get the QGIS Project from here (without taking this dump):
Download https://github.com/QGEP/data/archive/demodata.zip

## Testing
Run the latest deltafiles (replace `%%` with `%` and set the srid `2056`)

and then:
```
nosetests
```

# And because like always everything fucks up I did the following
## pg_service.conf
```
sudo vim ~/.pg_service.conf
```
```
[pg_qgep]
host=127.0.0.1
port=5432
dbname=qgep_prod 
user=postgres
```

## create qgep_prod db
## Get Demo stuff
From https://github.com/QGEP/datamodel/releases/latest I download `qgep_vx.y.z_structure_and_demo_data.backup`

## Restore the demo data
```
pg_restore --host 127.0.0.1 --port 5432 --username "postgres" --dbname "qgep_prod" --no-password  --create --clean qgep_v1.1.1_structure_and_demo_data.backup
```

I created this two views manually like mentioned here: https://github.com/QGEP/QGEP/issues/409
https://github.com/QGEP/datamodel/blob/master/07_views_for_network_tracking.sql


## QGIS Demo Project

Open the project from the project/folder (qgep_en.qgs) from the QGEP/QGEP repo.

Or get the QGIS Project from here (without taking this dump):
Download https://github.com/QGEP/data/archive/demodata.zip

## Testing
Run the latest deltafiles (replace `%%` with `%` and set the srid `2056`)

and then:
```
nosetests
```



# And because like always everything fucks up AGAIN I did the following
## pg_service.conf
```
sudo vim ~/.pg_service.conf
```
```
[pg_qgep]
host=127.0.0.1
port=5432
dbname=qgep_prod 
user=postgres
```

## create qgep_prod db in pgadmin and the extenstions 
**see that file (in Appendix more) - there I created it in console and worked like a charm: https://gitlab.com/signenotes/opengisch/technical_notes/blob/master/linux/ubuntu18.04.md**

## and then and make the restore there of:
From https://github.com/QGEP/datamodel/releases/latest I download `qgep_vx.y.z_structure_and_demo_data.backup`
In PgAdmin also, like a toy. Rightclick - restore:
Activate in `Restore Options #2` the `Include CREATE DATABASE...` and the `Clean before restore`

Then there are errors... that's why I run these scripts:
https://github.com/QGEP/datamodel/blob/master/07_views_for_network_tracking.sql

## Run the deltas and testing
Run the latest deltafiles (replace `%%` with `%` and set the srid `2056` instead of )
(file addtional_deltas_etc.sql)

and then:
```
nosetests
```