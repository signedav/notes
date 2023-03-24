### 1. Postgresql

```
sudo apt install postgresql-12
```
```
sudo vim /etc/postgresql/12/main/pg_hba.conf
```
```
local   all             postgres                                trust
local   all             all                                     trust
```
```
sudo apt install postgis
```
```
service postgresql restart 
psql -c "CREATE DATABASE fancy_db;" -U postgres
```
```
vim /home/dave/.pgpass
```
```
127.0.0.1:5432:*:postgres:postgres
```
```
sudo chmod 600 /home/dave/.pgpass 
```
Somehow the password always has been wrong (could connect by psql but not with QGIS and pgadmin) So I needed to change it:
```
sudo su - (not sure if needed)
psql postgres postgres
\password postgres
```
And restarted postgres...

### 2. ssh key
```
cd .ssh/
ssh-keygen -t rsa -b 4096 -C "david@opengis.ch"
```
Generiert ein neues File, aber mit richtigen Berechtigungen etc. Passphrase ist egal hier.

```
vim id_rsa
```
Dort den Key aus dem Gitlab company repo kopieren. 
```
ssh-keygen -y -f ~/.ssh/id_rsa > ~/.ssh/id_rsa.pub
```
Public key generieren. Da der Passphrase wohl im hineinkopierten Key ist, ist das Passwort das alte generelle passwort the days with !

### 3. QGIS 

Follow instructions here: https://github.com/qgis/QGIS/blob/master/INSTALL

For installing QGIS Master then, I build it with:
```
cmake -GNinja -D CMAKE_INSTALL_PREFIX=/home/dave/qfield_devenv/qgis_masterbuild
```
Then 
```
ninja
ninja install
```
Then if needed
```
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/dave/qfield_devenv/qgis_masterbuild/lib
export PATH=$PATH:/home/dave/qfield_devenv/qgis_masterbuild/bin
```
And the QGIS LTR I choose just `/usr` as prefix

#### 4. Was für es QT?
I make:
```
python3
>>> 
import inspect
from PyQt5 import Qt
vers = ['%s = %s' % (k,v) for k,v in vars(Qt).items() if k.lower().find('version') >= 0 and not inspect.isbuiltin(v)]
print('\n'.join(sorted(vers)))
```
And I get: 
```
[...]
PYQT_VERSION_STR = 5.14.1
[...]
```

#### 5. Git 
```
vim .git/config
```
```
[core]
        repositoryformatversion = 0
        filemode = true
        bare = false
        logallrefupdates = true
[remote "qgis"]
        url = git@github.com:QGIS/QGIS.git
        fetch = +refs/heads/*:refs/remotes/origin/*
[remote "ghdave"]
        url = git@github.com:signedav/QGIS.git
        fetch = +refs/heads/*:refs/remotes/origin/*
[branch "master"]
        remote = qgis

```

#### 6. PyCharm

The repository of mystic-mirage is obsolete. I could have done it with `umake`, but I did not:
```
sudo add-apt-repository ppa:ubuntu-desktop/ubuntu-make
sudo apt-get update
sudo apt-get install ubuntu-make
--remove it
umake -r ide pycharm
```
I downloaded it from the official pycharm site and did:
```
tar -xzf pycharm-community-2020.1.1.tar.gz
cd pycharm-community-2020.1.1/
cd bin
sh pycharm.sh
```
... and created a Desktop Entry...

```

