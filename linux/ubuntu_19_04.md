## PostgreSQL

```
sudo apt install postgresql-11
sudo systemctl status postgresql
sudo -i -u postgres
psql
>> no success
CTRL+d
sudo service postgresql start
pg_lsclusters
sudo systemctl start postgresql@11-main
sudo -i -u postgres
psql
\l
>> success
```
Then
```
sudo vim /etc/postgresql/11/main/pg_hba.conf 
```
and change it to:
```
# Database administrative login by Unix domain socket
local   all             postgres                                trust

# TYPE  DATABASE        USER            ADDRESS                 METHOD

# "local" is for Unix domain socket connections only
local   all             all                                     trust
```

Restart allways server...

## PostGIS
```
sudo apt install postgis
```

## PgAdmin 4
```
sudo apt-get install wget ca-certificates
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ `lsb_release -cs`-pgdg main" >> /etc/apt/sources.list.d/pgdg.list'
sudo apt-get update
sudo apt install pgadmin4 pgadmin4-apache2
```

Strange, that I couldn't create a server because my postgres login failed.
Well after connecting with `psql -U postgres` what does not request the password because its set to trust above, I just could make:
```
ALTER USER postgres PASSWORD 'postgres';
```
And then I didn't had issues logging in on PgAdmin anymore...


## GIT
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

## QT
https://wiki.qt.io/Install_Qt_5_on_Ubuntu
To install the QT I downloaded the `run` file from the official QT download page. I had to set it to executable and then I ran `./qt...run`
Fine. I had it then in the folder `home/signedav/Qt...` etc.

For QGIS we need QT 5.9 - on my other laptop it's installed in /usr/lib/qt5...

## Deactivate Touchscreen
Deactivate Touchscreen - it does not resolve the issue on the laptop...
```
xinput disable `xinput --list | egrep -o "ELAN Touchscreen.+id=[0-9]+" | egrep -o "[0-9]+"`
```
Otherwise add it to a startup script...

# Rubbish
```
    2  sudo apt-get update
   10  sudo apt update
   11  sudo apt install postgresql postgresql-contrib 
   19  postgres /usr/lib/postgresql/11/bin/pg_ctl -D /var/lib/postgresql/11/main -l logfile start
   >> this possibly did nothing...
   20  pg_ctl start -l logfile
   21  sudo -i -u postgres
   >> no success
   24  sudo service postgresql start
   25  pg_lsclusters
   26  pg_ctlcluster 11 main start
   27  postgres pg_ctlcluster 11 main start
   28  sudo -u postgres pg_ctlcluster 11 main start
   29  pg_lsclusters
   30  sudo systemctl status postgresql
   31  sudo -i -u postgres
   >> success
```
Then
```
sudo vim /etc/postgresql/11/main/pg_hba.conf 
```

# Database administrative login by Unix domain socket
local   all             postgres                                trust

# TYPE  DATABASE        USER            ADDRESS                 METHOD

# "local" is for Unix domain socket connections only
local   all             all                                     trust
```
Btw. I couldn't find `.pgpass`

