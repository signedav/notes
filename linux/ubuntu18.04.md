# Postgres

Tried this https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-18-04

> Then there has been trouble, I checked out this: https://stackoverflow.com/questions/42653690/psql-could-not-connect-to-server-no-such-file-or-directory-5432-error
Where it then said I have to do that
```
sudo systemctl start postgresql@10-main
```
and then it worked.

Anyway this `service postgresql@10-main restart` is to be used instad of that `service postgresql restart`

## Some config
Checked `/etc/postgresql/10/main/pg_hba.conf`
Hast to be changed to this:
```
# Database administrative login by Unix domain socket
local   all             postgres                                trust

# TYPE  DATABASE        USER            ADDRESS                 METHOD

# "local" is for Unix domain socket connections only
local   all             all                                     trust
# IPv4 local connections:

```

and ` /home/david/.pgpass`
and it was fine like this:
```
127.0.0.1:5432:*:postgres:postgres
```
but the `.pgpass` file needs to be `sudo chmod 600 /home/david/.pgpass`

## Setting up QGEP and see what's missing
Before I changed pg_hba.conf I got this...
```
david@david-ThinkPad-E580:~$ psql -c "CREATE DATABASE qgep_prod;" -U postgres
psql: FATAL:  Peer authentication failed for user "postgres"
```
Then I changed it like above to trust...
```
david@david-ThinkPad-E580:~$ sudo service postgresql@10-main restart
david@david-ThinkPad-E580:~$ psql -c "CREATE DATABASE qgep_prod;" -U postgres
CREATE DATABASE
```
and it worked...

## Postgis 
```
sudo apt install postgis
```

## PGAdmin
**I did not installed this stupid pgadmin3**

```
sudo apt install pgadmin3
```
Could not connect first to localhost with `postgres`

so I did:
```
sudo -u postgres psql
```
and there:
```
ALTER USER postgres PASSWORD 'postgres';
```

## QGEP connection
See *Appendix A* for everything...

# Chrome 
Let's try out Chromium:
```
sudo apt-get install chromium-browser
```

# GIT
Source: https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/
```
ssh-keygen -t rsa -b 4096 -C "david@opengis.ch"
```
(Passphrase ist usual Password)

Für proper Copy to Clipboard:
```
sudo apt-get install xclip
xclip -sel clip < ~/.ssh/id_rsa.pub

```
Then clone of my branch of qgis e.g.

`.git/config` for qgis/QGIS:
```
[core]
        repositoryformatversion = 0
        filemode = true
        bare = false
        logallrefupdates = true
[remote "origin"]
        url = git@github.com:QGIS/QGIS.git
        fetch = +refs/heads/*:refs/remotes/origin/*
[remote "ghdave"]
        url = git@github.com:signedav/QGIS.git
        fetch = +refs/heads/*:refs/remotes/origin/*
[branch "master"]
        remote = origin
        merge = refs/heads/master
```


# QGIS
https://linuxhint.com/install-qgis3-geospatial-ubuntu/


# Probleme 

#

```
david@david-ThinkPad-T470:~/dev/QGEP/datamodel$ sudo apt install vim
[...]
Unpacking vim-runtime (2:8.0.1453-1ubuntu1) ...
E: Sub-process /usr/bin/dpkg received a segmentation fault.
```

```
david@david-ThinkPad-T470:~/dev/QGEP/datamodel$ sudo apt-get install vim
E: dpkg was interrupted, you must manually run 'sudo dpkg --configure -a' to correct the problem. 
```

```
david@david-ThinkPad-T470:~/dev/QGEP/datamodel$ sudo dpkg --configure -a
Processing triggers for man-db (2.8.3-2ubuntu0.1) ...
dpkg: error processing package vim-runtime (--configure):
 package is in a very bad inconsistent state; you should
 reinstall it before attempting configuration
Errors were encountered while processing:
 vim-runtime
```
und dann noch einige versuche mehr. Lösung dann:

```
david@david-ThinkPad-T470:~/dev/QGEP/datamodel$ sudo dpkg --remove --force-remove-reinstreq vim-runtime
david@david-ThinkPad-T470:~/dev/QGEP/datamodel$ sudo apt-get install vim-runtime
david@david-ThinkPad-T470:~/dev/QGEP/datamodel$ sudo apt-get install vim
```


# Appendix

## Appendix A
The postgres installation history
```
    6  sudo apt update
    7  sudo apt install postgresql postgresql-contrib 
    8  /usr/lib/postgresql/10/bin/pg_ctl -D /var/lib/postgresql/10/main -l logfile start
    9  psql
   10  cd ..
   11  cd david/
   12  psql -u postgres
   13  service postgresql status
   14  systemctl status postgresql.service 
   15  systemctl restart  postgresql.service 
   16  systemctl status postgresql.service 
   17  sudo service postgresql restart
   18* sudo service postgresql res
   19  sudo service postgresql@10-main status
   20  pg_lsclusters
   21  sudo find / -name "pg_hba.conf"
   22  sudo vim /etc/postgresql/10/main/pg_hba.conf
   23  psql -c "CREATE DATABASE qgep_prod;" -U postgres
   24  sudo vim /etc/postgresql/10/main/pg_hba.conf
   25  psql -c "CREATE DATABASE qgep_prod;" -U postgres
   26  sudo vim /etc/postgresql/10/main/pg_hba.conf
   27  psql -c "CREATE DATABASE qgep_prod;" -U postgres
   28  sudo vim /etc/postgresql/10/main/pg_hba.conf
   29  sudo service postgresql@10-main restart
   30  psql -c "CREATE DATABASE qgep_prod;" -U postgres
   31  service postgresql@10-main restart
   32  sudo apt install postgis
   33  psql -c "CREATE EXTENSION postgis;" -U postgres -d qgep
   34  psql -c "CREATE EXTENSION postgis;" -U postgres -d qgep_prod
   35  psql -c "CREATE EXTENSION hstore;" -U postgres -d qgep
   36  psql -c "CREATE EXTENSION hstore;" -U postgres -d qgep_prod
   37  sudo vim ~/.pg_service.conf
   38  cd Downloads/
   39  pg_restore --host 127.0.0.1 --port 5432 --username "postgres" --dbname "qgep" --no-password  --create --clean qgep_v1.1.1_structure_and_demo_data.backup
   40  pg_restore --host 127.0.0.1 --port 5432 --username "postgres" --dbname "qgep_prod" --no-password  --create --clean qgep_v1.1.1_structure_and_demo_data.backup
   41  cd ..
   42  sudo vim ~/.pgpass
   43  sudo find / -name ".pgpass"
   44  sudo find / -name "pgpass"
   45  sudo vim ~/.pgpass
   46  sudo find / -name "pgpass"
   47  sudo find / -name ".pgpass"
   48  cd Downloads/
   49  pg_restore --host 127.0.0.1 --port 5432 --username "postgres" --dbname "qgep_prod" --no-password  --create --clean qgep_v1.1.1_structure_and_demo_data.backup
   50  sudo -u postgres psql
   51  pg_restore --host 127.0.0.1 --port 5432 --username "postgres" --dbname "qgep_prod" --no-password  --create --clean qgep_v1.1.1_structure_and_demo_data.backup
   52  pg_restore --host 127.0.0.1 --port 5432 --username "postgres" --dbname "qgep_prod" --create --clean qgep_v1.1.1_structure_and_demo_data.backup
   53  cd ..
   54  cd dev/
   55  ls -l
   56  mkdir QGEP
   57  cd QGEP/
   58  git clone git@github.com:QGEP/datamodel.git
   59  cd datamodel/
   60  git checkout geometry_altitude 
   61  ls -l
   62  wget -qO - https://download.sublimetext.com/sublimehq-pub.gpg | sudo apt-key add -
   63  echo "deb https://download.sublimetext.com/ apt/stable/" | sudo tee /etc/apt/sources.list.d/sublime-text.list
   64  sudo apt-get update
   65  sudo apt-get install sublime-text
   66  subl delta/delta_1.1.2_*
   67  psql "service=pg_qgep" -v ON_ERROR_STOP=1 -f after_dump_script.sql 
   68  chmod u=rw /home/david/.pgpass
   69  ls -l /home/david/.pgpass
   70  chmod 600 /home/david/.pgpass
   71  sudo chmod 600 /home/david/.pgpass
   72  ls -l /home/david/.pgpass
   73  nosetests
   74  sudo apt install python-nose
   75  nosetests
   76  pip install psudo pip install psycopg2
   77  sudo pip install psycopg2
   78  sudo apt install python-pip
   79  sudo pip install psycopg2
   80* pip install 
   81  nosetests
   82  psql -c "SELECT TOP 10 * from qgep_od.reach" -U postgres -d qgep_prod
   83  psql -c "SELECT TOP 10 from qgep_od.reach" -U postgres -d qgep_prod
   84  psql -c "SELECT * from qgep_od.reach limit 10" -U postgres -d qgep_prod
   85  pgpass
   86  vim ~/.pgpass
   87  sudo vim ~/.pgpass
   88  sudo rm ~/.pgpass
   89  vim ~/.pgpass
   90  nosetests
   91  sudo vim /etc/postgresql/10/main/pg_hba.conf
   92  nosetests
   93  sudo vim /etc/postgresql/10/main/pg_hba.conf
   94  sudo -u postgres psql postgres
   95  nosetests
   96  pip install psycopg2-binary
   97  nosetests
   98  sudo chmod 600 /home/david/.pgpass
   99  nosetests

```