## To Do:
- [ ] Script um DB / Schema / EXTENSION/Tabelle erstellen
- [ ] PgAdmin installieren
- [ ] Script um Benutzer zu erstellen
- [ ] PostGIS2gpkg Test (evtl. ihr Use Case)
- [ ] Installiere PG Modeller
- [ ] Geometriechecks: PostGIS Script vorbereiten
- [ ] Geometriechecks: QGIS Möglichkeiten auschecken

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

- [ ] trackt das auch User? Anyway, ist die Suche darin natürlich nicht so angenehm...
- [ ] wie macht man das?

Grundsätzlich kann das mit Triggers bewerkstelligt werden. Ist also "zu bauen", gibt aber vorlagen: 
https://wiki.postgresql.org/wiki/Audit_trigger_91plus

Ausserdem gibt es auch eine Extension: https://github.com/pgaudit/pgaudit

- [ ] check it out

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
