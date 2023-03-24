## Use Case with fixing this https://github.com/claeis/iox-ili/issues/92

### Reproducing
Set up data:
```
$psql -Upostgres
postgres=# create role "gap-qgis_agi_admins";
postgres=# create role "gap-qgis_agi_write";
postgres=# create role "gap-qgis_agi_read";
$psql -Upostgres < /home/dave/projects/ili2db/sh_trouble_wkb8/admin_einteilung_20200917.sql 
```

Export command:
`java -jar /home/dave/dev/opengisch/QgisModelBaker/QgisModelBaker/libili2db/bin/ili2pg-4.4.4/ili2pg-4.4.4.jar --dbhost localhost --dbusr postgres --dbpwd postgres --dbdatabase postgres --dbschema edit_administrative_einteilung_lv95_v1_0 --export --models Administrative_Einteilung_LV95_V1_0 /home/dave/projects/ili2db/sh_trouble_wkb8/export_1.xtf`

There are other errors like:
`Error: line 0: Administrative_Einteilung_LV95_V1_0.Administration.Einteilung: tid 725: duplicate coord at (2673872.874, 1281329.799, NaN)`

But the ones I care for are:
`Error:     Unknown WKB type 8`

### Building ili2db
`gradle build`

#### Troubles
```
gradle build
Execution failed for task ':usrdoc'.
> A problem occurred starting process 'command 'python''
```
an when I uncommented the usrdoc part in the code this:
```
java -jar /home/dave/dev/thirdparty/ili2db/build/libs/ili2pg-4.4.6-SNAPSHOT.jar 
Error: Could not find or load main class ch.ehi.ili2pg.PgMain
```

#### Solved it like this:
1. I dont have the command python on my computer. I only had "python3" or "python2" - alias did not have any effects so I changed it in the build.gradle:
```
	-def python= System.getProperty('python',properties.get('python','python'))
	+def python= System.getProperty('python3',properties.get('python3','python3'))
```
2. It could not find my rst2html so Mario gave me this command to build (he does not remember why he noted down the second parameter)
`~/dev/thirdparty/ili2db$ gradle -Drst2html=`which rst2html.py` --info ili2pgBindist`
3. Now it builds it, but to run it has to be unzipped first:
`~/dev/thirdparty/ili2db$ unzip dist/ili2pg-4.4.6-SNAPSHOT.zip -d dist/ili2pg`
4. And now we can run it  
`~/dev/thirdparty/ili2db$ java -jar dist/ili2pg/ili2pg-4.4.6-SNAPSHOT.jar`

`~/dev/thirdparty/ili2db$ java -jar dist/ili2pg/ili2pg-4.4.6-SNAPSHOT.jar --dbhost localhost --dbusr postgres --dbpwd postgres --dbdatabase postgres --dbschema edit_administrative_einteilung_lv95_v1_0 --export --models Administrative_Einteilung_LV95_V1_0 /home/dave/projects/ili2db/sh_trouble_wkb8/export_1.xtf`

#### But the fix is in iox-ili
Well I build it:
`gradle build`

and then I copied the file to the ili2db to test it:
`~/dev/thirdparty$ cp iox-ili/build/libs/iox-ili-1.21.5-SNAPSHOT.jar ili2db/dist/ili2pg/libs/iox-ili-1.21.5-SNAPSHOT`

