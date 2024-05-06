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
> 
> **port:** Port auf dem der Server die Verbindungen entgegennimmt.
> 
> **max_connections:** Maximale Anzahl der Verbindeungen, die gleichzeitig auf den PostgreSQL Server offen sein können. Je höher diese Zahl, desto höher die Speicheranforderung (siehe shared_buffers).

#### Speicher
- shared_buffers
- work_mem
- maintenance_work_mem

> **shared_buffers:** Die grösse des Shared-Buffer-Pools fest. Dieser puffert die Zugriffe und verhindert so, dass direkt auf HD-Speicher geschrieben werden muss. Normalerweise soll hier zw. 10 und 25 % des verfügbaren Arbeitsspeicherplatzes verwendet werden.
> 
> **work_mem:** Die Grösse des zur Vefügung stehenden Hauptspeichers für Algorithmen wie sortieren, filtern etc. Je nach dem, wie zufriedenstellend die Geschwindigkeite dieser Funktionen ist, kann dies verändert werden.

#### Transaktionslog / WAL
- fsync

> **fsync:** ist on oder off und sorgt dafür, dass ein Transaktionslog geschrieben wird oder nicht. Wenn nicht ist die Performance besser, dafür riskiert man Datenverlust bei einem Absturz etc. 

#### Logs
- log_destination
- logging_collector
- log_line_prefix
- lc_messages

> **log_destination:** Wohin soll geloggt werden? stderr, syslog, eventlog oder csvlog
> 
> **logging_collector:** Wenn aktiviert wird eine Art ein PostgreSQL-eignenes Syslog geführt.
> 
> **log_line_prefix:** Wird vor jede Zeile geschrieben. Kann Platzhalter brauchen wie zBs. %d für Datenbanknahme etc.
> 
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

# Start/Stop 
  
## Starten des Servers mit postgres
Einfachstes starten ist so:
```
$ postgresql
```
Vorausgesetzt die Environmentvariable `PGDATA` enthält den Pfad zu den Daten. Ansonsten:
```
$ postgresql -D /usr/local/pgsql/data
```
Dies lässt den Server im Vordergrund laufen. Um ihn im Hintergrund zu starten. 
```
$ postgres -D /usr/local/pgsql/data >logfile 2>&1 &
```
## Starten des Servers mit pg_ctl
```
$ pg_ctl start -D /usr/local/pgsql/data -l logfile
```
Da dieses Kommando vielseitig und einfach einsetzbar ist, ist es gegenüber `postgres` zu bevorzugen.

## Stoppen und Restarten des Servers mit pg_ctl
```
$ pg_ctl stop [-W] [-t seconds] [-s] [-D datadir] [-m s[mart] | f[ast] | i[mmediate] ]
```
```
$ pg_ctl restart [-w] [-t seconds] [-s] [-D datadir] [-c] [-m s[mart] | f[ast] | i[mmediate] ] [-o options]
```
> -W (Do not wait for startup or shutdown to complete. This is the default for start and restart modes.)
> -t (timeout)
> -s (print only errors and no infos)
> -c (allow to produce core files on crash)
> -m (mode: smart, fast, or immediate)
> -o (postgres command options)

## Starten der DB beim Aufstarten des Systems
Um den PostgreSQL Server beim Systemstart zu starten, muss man dieses Start-Kommando `/usr/local/pgsql/bin/pg_ctl start -l logfile -D /usr/local/pgsql/data` zu `/etc/rc.d/rc.local or /etc/rc.local` hinzufügen. 
Weitere Möglichkeiten gibt es auch mit `contrib/start-scripts/linux`.

# Backup/Restore
## Möglichkeiten
- SQL dump
- File system level backup
- Continuous archiving

## SQL dump
Generiert ein File mit SQL Kommandos. Beim Restore wird mit dessen Ausführung die Datenbank mit Inhalt wiederhergestellt.
```
$ pg_dump dbname > outfile
```
```
$ psql dbname < infile
```
### pg_restore
Währenddem psql nur Dumps, die als Plain Text abgespeichert wurden, wiederherstellen kann, ist das `pg_restore` für ein (auch selektives) wiederherstellen von Custom-Format Dumps:
```
$ pg_restore -d dbname filename
```
Diese wurden mit dem Parameter für Format Custom hergestellt:
```
$ pg_dump -Fc dbname > filename
```
Vorteil im Custom Format ist, dass man erstens nur ausgewählte Teile wiederherstellen kann (zBs. eine Tabelle) und zweitens die Dumps kleiner sind. Nachteil, dass sie nicht lesbar und Probleme schwieriger nachvollziehbar sind.

### Komprimieren
Deshalb macht es auch Sinn das Textformat zu verwenden, allerdings zu komprimieren:
```
$ pg_dump dbname | gzip > filename.gz
```
```
$ gunzip -c filename.gz | psql dbname
```
### Auf zweiten Server backupen
Um vom einen Server auf einen anderen Server zu backupen, kann das "dumpen" und "restoren" in einer Pipe aufgerufen werden:
```
$ pg_dump -h host1 dbname | psql -h host2 dbname
```
### pg_dumpall
Mit `pg_dumpall` kann man alle Datenbanken im Cluster backupen. Ausserdem werden auch Roles und Tablespace Definitionen berücksichtigt.
```
$ pg_dumpall > outfile
```
### Automatisieren
Um ein Backup mit Dumps zu automatisieren, d.H. dass man nicht manuell die Dumps erstellen und kopieren soll, gibt es verschiedene Möglichkeiten. Eine davon ist hier beschrieben:
https://wiki.postgresql.org/wiki/Automated_Backup_on_Linux

## File system level backup
Das Speichern der effektiven Daten (Kopieren der Ordner und Dateien). 
```
$ tar -cf backup.tar /usr/local/pgsql/data
```
Nachteile dieser Methode sind, dass man dafür den PostgreSQL Server stoppen und immer sämtliche Daten backupen muss. Frozen snapshots sind bei zwar möglich, allerdings muss man aufpassen, da dann die Datenbank in einem Stadium wiederhergestellt wird, als wäre sie nicht richtig neu gestartet worden. 
Die Backup-Daten würden mehr Platz brauchen als ein Dump. Dafür würde das Backup schneller gehen.

## Continuous archiving  (online backup)
Man stelle sich vor, dass alle Änderungen geloggt werden und danach werden diese Änderungen alle wieder vollzogen. Bei dieser Methode kann oft gebackupt werden, da nicht so grosse Daten auf einmal gebackupt werden. Weiter kann die Datenbank eines beliebigen Zeitpunktes wiederhergestellt werden. Natürlich kann bei vielen Änderungen auch viel Traffic entstehen. 
Diese Methode lässt sich gut mit dem File system level backup kombinieren.

### Write Ahead Log
Continuous archiving funktioniert mit WAL (write ahead log) Files. WAL Files werden auch erstellt, um bei einem Crash die Daten wiederherzustellen. Es ist nicht ein riesiges File, sondern 16MB grosse Segmente: "Segment-Files". Da die älteren WAL Segment-Files allerdings immer wieder gelöscht werden, muss man sie für das Backup zuvor irgendwo abspeichern.
### Konfiguration
Im postgresql.conf:
```
wal_level = hot_standby oder wal_level = archive
archive_mode = on
archive_command = 'test ! -f /mnt/server/archivedir/%f && cp %p /mnt/server/archivedir/%f'
```
`%p` ist Pfadname, `%f` ist Filename des aktuellen Segment-Files.

### Wiederherstellen
```
SELECT pg_start_backup('label', true);
```
```
SELECT pg_stop_backup();
```
Beispiel:
```
touch /var/lib/pgsql/backup_in_progress
psql -c "select pg_start_backup('hot_backup');"
tar -cf /var/lib/pgsql/backup.tar /var/lib/pgsql/data/
psql -c "select pg_stop_backup();"
rm /var/lib/pgsql/backup_in_progress
tar -rf /var/lib/pgsql/backup.tar /var/lib/pgsql/archive/
```

## Was macht Sinn?
Verschieden Punkte beeinflussen die Entscheidung, welche Backup/Restore Methode am meisten Sinn macht:
- Beschaffenheit des Systems
- Gewünschter Backup-Intervall
- Grösse der Datenbank
- Aktivität der Datenbank


# Routine Vacuuming Maintanance
Vacuuming nennt man die periodische Datenbankmaintanance zu folgenden Zwecke:
- Speicherplatz freigeben
- Schützen vor Datenverlust aufgrund von **transaction ID wraparound**
- Datenbankstatistiken aktuell halten

## VACUUM
PostgreSQL gibt Speicherplatz nicht unmittelbar frei. Denn aufgrund von Multiversion Concurrency Control muss eine beispielweise gelöschte Zeile noch immer sichtbar bleiben.
```
VACUUM [ ( { FULL | FREEZE | VERBOSE | ANALYZE } [, ...] ) ] [ table [ (column [, ...] ) ] ]
```
- `VACUUM` (schneller, parallel zu Datenbankoperationen)
- `VACUUM FULL` (kann mehr Speicherplatz freigeben)
`VACUUM` wird generell empfohlen. Der Speicherplatz, den `VACUUM FULL` zusätzlich freigeben kann, wird oftmals sehr schnell wieder gefüllt.

## CLUSTER
Um Speicher freizugeben und an Performance zu gewinnen, kann man anstelle von `VACUUM FULL` auch `CLUSTER` verwenden. Dies sortiert eine Table anhand eines Indexes um.
```
CLUSTER [VERBOSE] table_name [ USING index_name ]
```

## Transaction ID Wraparound
Multiversion Concurrency Control benötigt die Transaktions-IDs (XID), um zu überprüfen, ob Transaktionen in der Zukunft oder Vergangenheit liegen. Da diese Transaktions-IDs allerdings auf 32 bit limitiert sind, beginnen sie nach vier Milliarden Transaktionen wieder von vorne. Damit dies nicht zu Datenverlust führt, soll nie mehr als die Hälfte der verwendeten Transaktions-IDs aktiv sein, und so mindestens alle zwei Milliarden Transaktionen einmal ein Vacuuming durchgefürt werden.

## ANALYZE
Der PostgreSQL Query Planner benötigt aktuelle Datenbankstatistiken. 
```
ANALYZE [ VERBOSE ] [ table [ ( column [, ...] ) ] ]
```
`ANALYZE` ist optionaler Bestandteil von `VACUUM`.
Dies soll häufiger gemacht werden bei Tabellen, die oft verändert werden. Insbesondere bei solchen, dessen Minimal- oder Maximalwert sich oft verändert. Also Beispielsweise, wenn pro Transaktion ein aktueller timestamp in eine Spalte geschrieben wird.
`ANALYZE` kann man nicht nur auf spezifische  Tabellen, sondern auch Spalten ausführen.

## REINDEX
Neuerstellen der Indexen kann Sinn machen.
- Wenn Index-Pages leer sind, wird der Speicherplatz freigegeben. Wenn sie allerdings nur "fast leer" sind, wird dieser Speicherplatz nur durch das Neuerstellen der Indexen freigegeben.
- Neuerstellte Indexen sind auch performanter.
```
REINDEX { INDEX | TABLE | DATABASE | SYSTEM } name [ FORCE ]
```
## Automatisieren
### Autovacuuming 
Das Feature `autovacuum` automatisiert das Vacuuming. Der "Autovacuum Deamon" besteht aus folgenden Prozessen:
- Der `autovacuum launcher` führt den `autovacuum worker` in einem definierten Intervall (`autovacuum naptime`) aus
- Der `autovacuum worker` führt die Befehle `VACUUM` und `ANALYZE` aus.
Es können mehrere `autovacuum worker` auf verschiedenen Datenbanken arbeiten.
### Cron
Natürlich ist eine Automatisierung in Linux auch mit Cron möglich, der zeitbasiertes Ausführen von Prozessen und Aufgaben (Cronjobs) regelt.

## Log File Maintenance
Es macht Sinn, alte Logfiles zu entfernen. Das File, in das der Output stderr geschrieben wird, kann man unterteilen indem man den Server neu startet. Dies ist in einer Produktivumgebung natürlich nicht möglich. Deshalb gibt es zwei andere Möglichkeiten.
### Externes Log Rotation Programm wie zBs. "rotatelogs"
```
$ pg_ctl start | rotatelogs /var/log/pgsql_log 86400
```
### Syslog
Wenn ausschliesslich ins Syslog geschrieben wird (aktivieren des Parameters `log_destination = syslog`, kann man mit einem `SIGHUP` Signal zum Syslog Daemon das Logfile unterteilen.

# Upgrades
## Versionen
Die ersten zwei Zahlen in den Versionsinformationen (9.6.) bezeichen die **major version** und die dritte den **minor release** (9.6.1). Minor Releases verändern das interne Speicherformat nie und sind immer kompatibel mit derselben Major Version.
## Möglichkeiten von Upgrades
- Dump und Reload
- pg_upgrade
- Replikation

## Upgrade mit Dump
Man braucht eine "logical" Backupmethode wie `pg_dumpall`. 
```
$ pg_dumpall > outputfile
$ pg_ctl stop
$ mv /usr/local/pgsql /usr/local/pgsql.old
```
Dann installieren der neuen PostgreSQL Version.
```
$ /usr/local/pgsql/bin/initdb -D /usr/local/pgsql/data
$ /usr/local/pgsql/bin/postgres -D /usr/local/pgsql/data
$ /usr/local/pgsql/bin/psql -d postgres -f outputfile
```
Oder man installiert die beiden Versionen parallel auf unterschiedlichen Ports und dumpt direkt in die neue Version:
```
$ pg_dumpall -p 5432 | psql -d postgres -p 5433
```
## Upgrade mit pg_upgrade
Eine schnellere Methode ist ein Upgrade mit `pg_upgrade`
```
$ pg_upgrade -b oldbindir -B newbindir -d olddatadir -D newdatadir [option...]
```
## Upgrade mit einem Replikationstool 
Gewisse Replikationssysteme, wie beispielsweise **Slony** unterstützen die Replikation zwischen verschiedenen Major Versionen von PostgreSQL. Somit kann man einfach die Datenbank replizieren und anschliessend den Master wechseln.

