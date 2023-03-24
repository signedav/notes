Compared to setup just the normal project (see setup_real_project.md), this should be the environment built by travis as well to run tests etc.

## sudo vim ~/.pg_service.conf

Enter in sudo vim ~/.pg_service.conf:
```
[qgep_prod]
host=127.0.0.1
port=5432
dbname=qgep_prod
user=postgres

[qgep_test]
host=127.0.0.1
port=5432
dbname=qgep_test
user=postgres

[qgep_comp]
host=127.0.0.1
port=5432
dbname=qgep_comp
user=postgres

```

## Create Databases:
```
psql -c "CREATE DATABASE qgep;" -U postgres
psql -c "CREATE DATABASE qgep_prod;" -U postgres
psql -c "CREATE DATABASE qgep_test;" -U postgres
psql -c "CREATE DATABASE qgep_comp;" -U postgres

```

## Create Extenstions:
```
psql -c "CREATE EXTENSION postgis;" -U postgres -d qgep
psql -c "CREATE EXTENSION hstore;" -U postgres -d qgep
psql -c "CREATE EXTENSION postgis;" -U postgres -d qgep_prod
psql -c "CREATE EXTENSION hstore;" -U postgres -d qgep_prod
psql -c "CREATE EXTENSION postgis;" -U postgres -d qgep_test
psql -c "CREATE EXTENSION hstore;" -U postgres -d qgep_test
psql -c "CREATE EXTENSION postgis;" -U postgres -d qgep_comp
psql -c "CREATE EXTENSION hstore;" -U postgres -d qgep_comp
```

## Create user qgep (i don't do that (just using postgres)):
```
psql -c "CREATE GROUP qgep;" -U postgres
psql -c "CREATE ROLE qgepuser LOGIN;" -U postgres
psql -c "GRANT qgep TO qgepuser;" -U postgres
```

## Install with script:

```
scripts/db_setup.sh -r
```

## Pum Stuff 

```
export VERSION=1.1.2

 # Create a db from a dump file. This simulate the prod db
pum restore -p qgep_prod -x --exclude-schema public --exclude-schema topology -- test_data/qgep_demodata_1.0.0.dump
pum baseline -p qgep_prod -t qgep_sys.pum_info -d delta/ -b 1.0.0

# Create last version of qgep db using db_setup.sh script as comp db
export PGSERVICE=qgep_comp
scripts/db_setup.sh
pum baseline -p qgep_comp -t qgep_sys.pum_info -d delta/ -b $VERSION

# Run pum's test and upgrade
yes | pum test-and-upgrade -pp qgep_prod -pt qgep_test -pc qgep_comp -t qgep_sys.pum_info -d delta/ -f /tmp/qwat_dump -i constraints views --exclude-schema public -v int SRID 2056

# Run tests on qgep_prod
export PGSERVICE=qgep_prod
nosetests -e test_views.py
```

And nothing works... damn...