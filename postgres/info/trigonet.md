## To Do:
- [ ] Check nochmals slides
- [ ] Script um DB / Schema / EXTENSION / Tabelle erstellen
- [ ] Gute links für das erstellen
- [ ] Script um Benutzer zu erstellen
- [ ] PostGIS2gpkg Test (evtl. ihr Use Case)
- [ ] Installiere PG Modeller
- [ ] Geometriechecks: PostGIS Script vorbereiten
- [ ] Geometriechecks: QGIS Möglichkeiten auschecken
- [ ] Editor Tracking tools..

## Workshop 26. April
### PostgreSQL Allgemein
#### Servers / Instances

**Instances** sind **Services** sind **Server** und heissen **Clusters**. Was so ein **Cluster** beinhaltet (zBs. auch Logins), sehen wir dann im **PgAdmin**. 

Das Wort **Cluster** ist ein bisschen unglücklich, da man denken könnte, dass die Last über mehrere "physische" Server aufgeteilt wird. Das ist aber etwas anderes (kann schon bewerkstelligt werden mit Container-Lösungen wie Docker oder Kubernetes.

```
initdb -D /usr/local/pgsql/data
psql -U Postgres -p 5436 -h localhost
CREATE DATABASE mynewdb;
pg_ctlcluster 12 main start
```

Gibt sicher auch **Managing Tools**, aber müsste ich selbst googlen.

Gut für:
- Isolation
- Versionen
- Allocate Ressources: Glaube schon möglich, aber übersteigt etwas mein Know-How - Sicher möglich bei Containerisierungen
- Logins (kann man pro Instanzes festlegen) Betr. AD weiss ich, dass es mit LDAP genutzt wird, aber übersteigt etwas mein Know-How
- Clusterübergreiffende SELECTS (Views) - wie gesagt sowieso möglich mit QGIS. DBübergreiffend möglich mit `dblink` EXTENSION siehe https://stackoverflow.com/questions/42447131/postgres-run-same-query-on-all-databases-same-schemas/72562629#72562629 - nicht sicher ob auch clusterübergreiffend...
  
#### Installation

Siehe das hier: https://github.com/opengisch/postgresql-admin-course/blob/master/installation.md

- *Weshalb möchtet ihr von Source builden?*
- Version 15 oder 16: https://www.postgresql.org/docs/release/
- Ich würde offizielle Version installieren

Weiter kann man dann Connection-Masken konfigurieren https://www.postgresql.org/docs/current/auth-pg-hba-conf.html

##### PostGIS
Letzter Release PostGIS 3.4.1 von postgis.net

```
CREATE EXTENSION postgis;
-- enabling raster support
CREATE EXTENSION postgis_raster;
-- enabling advanced 3d support
CREATE EXTENSION postgis_sfcgal;
-- enabling SQL/MM Net Topology
CREATE EXTENSION postgis_topology;
```

#### PG Admin
- Durchgehen
  - Nützliche Extensions
    ```
    CREATE EXTENSION uuid-ossp;
    CREATE EXTENSION hstore;
    ```
- DB Aufbau / DB Backup
- Alternative (DBeaver)
  
#### PSQL 

https://www.postgresql.org/docs/current/app-psql.html

##### DB und Schema erstellen und EXTENSIONS aktivieren

```none
$psql -hlocalhost -p5432 -dpostgres -Upostgres 

postgres=# CREATE DATABASE gis;
 
postgres=# \connect gis;

postgres=# CREATE SCHEMA kbs;
```
etc.

##### Rollen und User erstellen
```
CREATE ROLE dave WITH CREATEDB CREATEROLE LOGIN PASSWORD 'Quentin';
```
Siehe auch: https://www.postgresql.org/docs/current/sql-createrole.html

`CREATE USER` ist `CREATE ROLE`, das `LOGIN` impliziert...

Man kann dann Berechtigungen vergeben:
```
GRANT INSERT ON tabelle TO dave;
GRANT INSERT ON SCHEMA kbs TO dave;
GRANT INSERT ON DATABASE gis TO dave;
```

Oder Gruppen:
```
CREATE GROUP opengisch-ninja WITH USER dave;
```
```
GRANT INSERT ON tabelle TO GROUP opengisch-ninja;
```
etc.

#### PL/pgsql

Kann man cooles Zeug machen:
https://github.com/QGEP/datamodel/blob/master/12_0_roles.sql
Damit kann man auch die Trigger funktionen machen:
```
CREATE OR REPLACE FUNCTION
test.insert_update_test_trigger_function()
RETURNS trigger AS
$BODY$
  BEGIN
    NEW.flaeche := ST_Area(NEW.the_geom);
    NEW.umfang := ST_Perimeter(NEW.the_geom);
    RETURN NEW;
  END;
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;
```

### Möglichkeiten mit QGIS

#### Editierung der Datenbank in QGIS

- Connection hinzufügen
- DB Browser
- DB Manager

#### Authentification
##### Service File
```
SHOW config_file;
```
- [ ] Teste das aus inkl. Verbindung auf Docker (server)

##### Authentifications

#### PG Modeller
Demo...

#### Geometriechecks
- [ ] To do...

### Montioring und Wartung

#### Maintanance und Backups

Siehe: https://github.com/opengisch/postgresql-admin-course/blob/master/maintenance.md

pg_dump für Schema mit `--schema` für Tabelle mit `--table`.

Aktivieren von autovacuum etc. so: https://github.com/opengisch/postgresql-admin-course/blob/master/konfiguration.md

#### Monitoring Tool

- [ ] To do...

#### Editor Tracking Lösung
Es gibt die normalen Logging-Funktionen, wie:

```
log_statement = all
```

Grundsätzlich kann das mit Triggers bewerkstelligt werden. Ist also "zu bauen", gibt aber vorlagen: 
https://wiki.postgresql.org/wiki/Audit_trigger_91plus

Ausserdem gibt es auch eine Extension: https://github.com/pgaudit/pgaudit

oder auch `pg_stat_statements`

### PG2GPKG and Back

- [ ] pg2gpkg und zurück (SDE Datenbanken werden regelmässig in File-GeoDatabases konvertiert um weiterzugeben) - was passiert mit Beziehungen? Kann das GPKG nicht?

#### Historisierungs Lösung
Könnte man auch mit Trigger-Functions bauen...

#### Versionisierung
Nicht direkt in PosgreSQL oder QGIS aber mit anderen Lösungen:

https://kartproject.org/

### Sonstiges

#### INTERLIS Baskets
- Demo vorzeigen

#### Raster in PG?

Nun in PostgreSQL kann man das machen, ist aber u.U. nicht so preformant. Habe Info in Slides dazu...

Wir haben gute Erfahrungen gemacht mit Cloud Optimized Geo Tiff (COG), die kann man auf ein Netzlaufwerk tun oder auf einen HTTP Server).

### Was sie so nutzen

MSSQL für Produktion und für Publikation

ArcSDE Dings 

ArcMap oder GEONIS

FME

Bauen auch mit ArcMap Datenmodelle

Oder auch INTERLIS

Auch machen sie an bestehenden Datenbanken updates. Das können sie mit GeoCom XML Script wo man das ganze Modell bauen kann. Oder auch mit Anpassungen über ArcGIS. Das kann man soweit nicht in QGIS...

### Was sie wollen

MSSQL sollen nicht ersetzt, sondern ergänzt werden mit PostgreSQL mit Fokus auf Produktion.

Mit PostgreSQL möglichst auf Windows bleiben.

Maintanance und Monitoring + Backup Konzept

Mit Knopfdruck für Maintanance -> Gibt es Oberfläche von SQL Server

### Aufbau

Mehrere SQL Server Instanzen. Mehrere Windows Services.

In Instanzen aufgeteilt deshalb, damit man gewisse Ressourcen zuweisen könnte RAM und Cores.

Instanzen haben Logins oder AD Join.

Datenbanken haben Benutzer / Rollen.

Spiegeln von einer Instanz auf die Andere und die SIDs (Logins) sind überall gleich.

Ein Operat ist meistens Thema (zBs. Wasser) + Gemeinde (zBs. Obwalden).

Pro Operat eine eigene Datenbank - Mehrere 100 Datenbanken. Auch super mit Berechtigungen.

Sie haben oft die gleichen Benutzer und die selben Modelle / Strukturen auf den DBs, weshalb sie gleiche Scripts nützen können.

Meistens ein bis drei Schemas pro DB.

Sie können auch einfach ein Dump wiedereinspielen, wenn für einzelne Operate so...

### Weitere Info über MSSQL - kann das PG auch?

Instanzen haben Logins oder AD Join.

MS SQL kann DB übergreiffend sein... SELECT * FROM dbname.schemaname.tabellenname Nur innerhalb derselben Instanz Möglich


### Was sie noch wissen möchten

- Maintanance Backup Monitoring

- Connectionfiles pg_config + auf Server oder Lokal + Authentification - kann man auch im QGIS so ähnlich?

- In ArcMap kann man verchlüssletes Passwort in Projekt speichern - was kann QGIS?

- Kann das PG auch (Weitere Info über MSSQL)

- QGIS Layerzugriff MSSQL / PostgreSQL was sind die unterschiede - vorzeigen

- INTERLIS Baskets und Datasets kurz zeigen...

- DB Mutationen mit QGIS / INTERLIS

- Durch PG Admin klicken und alles erklären (in struktur links)

- PG Installation: wie baut man best practice die db auf (von source builden) und Postgis und ersetzen.

- Scripts um DB zu erstellen etc...

- Weitere Nützliche Extensions ausser POSTGIS

- pg2gpkg und zurück (SDE Datenbanken werden regelmässig in File-GeoDatabases konvertiert um weiterzugeben) - was passiert mit Beziehungen? Kann das GPKG nicht?

- Auf ESRI kann man eine Version machen. Darauf arbeiten und dann wieder verwerfen. Also eine temporäre Kopie um darauf zu arbeiten und dann in den Dataset einspeisen (Snapshots evt.) - gibt es etwas ähnliches

- Archiv - eher wie Historisierung / zeigen evtl. Triggerlösung - evtl. wieQField

- EditorTracking (Username+Zeitstempfel)


## Aufbau von Produktionsdatenbanken / Schemas

### Wie aufbauen?

- PSQL (Statements)
- PGAdmin
- DBeaver
- PostgreSQL Studio (web-basiert) kenn ich nicht
- TeamPostgreSQL (web-basiert) kenn ich nicht

> Demo: Wie bauen in PSQL (gemäss Script), PGAdmin, DBeaver

### Wie aufteilen?

#### Für was eigene Datenbanken?

> A client connection to the server can only access data in a single database, the one specified in the connection request.

Natürlich ist es möglich in QGIS auf mehrere Datenbanken zuzugreifen. Doch um zBs. JOINS zu bauen, ist dies einfacher wenn es nicht datenbankübergreiffend ist (sondern nur schemaübergreiffend).

Datenbanksettings sind Schemaübergreiffend (zBs. EXTENSIONS etc.), müssen also nur einmal gemacht werden.

Kommt auch auf die Anforderung drauf an drauf an. Manche haben test / prod etc. Datenbanken. Andere haben dafür seperate Cluster (Server).

#### Für was eigene Schemas?

In INTERLIS macht es sicherlich Sinn für ein einzelnes Modell ein Schema zu machen (schon nur zur Ordnung mit allen ili2db Tabellen) und auch der Benennungen der Tabellen (weiss ich gar nicht was geschieht, wenn diese nicht mehr einmalig sind).

#### Und mit den Operaten?

Sofern Operate Gemeinden sind, gibt es da verschiedene Ansätze, die ersten drei kommen eigentlich auf das gleiche heraus (technisch):

- Ein Schema pro Operat + Modell
- Eine DB pro Operat und ein Schema pro Modell
- Eine DB pro Modell und ein Schema pro Operat
- Datasets und Baskets (vor allem cool, wenn man auch mehrere Operate in einem Datensatz haben können möchte)

Die Frage ist, inwieferen diese Daten einzeln behandelt werden sollen. Möchte man zBs. Usern zugriff auf alle Modelle eines einzigen Operates geben (dann DB pro Operat optimaler (Schema auch möglich) aber Baskets nicht so gut)? Oder eher eines einzigen Modells für alle Operate (dann eher DB pro Modell, oder auch mit Baskets nice).

#### Best Practices

Who knows? Jeder macht's anderst...

## Erstellung von Datenmodellen

### Tools

- INTERLIS gibt es den UML Editor
- Für Postgres gibt es den PGModeller (auch reverse modeller)

#### Doku-WebSites und Libraries

Wofür?

## Datenbankobjekte

### ESRI Zeuch
- Tabellen
- FeatureDatasets
- FeatureClasses
- Relationshipclasses
- Domains

FeatureClasses einfach Tabellen sind mit Geometrien.

Und Feature Datasets ähnlich Schemas separieren Daten und Struktur in File-Geodatabase.

Relationships werden normalerweise in PostgreSQL mit FKs und Join-Tabellen gelöst. In INTERLIS mit ASSOCIATIONS modelliert. In QGIS werden dann (mit oder ohne Model Baker) die Relations erstellt.

### Rasterdaten

Wurde vermutlich mit ArcGIS auch in DB gespeichert. Nun in PostgreSQL kann man das machen, ist aber u.U. nicht so preformant. Habe Info in Slides dazu...

Wir haben gute Erfahrungen gemacht mit Cloud Optimized Geo Tiff (COG), die kann man auf ein Netzlaufwerk tun oder auf einen HTTP Server).

## Konfiguration

### Mutliuser

### Versionierungen,…

### Berechtigungen

### EditorTracking

Es gibt die normalen Logging-Funktionen, wie:

```
log_statement = all
```

Grundsätzlich kann das mit Triggers bewerkstelligt werden. Ist also "zu bauen", gibt aber vorlagen: 
https://wiki.postgresql.org/wiki/Audit_trigger_91plus

Ausserdem gibt es auch eine Extension: https://github.com/pgaudit/pgaudit

oder auch `pg_stat_statements`

### Archiv

## Checktools

- bspw. Fläche geschlossen
- PostGIS checks
- QGIS Möglichkeiten

## PostGIS2gpkg und umgekehrt

### was gibt es da
ogr2ogr

### wie wird mit Beziehungen usw. umgegangen?
vermutlich gehen die kaputt...

## Aufbau (Verständnis) von User und Rollen (Server und Datenbanken)

### About owner
> Ordinarily, only the object's owner (or a superuser) can grant or revoke privileges on an object. However, it is possible to grant a privilege “with grant option”, which gives the recipient the right to grant it in turn to others.


### best practice
 
## Vorlage Skript’s für die (halb)automatische Erstellung von Datenbanken, User und Rollen

https://github.com/QGEP/datamodel/blob/master/12_0_roles.sql

## Gibt es etwas ähnliches wie Connectionfile’s?

- PGService.conf -> cool da man dann mit dem gleichen QGIS Projekt auf unterschiedliche Systeme zugreiffen kann (Prod und Test zBs.) - Ausserdem ist Info nicht im Projekt
- Oder QGIS Authentification File (kann auch mit Services kombiniert werden)

### Umgang mit User und PW?
- was ist gemeint? Mit den Daten oder geht es um die Rollen?

### Unterschiedliche Verbindungspfade (Cnames)

- was ist gemeint? Ob die im hba.conf zugelassen sind?

## Wartungstasks

### Welche

- Backup
- Autovacuum
- Indexierung?

### Wann

- Keine Ahnung...

### Datensicherungen

- Regelmässige Dumps
- WAL Möglichkeiten

### Zeitgesteuert

- CRON?
