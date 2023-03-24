```
    1  if ! test -x /usr/bin/qmake; then   echo "error";   exit 1; fi
    2  qmake --version
    3  apt show postgresql
    4  sudo apt update 
    5  apt list --upgradable 
    6  sudo -u postgres psql
    7  sudo apt update 
    8  apt list --upgradable
    9  apt upgrade gnome-shell
   10  sudo apt upgrade gnome-shell
   11  apt list --upgradable
   12  sudo apt install postgresql-11
   13  sudo systemctl status postgresql
   14  sudo -i -u postgres
   15  vim 
   16  sudo apt install vim
   17  vim /etc/postgresql/11/main/pg_hba.conf 
   18  sudo vim /etc/postgresql/11/main/pg_hba.conf 
   19  vim /home/david/.pgpass
   20  sudo vim /home/david/.pgpass
   21  vim /home/david/.pgpass
   22  ls -l
   23  sudo chmod 600 /home/david/.pgpass 
   24  psql
   25  psql -c "CREATE DATABASE fancy_db;" -U postgres
   26  service postgresql@11-main restart
   27  psql -c "CREATE DATABASE fancy_db;" -U postgres
   28  sudo apt install postgis
   30  sudo apt update 
   31  sudo apt install pgadmin4
   32  sudo apt install pgadmin4 pgadmin4-apache -y
   33  sudo apt-get install wget ca-certificates
   34  wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
   35  sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ `lsb_release -cs`-pgdg main" >> /etc/apt/sources.list.d/pgdg.list'
   37  sudo apt-get update 
   38  sudo apt install pgadmin4 pgadmin4-apache2
   39  pgadmin4 
   40  history 
   41  wget http://download.qt.io/official_releases/qt/5.14/5.14.1/qt-opensource-linux-x64-5.14.1.run
   42  wget http://download.qt.io/official_releases/qt/5.14/5.14.2/qt-opensource-linux-x64-5.14.2.run
   43  chmod +x qt-opensource-linux-x64-5.14.2.run 
   44  ./qt-opensource-linux-x64-5.14.2.run 
   45  gdalinfo --version
   46  sudo apt install gdal-bin 
   47  gdalinfo --version
   48  gdalinfo "WMS:http://sg.geodatenzentrum.de/wms_dop__77008511-bad8-d96e-4930-50d31caab5c3"
   49  gdal_translate "WMS:http://sg.geodatenzentrum.de/wms_dop__77008511-bad8-d96e-4930-50d31caab5c3?VERSION=1.1.0&request=GetMap&service=WMS&layers=rgb&srs=EPSG:25832" wms_01.xml -of WMS
   50  gdal_translate -of GPKG wms_001.xml wms_gpkg_JPEG.gpkg -co TILE_FORMAT=JPEG
   51  gdal_translate "WMS:http://sg.geodatenzentrum.de/wms_dop__77008511-bad8-d96e-4930-50d31caab5c3?VERSION=1.1.0&request=GetMap&service=WMS&layers=rgb&srs=EPSG:25832&bbox=468828,5276478,515401,5292771" wms_sh.xml -of WMS
   52  Input file size is 1073741824, 375635572
   53  gdal_translate -of GPKG wms_sh.xml wms_gpkg_JPEG.gpkg -projwin 468828 5292771 515401 5276478 -projwin_srs EPSG:25832 -co TILE_FORMAT=JPEG
   54  bim wms_sh.xml 
   55  vim wms_sh.xml 
   56  gdal_translate -of GPKG wms_sh.xml wms_gpkg_JPEG.gpkg -projwin 468828 5292771 515401 5276478 -projwin_srs EPSG:25832 -co TILE_FORMAT=JPEG
   57  gdalinfo "WMS:http://sg.geodatenzentrum.de/wms_dop__77008511-bad8-d96e-4930-50d31caab5c3"
   58  gdal_translate "WMS:http://sg.geodatenzentrum.de/wms_dop__77008511-bad8-d96e-4930-50d31caab5c3?VERSION=1.1.0&request=GetMap&service=WMS&layers=rgb&srs=EPSG:25832" wms_001.xml -of WMS
   59  gdal_translate -of GPKG wms_001.xml wms_001_gpkg_JPEG.gpkg -projwin 468828 5292771 515401 5276478 -projwin_srs EPSG:25832 -co TILE_FORMAT=JPEG
   60  vim wms_001.xml 
   61  gdal_translate -of GPKG wms_001.xml wms_001_gpkg_JPEG.gpkg -projwin -180 90 180 -90 -projwin_srs EPSG:25832 -co TILE_FORMAT=JPEG
   62  cmake --version
   63  sudo apt install cmake
   64  sudo apt install flex
   65  sudo apt install bison
   66  sudo install GEOS
   67  sudo apt install GEOS
   68  sudo apt search geos
   69  sudo apt search libgeos
   70  sudo apt install libgeos++-dev
   71  python3 --version
   72  sudo add-apt-repository ppa:ubuntugis/ppa
   73  sudo apt-get update
   74  sudo apt-get install gdal-bin 
   75  ogrinfo --version
   76  sudo apt install libgdal-dev
   77  export CPLUS_INCLUDE_PATH=/usr/include/gdal
   78  export C_INCLUDE_PATH=/usr/include/gdal
   79  ogrinfo --version
   80  pip install GDAL==2.4.2
   81  sudo apt install python-pip
   82  pip install GDAL==2.4.2
   83  sudo apt update -y
   84  sudo apt install -y libzip-dev 
   85  sudo apt install autoconf automake libtool curl make g++ unzip -y
   86  git clone https://github.com/google/protobuf.git
   87  cd protobuf/
   88  git submodule update --init --recursive
   89  ./autogen.sh 
   90  ./configure 
   91  make
   92  make check
   93  vim
   94  vim .ssh/
   95  ssh-keygen 
   96  ssh-keygen -t rsa -b 4096 "david@opengis.ch"
   97  ssh-keygen -t rsa -b 4096 -C "david@opengis.ch"
   98  ls .ssh/
   99  cd .ssh/
  100  ls -l
  101  vim id_rsa.pub 
  102  vim id_rsa
  103  vim id_rsa.pub 
  104  rm id_rsa.pub 
  105  ls -l
  106  cd ..
  107  ssh-keygen -y -f ~/.ssh/id_rsa > ~/.ssh/id_rsa.pub
  108  mkdir dev
  109  cd dev/
  110  mkdir qgis
  111  cd qgis/
  112  git clone git@github.com:signedav/QGIS.git
  113  sudo apt install git
  114  git status
  115  git init 
  116  git@github.com:signedav/QGIS.git
  117  git clone git@github.com:signedav/QGIS.git
  118  cd ..
  119  ssh-keygen -y -f ~/.ssh/id_rsa > ~/.ssh/id_rsa.pub
  120  cd dev/qgis/
  121  git clone git@github.com:signedav/QGIS.git
  122  cd QGIS/
  123  git status
  124  vim .git/config
  125  git status
  126  git log
  127  git pull upstream master
  128  git log
  129  git status
  130  git checkout -b fix_branch
  131  sudo apt search protobuf
  132  sudo install libprotobuf-dev
  133  sudo apt install libprotobuf-dev
  134  sudo apt install libprotoc-dev
  135  protobuf --version
  136  ./protoc --version
  137  sudo apt install protobuf-compiler
  138  sudo apt install mesa-common-dev libglu1-mesa-dev 
  139  sudo apt install libqt5webkit5-dev 
  140  sudo apt install libqt5webkit5
  141  sudo apt install libqca-qt5-2-dev 
  142  sudo apt install sip-dev 
  143  sudo apt remove sip-dev 
  144  sudo apt search sip
  145  sudo apt search sip-de
  146  sudo apt search libsip-de
  147  sudo apt search libsip
  148  sudo apt install sip-dev 
  149  sudo apt install python3-sip-dev 
  150  sudo apt search pyuic4
  151  sudo apt search pyuic5
  152  sudo apt install pyqt5-dev-tools 
  153  sudo apt install qt5keychain-dev 
  154  sudo apt install libspatialindex-dev 
  155  sudo apt search exiv
  156  sudo apt search libexiv
  157  sudo apt search libexiv2-dev
  158  sudo apt install libexiv2-dev
  159  ory in directory /home/david/dev/qgis/QGIS/src/gui
  160  sudo apt install libqscintilla2-qt5-dev
  161  sudo apt install libqscintilla2-dev
  162  sudo apt install libqwt-qt5-dev
  163  history
  164  rm -r protobuf/
  165  sudo rm -r protobuf/
  166  sudo apt remove libprotobuf-dev 
  167  history
  168  sudo apt remove libprotoc-dev
  169  sudo apt autoremove 
  170  history
  171  sudo apt remove protobuf-compiler
  172  sudo apt remove mesa-common-dev libglu1-mesa-dev libqt5webkit5-dev libqt5webkit5 libqca-qt5-2-dev sip-dev python3-sip-dev pyqt5-dev-tools qt5keychain-dev libspatialindex-dev libexiv2-dev libqscintilla2-qt5-dev libqscintilla2-dev libqwt-qt5-dev
  173  sudo apt-get update
  174  sudo apt-get install bison ca-certificates ccache cmake cmake-curses-gui dh-python doxygen expect flex flip gdal-bin git graphviz grass-dev libexiv2-dev libexpat1-dev libfcgi-dev libgdal-dev libgeos-dev libgsl-dev libpq-dev libproj-dev libprotobuf-dev libqca-qt5-2-dev libqca-qt5-2-plugins libqscintilla2-qt5-dev libqt5opengl5-dev libqt5serialport5-dev libqt5sql5-sqlite libqt5svg5-dev libqt5webkit5-dev libqt5xmlpatterns5-dev libqwt-qt5-dev libspatialindex-dev libspatialite-dev libsqlite3-dev libsqlite3-mod-spatialite libyaml-tiny-perl libzip-dev lighttpd locales ninja-build ocl-icd-opencl-dev opencl-headers pkg-config poppler-utils protobuf-compiler pyqt5-dev pyqt5-dev-tools pyqt5.qsci-dev python3-all-dev python3-autopep8 python3-dateutil python3-dev python3-future python3-gdal python3-httplib2 python3-jinja2 python3-lxml python3-markupsafe python3-mock python3-nose2 python3-owslib python3-plotly python3-psycopg2 python3-pygments python3-pyproj python3-pyqt5 python3-pyqt5.qsci python3-pyqt5.qtsql python3-pyqt5.qtsvg python3-pyqt5.qtwebkit python3-requests python3-sip python3-sip-dev python3-six python3-termcolor python3-tz python3-yaml qt3d-assimpsceneimport-plugin qt3d-defaultgeometryloader-plugin qt3d-gltfsceneio-plugin qt3d-scene2d-plugin qt3d5-dev qt5-default qt5keychain-dev qtbase5-dev qtbase5-private-dev qtpositioning5-dev qttools5-dev qttools5-dev-tools saga spawn-fcgi txt2tags xauth xfonts-100dpi xfonts-75dpi xfonts-base xfonts-scalable xvfb
  175  yes | sudo apt-get install bison ca-certificates ccache cmake cmake-curses-gui dh-python doxygen expect flex flip gdal-bin git graphviz grass-dev libexiv2-dev libexpat1-dev libfcgi-dev libgdal-dev libgeos-dev libgsl-dev libpq-dev libproj-dev libprotobuf-dev libqca-qt5-2-dev libqca-qt5-2-plugins libqscintilla2-qt5-dev libqt5opengl5-dev libqt5serialport5-dev libqt5sql5-sqlite libqt5svg5-dev libqt5webkit5-dev libqt5xmlpatterns5-dev libqwt-qt5-dev libspatialindex-dev libspatialite-dev libsqlite3-dev libsqlite3-mod-spatialite libyaml-tiny-perl libzip-dev lighttpd locales ninja-build ocl-icd-opencl-dev opencl-headers pkg-config poppler-utils protobuf-compiler pyqt5-dev pyqt5-dev-tools pyqt5.qsci-dev python3-all-dev python3-autopep8 python3-dateutil python3-dev python3-future python3-gdal python3-httplib2 python3-jinja2 python3-lxml python3-markupsafe python3-mock python3-nose2 python3-owslib python3-plotly python3-psycopg2 python3-pygments python3-pyproj python3-pyqt5 python3-pyqt5.qsci python3-pyqt5.qtsql python3-pyqt5.qtsvg python3-pyqt5.qtwebkit python3-requests python3-sip python3-sip-dev python3-six python3-termcolor python3-tz python3-yaml qt3d-assimpsceneimport-plugin qt3d-defaultgeometryloader-plugin qt3d-gltfsceneio-plugin qt3d-scene2d-plugin qt3d5-dev qt5-default qt5keychain-dev qtbase5-dev qtbase5-private-dev qtpositioning5-dev qttools5-dev qttools5-dev-tools saga spawn-fcgi txt2tags xauth xfonts-100dpi xfonts-75dpi xfonts-base xfonts-scalable xvfb
  176  cd /usr/local/bin
  177  sudo ln -s /usr/bin/ccache gcc
  178  sudo ln -s /usr/bin/ccache g++
  179  cd
  180  cd dev/
  181  ls -l
  182  cd qgis/
  183  ls -l
  184  cd QGIS/
  185  ls -l
  186  git status
  187  cd
  188  cd /usr/local/bin
  189  ls -l
  190  rm g++ 
  191  sudo rm g++ 
  192  sudo rm gcc 
  193  ls -l
  194  cd ..
  195  sudo apt install libqscintilla2-dev
  196  sudo apt autoremove 
  197  cd dev/
  198  cd qgis/
  199  git status
  200  cd QGIS/
  201  git status
  202  ls -l
  203  git checkout CMakeLists.txt
  204  rm CMakeLists.txt.user 
  205  cd 
  206  /usr/bin/qmake -b
  207  /usr/bin/qmake -v
  208  ls -l
  209  cd Qt5.14.2/
  210  ls -l
  211  ./MaintenanceTool 
  212  ls -l
  213  cd ..
  214  ls -l
  215  ./qt-opensource-linux-x64-5.14.2.run 
  216  cc --version
```