## That's how I did it in November 25 (remember, remember)

Of course:
```
cmake -DWITH_SERVER=TRUE ../QGIS
```

``` 
/home/dave/dev/qgis/QGIS/build/Desktop-Debug/output/bin/qgis_mapserv.fcgi

/home/dave/dev/qgis/QGIS/build/Desktop-Debug/output/bin/qgis_mapserv.fcgi --version
```

Now I created the local webservers stuff like [here](qgis-local-server-notes.md)

Ant the specific commands are:

sudo apt install apache2 libapache2-mod-fcgid

``` 
service apache2 start
``` 
``` 
sudo service lighttpd stop
``` 
``` 
sudo systemctl disable lighttpd
``` 
``` 
service apache2 start
``` 
``` 
vim /etc/apache2/sites-available/qgis.demo.conf
``` 
< Inhalt abgefüllt und Pfade angepasst...
``` 
sudo mkdir /var/log/qgis/
sudo chown www-data:www-data /var/log/qgis
mkdir /home/dave/qgis_server
mkdir /home/dave/qgis_server/qgisserverdb
sudo chown www-data:www-data /home/dave/qgis_server/qgisserverdb
``` 
``` 
a2enmod fcgid
``` 
``` 
sudo a2ensite qgis.demo
``` 
``` 
systemctl reload apache2
``` 
``` 
sudo sh -c "echo '127.0.0.1 qgis.demo' >> /etc/hosts"
``` 

``` 
curl http://qgis.demo/cgi-bin/qgis_mapserv.fcgi
``` 
< ohne Erfolg "You don't have permission to access this resource."

Dann musste ich die Berechtigungen auf alle Directories setzen:
``` 
sudo chmod +x /home/dave /home/dave/dev /home/dave/dev/qgis /home/dave/dev/qgis/QGIS2 /home/dave/dev/qgis/QGIS2/build /home/dave/dev/qgis/QGIS2/build/Desktop-Debug /home/dave/dev/qgis/QGIS2/build/Desktop-Debug/output /home/dave/dev/qgis/QGIS2/build/Desktop-Debug/output/bin/
```
Dann ist est gegangen. Braucht halt so MAP Parameter...

```
curl "http://qgis.demo/cgi-bin/qgis_mapserv.fcgi?MAP=/home/dave/dev/qgis/qgis-docker/server/test/data/exclude-test/exclude-test.qgs&SERVICE=WMS&REQUEST=GetCapabilities"
```

Und dann auch im Browser:
```
http://qgis.demo/cgi-bin/qgis_mapserv.fcgi?MAP=/home/dave/dev/qgis/qgis-docker/server/test/data/exclude-test/exclude-test.qgs&SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&BBOX=47.03213800000000333%2C8.362049999999999983%2C47.03285600000000244%2C8.363277999999999324&CRS=EPSG%3A4326&WIDTH=935&HEIGHT=548&LAYERS=Strassen&STYLES=&FORMAT=image%2Fjpeg&DPI=96&MAP_RESOLUTION=96&FORMAT_OPTIONS=dpi%3A96
```

--- 

These are the old notes...

To use qgisserver the qgis has to be compiled as server.

in build folder: 
```
cmake -DWITH_SERVER=TRUE ../QGIS
```

And then you can test like usual...

see for setup QGIS server qgis-local-server-notes.md