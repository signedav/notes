## Was ist das Connection Service File?

Das Connection Service File erlaubt Verbindungsoptionen pro sogenannten "Service" lokal abzuspeichern.

Habe ich also auf einem lokalen PostgreSQL mit Port `54322` und Benutzername/Passwort `docker`/`docker` eine Datenbank `gis` kann ich dies als mein Service `my-local-gis` konfigurieren.

```
# Lokale GIS Datenbank für Testzwecke
[my-local-gis]
host=localhost
port=54322
dbname=gis
user=docker
password=docker
```

Dieses Connection Service File heisst `pg_service.conf` und wird von den Client-Applikationen (wie psql oder QGIS) grundsätzlich direkt im Benutzerverzeichnis gesucht. Es heisst dann in Windows im Applikationsverzeichnis des Benutzers unter `postgresql\.pg_service.conf`. In Linux liegt es standardmässig direkt im Verzeichnis des Benutzers `~/.pg_service.conf`. Es muss aber nicht zwingend dort liegen. Es kann irgendwo auf dem System (oder einem Netzlaufwerk) liegen, solange man die Umgebungsvariable `PGSERVICEFILE` entsprechend konfiguriert:

```
export PGSERVICEFILE=/home/dave/connectionfiles/pg_service.conf 
```

Hat man das gemacht, wird von den Client-Applikationen zuerst dort gesucht - und gefunden.

Mit `PGSYSCONFDIR` kann auch ein Ordner definiert werden, wo das File `pg_service.conf` zu finden ist.

Hat man das, kann ein Service der Client-Applikation übergeben werden. Das heisst in psql (https://www.postgresql.org/docs/current/app-psql.html) würde das so aussehen:

```bash
~$ psql service=my-local-gis
psql (14.11 (Ubuntu 14.11-0ubuntu0.22.04.1), server 14.5 (Debian 14.5-1.pgdg110+1))
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

gis=# 
```

Und in QGIS so:
![image](https://gist.github.com/assets/28384354/2c80638f-8b1d-4281-8336-423d9db1b948)

Wenn ich dann in QGIS einen Layer hinzufüge, dann wird im Projektfile nur der Name des Services geschrieben. Weder die Verbindungsparameter noch Benutzername/Passwort ist gespeichert. Das hat neben dem Sicherheitsaspekt verschiedene Vorteile, mehr dazu weiter unten.

Man muss aber nicht alle diese Parameter einem Service übergeben, übergebe ich nur Teile davon (zBs. ohne die Datenbank), dann muss ich die beim Aufruf der Verbindung noch mitgeben:

```bash
$ psql "service=my-local-gis dbname=gis"
psql (14.11 (Ubuntu 14.11-0ubuntu0.22.04.1), server 14.5 (Debian 14.5-1.pgdg110+1))
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

gis=# 
```

Ich kann aber auch Parameter übersteuern. Wenn ich eine Datenbank `gis` im Service konfiguriert habe, aber auf die Datenbank `web` zugreiffen will, kann ich Service und explizite Datenbank angeben:
```bash
$ psql "service=my-local-gis dbname=web"
psql (14.11 (Ubuntu 14.11-0ubuntu0.22.04.1), server 14.5 (Debian 14.5-1.pgdg110+1))
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

web=# 
```

Das gleiche gilt natürlich auch für QGIS.

Und betreffend den genannten Umgebungsvariablen. Ich kann mir auch einen Standard-Service setzen.

```
export PGSERVICE=my-local-gis
```

Besonders angenehm in der täglichen Arbeit mit immer derselben Datenbank.

```bash
$ psql
psql (14.11 (Ubuntu 14.11-0ubuntu0.22.04.1), server 14.5 (Debian 14.5-1.pgdg110+1))
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

gis=# 
```

## Und weshalb ist es besonders cool?

Es gibt verschiedene Gründe, weshalb ein solches File nützlich ist:
- Sicherheit: Man muss die Verbindungsparameter nirgendwo in den Client Files (sBs. QGIS Projektfiles) abspeichern
- Abkopplung: Man kann die Verbindungsparameter ändern, ohne die Settings in Client Files (sBs. QGIS Projektfiles) ändern zu müssen
- Multi-User: Man kann das File auf einem Netzlaufwerk abspeichern. Solange die Umgebungsvariable der lokalen Systeme auf dieses File zeigt, können alle Benutzer mit den gleichen Logins darauf zugreiffen.
- Diversität: Man kann mit demselben Projektfile auf verschiedene Datenbanken mit gleicher Struktur zugreiffen, wenn lediglich der Name des Services gleich bleibt.

Zum letzten Grund hier dre Use Cases.

### Support-Case

Jemand meldet uns ein Problem in QGIS bei einem spezifischen Fall mit ihrer Datenbank. Da das Problem nicht zu reproduzieren ist, schicken sie uns einen DB-Dump eines Schemas und ein QGIS Projektfile. Die Layer im QGIS Projektfile sind einem Service verknüpft. Nun kann ich den Dump auf meiner lokalen Datenbank wiederherstellen und mit meinem eigenen Service darauf zugreiffen. Das Problem kann reproduziert werden.

### INTERLIS

Mit INTERLIS wird die Struktur eines Datenbankschemas genau spezifiziert. Wenn nun der Kanton dieses Modell bei sich erstellt hat und ein QGIS Projekt dazu konfiguriert, kann er das Projektfile einer Firma weitergeben, ohne auch die Datenbankstruktur mitzugeben. Die Firma kann auf ihrer eigenen PostgreSQL Datenbank das Schema aufgrund des INTERLIS Modells aufbauen und anhand ihres eigenen Services mit gleichem Namen darauf zugreiffen.

### Test/Prod Switching
Man kann mit demselben QGIS Projekt auf eine Test und eine Produktivdatenbank zugreiffen, wenn man pro QGIS Profil (https://docs.qgis.org/3.34/de/docs/user_manual/introduction/qgis_configuration.html#user-profiles) die Umgebungsvariable für das Connection Service File anders setzt.

Man erstellt zwei Connection Service Files. 

Das zur Testdatenbank in `/home/dave/connectionfiles/test/pg_service.conf`:

```
[my-local-gis]
host=localhost
port=54322
dbname=gis-test
```

Und das für die Produktivdatenbank in `/home/dave/connectionfiles/prod/pg_service.conf`:

```
[my-local-gis]
host=localhost
port=54322
dbname=gis-produktiv
```

In QGIS erstellt man zwei Profile "Test" und "Prod":
![image](https://gist.github.com/assets/28384354/ffef9560-336e-4cd5-9df8-a8522dd807f9)

Und pro Profil setzt man die Umgebungsvariable `PGSERVICEFILE` die verwendet werden soll (im Menu *Settings > Options...* und dort unter *System* herunterscrollen bis *Environment*

![image](https://gist.github.com/assets/28384354/b4ff78b9-05bc-4f07-99d0-46a42dff3a54)

bzw.

![image](https://gist.github.com/assets/28384354/d84322e8-fb26-4df6-9931-987d966c098c)

Wenn ich nun in QGIS auf den Service `my-local-gis` zugreiffe, verbindet es mir im Profil "Prod" mit der Datenbank `prod` und im Profil "Test" mit der Datenbank `test`.


### Die Authentifizierungskonfiguration

Nun noch zur Authentifizierung. Hat man das Connection Service File auf einem Netzlaufwerk und stellt es mehreren Usern zur Verfügung, möchte man ja vielleicht eher nicht, dass alle mit demselben Login zugreiffen. Oder man möchte generell keine Benutzerinformation in diesem File drin haben. Das lässt sich in QGIS elegant mit der Authentifizierungskonfiguration kombinieren.

Möchte man eine QGIS Projektfile mehreren Usern zur Verfügung stellen, erstellt man die Layer mit einem Service. Dieser Service enthält alle Verbindungsparameter, bis auf die Login-Information.

Diese Login-Information übergibt man mit einer QGIS Authentifizierung. 

![image](https://gist.github.com/assets/28384354/2e98763c-b84c-404c-9658-b8fb5a83e723)

Diese Authentifizierung konfiguriere ich ebenfalls pro obengenanntem QGIS-Profil. Dieses wird über Menu *Settings > Options...* und dort unter *Authetification* mit dem *+* erstellt:

![image](https://gist.github.com/assets/28384354/1e8600eb-4918-4396-b14e-2e3aed772d09)

(oder auch direkt dort, wo man die PostgreSQL Verbindung erstellt)

Wenn man so einen Layer hinzufügt, werden im QGIS Projektfile einerseits der Service wie auch die Id der Authentifizierungskonfiguration gespeichert. Diese ist in diesem Fall `mylogin` und muss natürlich den anderen Usern mitgeteilt werden, damit auch sie für ihr Login die Id `mylogin` konfigurieren.

Pro Profil kann man natürlich mehrere Authetifizierungkonfigurationen verwenden.

## QGIS Plugin

Übrigens gibt es neu ein tolles Plugin, um diese Services direkt in QGIS zu konfigurieren. So muss man sich nicht mehr mit Textbasierten INI-Files herumschlagen. Es heisst "PG service parser" (https://github.com/opengisch/qgis-pg-service-parser-plugin):

![image](https://gist.github.com/assets/28384354/9b9c7cc8-4015-4349-b9d2-020882f3d9cc)
