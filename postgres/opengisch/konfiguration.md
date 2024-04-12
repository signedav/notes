# Konfiguration
## Erstellen eines PostgreSQL Benutzers
```
$ sudo passwd postgres
```

## Konfigurationsfile postgresql.conf
### Finden der Location
```
$ psql -U postgres
postgres=# SHOW config_file;
             config_file             
-------------------------------------
 /var/lib/pgsql/data/postgresql.conf
postgres=# \q
```
```
$ sudo vim /var/lib/pgsql/data/postgresql.conf
```
Mehr als 200 Parameter. Nicht alle sind essentiell.

### Wichtigste Parameter
#### Verbindungen
- listen_addresses = '*'
- port = 5432
- max_connections = 100
> **listen_addresses:** IP-Adressen von denen der Server Verbindungsanfragen entgegennimmt. Es können mehrere Adressen oder Hostnamen angegeben werden (getrennt durch Komma). Mit dem Platzhalter * erlaubt man Verbindungen von allen Adressen.
> **port:** Port auf dem der Server die Verbindungen entgegennimmt.
> **max_connections:** Maximale Anzahl der Verbindeungen, die gleichzeitig auf den PostgreSQL Server offen sein können. Je höher diese Zahl, desto höher die Speicheranforderung (siehe shared_buffers).

#### Speicher
- shared_buffers
- work_mem
- maintenance_work_mem
> **shared_buffers:** Die grösse des Shared-Buffer-Pools fest. Dieser puffert die Zugriffe und verhindert so, dass direkt auf HD-Speicher geschrieben werden muss. Normalerweise soll hier zw. 10 und 25 % des verfügbaren Arbeitsspeicherplatzes verwendet werden.
> **work_mem:** Die Grösse des zur Vefügung stehenden Hauptspeichers für Algorithmen wie sortieren, filtern etc. Je nach dem, wie zufriedenstellend die Geschwindigkeite dieser Funktionen ist, kann dies verändert werden.
> **maintanance_work_mem:** Die Grö

#### Transaktionslog / WAL
- fsync
> **fsync:** ist on oder off und sorgt dafür, dass ein Transaktionslog geschrieben wird oder nicht. Wenn nicht ist die Performance besser, dafür riskiert man Datenverlust bei einem Absturz etc. 

#### Logs
- log_destination
- logging_collector
- log_line_prefix
- lc_messages
> **log_destination:** Wohin soll geloggt werden? stderr, syslog, eventlog oder csvlog
> **logging_collector:** Wenn aktiviert wird eine Art ein PostgreSQL-eignenes Syslog geführt.
> **log_line_prefix:** Wird vor jede Zeile geschrieben. Kann Platzhalter brauchen wie zBs. %d für Datenbanknahme etc.
> **lc_messages:** Spracheinstellungen für die Log-Meldungen

#### Dienste
- autovacuum
- stats collector
> **autovacuum und stats collector** Aktiviert den Autovacuum-Dienst, der für die Wartung v on Tabellen und Indexen zuständig ist. Dies ist wichtig für eine gute Performance.

### SET / RESET in der aktuellen Session
Parameter können auch in der aktuellen Session konfiguriert werden:
```
postgres=# SET work_mem = '16MB';
```
Oder auch nur für die aktuelle  Transaktoin (LOCAL):
```
postgres=# SET LOCAL work_mem = '16MB';
```
Um den Parameter zurückzusetzen verwendet man RESET:
```
postgres=# RESET work_mem;
```

### Automatisches Generieren 
Mit https://www.pgconfig.org kann man das Konfigurationsfile automatisch und angepasst auf die individuellen Voraussetzungen generieren.

## Extensions
PostgreSQL ist beliebig erweiterbar. Mit PostgreSQL "mitgeliefert" sind die Module im `contrib/` directory. Beschreibung dazu hier: https://www.postgresql.org/docs/9.5/static/contrib.html
Andere, wie PostGIS, können zusätzlich installiert werden.

### Installieren von PostGIS
```
tar xvfz postgis-2.4.5dev.tar.gz
cd postgis-2.4.5dev
./configure
make
make install
```
### Hinzufügen von PostGIS
```
CREATE EXTENSION postgis;
```
oder ändern:
```
ALTER EXTENSION postgis UPDATE TO "2.4.4";
```


