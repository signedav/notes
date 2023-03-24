# Systemanforderungen
## Plattform
Grundsätzlich sollte PostgreSQL auf einer modernen Unix Plattform laufen können. Getestete Plattformen findet man auf der PostgreSQL-Build Farm:
https://buildfarm.postgresql.org/cgi-bin/show_status.pl
![buildfarm](images/installation_buildfarm.png)

## Installierte Programme (minimum)
- GNU make version 3.80 oder neuer. Check `make --version`
- C compiler wie zBs. GCC. Check `rpm -q gcc`
- tar um Sourcen zu entpacken
- GNU Readline library (ausser man deaktiviert es)
- zlib compression library (ausser man deaktiviert es)

## Speicherplatz
100 MB für die Kompilierung
20 MB für den Install-Ordner
Ein leeres DB-Cluster braucht ca. 35 MB.

# Installation anhand des Sources
## Download der Source
http: 	https://www.postgresql.org/download/
ftp:	ftp://ftp.postgresql.org/

Das File heisst postgresql-X.Y.Z.tar.gz wobei X.Y.Z die Version beschreibt.

## Kompilieren der Sourcen

```
$ tar zxvf postgresql-X.Y.Z.tar.gz
$ cd postgresql-X.Y.Z
$ ./configure [OPTIONS] 
$ make (oder gmake)
```
Extrahieren, konfigurieren, kompilieren

### Wichtigste Konfigurationsoptionen
```
--prefix=PREFIX
--with-pgport=NUMBER
--with-openssl
--enable-nls=[LANGUAGES]
--with-perl
--with-tcl
--bindir=DIRECTORY
```

Erklärung unter: https://www.postgresql.org/docs/9.6/static/install-procedure.html

Diese Konfiguration kann man aber auch später machen mit dem folgenden Kommando:
```
$ pg_config --configure
```

### Prüfen des Builds
```
$ make check
```
(oder gmake)

### Installation
```
$ make install
```
Ohne eine Konfiguration werden die Files in `/usr/local/pgsql` geschrieben.

### Post-Installation
#### Setzen der Environment Variablen:
Neu installierte Shared Libraries:
```
$ export LD_LIBRARY_PATH=/usr/local/pgsql/lib:$LD_LIBRARY_PATH
```
Pfad wo es installiert ist (evtl. gesetzt mit `--bindir=DIRECTORY` im `./configure`)
```
$ export PATH=/usr/local/pgsql/bin:$PATH
```
Pfad für Dokumentation:
```
$ export MANPATH=$MANPATH:/usr/local/pgsql/share/man
```
Pfad für das Arbeitsverzeichnis:
```
$ export PGDATA=/usr/local/pgsql/data
```
Um diese Variablen bei einem Neustart nicht zu verlieren, füge sie ein ins `/etc/profile` oder ins `${HOME}/.profile` (`~/.profile`)

#### Erstellen DB-Cluster
```
$ initdb [option...] [--pgdata | -D] /usr/local/pgsql/data
```

# Installation anhand der Binaries
## Verschiedene Distributionen
- Debian / Ubuntu -> deb Packete
- Fedora / Redhat / Suse -> RPM Packete

## Installation auf Debian / Ubuntu
### Installation PostgreSQL
```
$ sudo apt-get install postgresql-x.x
```
Wobei x.x die PostgreSQL-Version ist.

### Installation zusätzlicher Packete
Für Porting Tools, Analysis Utilities, and Plug-in Features, die nicht Bestandteil des PostgreSQL Cores sind:
```
$ sudo apt-get install postgresql-contrib
```
Um je nach dem in welcher Sprache geschrieben Prozeduren zu nutzen:
```
$ sudo apt-get install postgresql-plpython-x.x
$ sudo apt-get install postgresql-plperl-x.x
$ sudo apt-get install postgresql-pltcl-x.x
```
Für Entwicklungsbibliotheken:
```
$ sudo apt-get install postgresql-server-dev-x.x
```
## Installation auf Redhat / Fedora / Suse
### Installation PostgreSQL
```
$ sudo yum install postgresql-server postgresql-contrib
```
bzw.
```
$ sudo dnf install postgresql-server postgresql-contrib
```
### Erstellen DB-Cluster
Dies muss bei Fedora etc. gemacht werden (im Gegensatz zu Ubuntu / Debian)
```
$ sudo postgresql-setup initdb
```
Erstellt postgres.conf und  pg_hba.conf

# Dockerize PostgreSQL
## Installieren von Docker
```
$ sudo apt-get install docker-ce
$ sudo docker run hello-world<
```
## Starten eines offiziellen Postgres Containers
Vom Repository https://hub.docker.com/_/postgres/
```
$ docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres
```

## Aufsetzen eines eigenen Postgres Containers
1. Erstellen eines Dockerfiles:
![dockerfile](images/installation_dockerfile.png)
2. Builden den Containers:
```
$ docker build -t eg_postgresql .
```
3. Starten des Containers:
```
$ docker run --rm -P --name pg_test eg_postgresql
```
Mehr Info dazu:
https://docs.docker.com/engine/examples/postgresql_service/

