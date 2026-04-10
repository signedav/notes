# QGIS Server from Package

I follow the instruction of this here: https://docs.qgis.org/testing/en/docs/training_manual/qgis_server/install.html
```
david@david-ThinkPad-E580:~$ sudo find / -name 'qgis_mapserv.fcgi'
[sudo] password for david: 
find: ‘/run/user/1000/gvfs’: Permission denied
/usr/lib/cgi-bin/qgis_mapserv.fcgi
```
and
```
david@david-ThinkPad-E580:~$ /usr/lib/cgi-bin/qgis_mapserv.fcgi
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver JP2ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver JP2ECW to unload from GDAL_SKIP environment variable.
Initializing server modules from  "/usr/lib/qgis/server" 

"Checking /usr/lib/qgis/server for native services modules"
"Loading native module /usr/lib/qgis/server/libdummy.so"
"Loading native module /usr/lib/qgis/server/libwcs.so"
"Loading native module /usr/lib/qgis/server/libwfs.so"
"Loading native module /usr/lib/qgis/server/libwms.so"
"Loading native module /usr/lib/qgis/server/libwmts.so"
QFileInfo::absolutePath: Constructed with empty filename
QFSFileEngine::open: No file name specified
Content-Length: 54
Content-Type: text/xml; charset=utf-8
Server:  Qgis FCGI server - QGis version 3.4.4-Madeira
Status:  500

<ServerException>Project file error</ServerException>
```

### Bernasocchis command
```
QUERY_STRING="MAP=/home/david/qgis_projects/server_test/server_test2.qgs&SERVICE=WMS&REQUEST=GetCapabilities" /usr/lib/cgi-bin/qgis_mapserv.fcgi
```
Returns
```
david@david-ThinkPad-E580:~$ QUERY_STRING="MAP=/home/david/qgis_projects/server_test/server_test2.qgs&SERVICE=WMS&REQUEST=GetCapabilities" /usr/lib/cgi-bin/qgis_mapserv.fcgi
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver JP2ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver JP2ECW to unload from GDAL_SKIP environment variable.
Initializing server modules from  "/usr/lib/qgis/server" 

"Checking /usr/lib/qgis/server for native services modules"
"Loading native module /usr/lib/qgis/server/libdummy.so"
"Loading native module /usr/lib/qgis/server/libwcs.so"
"Loading native module /usr/lib/qgis/server/libwfs.so"
"Loading native module /usr/lib/qgis/server/libwms.so"
"Loading native module /usr/lib/qgis/server/libwmts.so"
Content-Length: 217
Content-Type: text/xml; charset=utf-8
Server:  Qgis FCGI server - QGis version 3.4.4-Madeira
Status:  200

<ServiceExceptionReport xmlns="http://www.opengis.net/ogc" version="1.3.0">
 <ServiceException code="OperationNotSupported">Please check the value of the REQUEST parameter</ServiceException>
</ServiceExceptionReport>
```
not sure if this is that bad...


# Building QGIS Server

Add in cmake
```
cmake -DWITH_SERVER=TRUE ../QGIS
```

# Apache and friends
When I follow the instruction of this here: https://docs.qgis.org/testing/en/docs/training_manual/qgis_server/install.html

## Install Apache
```
get install apache2 libapache2-mod-fcgid
```

> **lighttpd running**
> It says:
> ```
> Feb 14 15:29:23 david-ThinkPad-E580 apachectl[22273]: (98)Address already in use: AH00072: make_sock: could not bind to address [::]:80
> Feb 14 15:29:23 david-ThinkPad-E580 apachectl[22273]: (98)Address already in use: AH00072: make_sock: could not bind to address 0.0.0.0:80
> Feb 14 15:29:23 david-ThinkPad-E580 systemd[1]: apache2.service: Control process exited, code=exited status=1
> Feb 14 15:29:23 david-ThinkPad-E580 systemd[1]: apache2.service: Failed with result 'exit-code'.
> Feb 14 15:29:23 david-ThinkPad-E580 systemd[1]: Failed to start The Apache HTTP Server.
> ```
> and I belief because there is another serverli somewhere. So I did:
> ```
> david@david-ThinkPad-E580:~$ sudo service lighttpd stop
> david@david-ThinkPad-E580:~$ sudo systemctl disable lighttpd
> ```
> and start apache2 again:
> ```
> service apache2 start
> ```

## Virtual Host
Follow the instruction:
Create the qgis.demo.conf:
```
david@david-ThinkPad-E580:~$ sudo vim /etc/apache2/sites-available/qgis.demo.conf
```
looks like this:
```
<VirtualHost *:80>
  ServerAdmin webmaster@localhost
  ServerName qgis.demo

  DocumentRoot /var/www/html

  # Apache logs (different than QGIS Server log)
  ErrorLog ${APACHE_LOG_DIR}/qgis.demo.error.log
  CustomLog ${APACHE_LOG_DIR}/qgis.demo.access.log combined

  # Longer timeout for WPS... default = 40
  FcgidIOTimeout 120

  FcgidInitialEnv LC_ALL "en_US.UTF-8"
  FcgidInitialEnv PYTHONIOENCODING UTF-8
  FcgidInitialEnv LANG "en_US.UTF-8"

  # QGIS log (different from apache logs) see https://docs.qgis.org/testing/en/docs/user_manual/working_with_ogc/ogc_server_support.html#qgis-server-logging
  FcgidInitialEnv QGIS_SERVER_LOG_FILE /var/log/qgis/qgisserver.log
  FcgidInitialEnv QGIS_SERVER_LOG_LEVEL 0

  FcgidInitialEnv QGIS_DEBUG 1

  # default QGIS project
  # FcgidInitialEnv QGIS_PROJECT_FILE /home/david/qgis_projects/server_test/server_test.qgs

  # QGIS_AUTH_DB_DIR_PATH must lead to a directory writeable by the Server's FCGI process user
  FcgidInitialEnv QGIS_AUTH_DB_DIR_PATH "/home/david/qgis_server/qgisserverdb/"
  FcgidInitialEnv QGIS_AUTH_PASSWORD_FILE "/home/david/qgis_server/qgisserverdb/qgis-auth.db"

  # See https://docs.qgis.org/testing/en/docs/user_manual/working_with_vector/supported_data.html#pg-service-file
  SetEnv PGSERVICEFILE /home/david/.pg_service.conf
  FcgidInitialEnv PGPASSFILE "/home/david/.pgpass"

  # Tell QGIS Server instances to use a specific display number
  #FcgidInitialEnv DISPLAY ":99"

  # if qgis-server is installed from packages in debian based distros this is usually /usr/lib/cgi-bin/
  # run "locate qgis_mapserv.fcgi" if you don't know where qgis_mapserv.fcgi is
  ScriptAlias /cgi-bin/ /home/david/dev/qgis/build-QGIS-Desktop-Debug/output/bin/
  <Directory "/home/david/dev/qgis/build-QGIS-Desktop-Debug/output/bin/">
    AllowOverride None
    Options +ExecCGI -MultiViews -SymLinksIfOwnerMatch
    Order allow,deny
    Allow from all
    Require all granted
  </Directory>

 <IfModule mod_fcgid.c>
 FcgidMaxRequestLen 26214400
 FcgidConnectTimeout 60
 </IfModule>

</VirtualHost>
```

Betreffend: `# FcgidInitialEnv QGIS_PROJECT_FILE /home/david/qgis_projects/server_test/server_test.qgs` this would take always this project without `MAP` parameter. But other projects cannot be requested.

And the folders:
```
david@david-ThinkPad-E580:~$ sudo mkdir /var/log/qgis/
david@david-ThinkPad-E580:~$ sudo chown www-data:www-data /var/log/qgis
```

```
david@david-ThinkPad-E580:~$ mkdir /home/david/qgis_server
david@david-ThinkPad-E580:~$ mkdir /home/david/qgis_server/qgisserverdb
david@david-ThinkPad-E580:~$ sudo chown www-data:www-data  /home/david/qgis_server/qgisserverdb
```
## Enabling FCGI
```
a2enmod fcgid
sudo a2ensite qgis.demo
service apache2 restart
```

## Setting qgis.demo

And I set qgis.demo to `sudo vim /etc/hosts`

# Test it:

## Curl:
```
david@david-ThinkPad-E580:~$ curl http://qgis.demo/cgi-bin/qgis_mapserv.fcgi
<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
<html><head>
<title>500 Internal Server Error</title>
</head><body>
<h1>Internal Server Error</h1>
<p>The server encountered an internal error or
misconfiguration and was unable to complete
your request.</p>
<p>Please contact the server administrator at 
 webmaster@localhost to inform them of the time this error occurred,
 and the actions you performed just before this error.</p>
<p>More information about this error may be available
in the server error log.</p>
<hr>
<address>Apache/2.4.29 (Ubuntu) Server at qgis.demo Port 80</address>
</body></html>
```

## Curl on the built of QGIS Server
```
david@david-ThinkPad-E580:~$ curl http://qgis.demo/cgi-bin/qgis_mapserv.fcgi
<ServerException>Project file error</ServerException>
```
I assume the project is not configured correctly...

### In the browser I receive the same on
`http://qgis.demo/cgi-bin/qgis_mapserv.fcgi?MAP=/home/david/qgis_projects/server_test/server_test.qgsSERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`

## Bernasocchis command
```
QUERY_STRING="MAP=/home/david/qgis_projects/server_test/server_test.qgs&SERVICE=WMS&REQUEST=GetCapabilities" /usr/lib/cgi-bin/qgis_mapserv.fcgi
```
Returns
```
david@david-ThinkPad-E580:~$ QUERY_STRING="MAP=/home/david/qgis_projects/server_test/server_test.qgs&SERVICE=WMS&REQUEST=GetCapabilities" /usr/lib/cgi-bin/qgis_mapserv.fcgi 
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver JP2ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver ECW to unload from GDAL_SKIP environment variable.
Warning 1: Unable to find driver JP2ECW to unload from GDAL_SKIP environment variable.
Initializing server modules from  "/usr/lib/qgis/server" 

"Checking /usr/lib/qgis/server for native services modules"
"Loading native module /usr/lib/qgis/server/libdummy.so"
"Loading native module /usr/lib/qgis/server/libwcs.so"
"Loading native module /usr/lib/qgis/server/libwfs.so"
"Loading native module /usr/lib/qgis/server/libwms.so"
"Loading native module /usr/lib/qgis/server/libwmts.so"
Content-Length: 217
Content-Type: text/xml; charset=utf-8
Server:  Qgis FCGI server - QGis version 3.4.4-Madeira
Status:  200

<ServiceExceptionReport version="1.3.0" xmlns="http://www.opengis.net/ogc">
 <ServiceException code="OperationNotSupported">Please check the value of the REQUEST parameter</ServiceException>
</ServiceExceptionReport>
```
they say it's good

## Bernasocchis command on the built of QGIS Server
```
david@david-ThinkPad-E580:~$ QUERY_STRING="MAP=/home/david/qgis_projects/server_test/server_test.qgs&SERVICE=WMS&REQUEST=GetCapabilities" /home/david/dev/qgis/build-QGIS-Desktop-Debug/output/bin/qgis_mapserv.fcgi
```
Brings an XML with info, looks better, don't know why...

# And this works:

In the browser directly `http://qgis.demo/cgi-bin/qgis_mapserv.fcgi?MAP=/home/david/qgis_projects/server_test/server_test.qgs&SERVICE=WMS&REQUEST=GetCapabilities`

Or as curl:
```
curl "http://qgis.demo/cgi-bin/qgis_mapserv.fcgi?MAP=/home/david/qgis_projects/server_test/server_test.qgs&SERVICE=WMS&REQUEST=GetCapabilities"
```

# Multiple hosts:

I made addionally file `qgis.build_34.conf` and `qgis.build_36.conf` and configured there the paths to my backport builds.
And added it here in the `/etc/hosts`
```
127.0.0.1       localhost qgis.demo
127.0.0.1       localhost qgis.build_34
127.0.0.1       localhost qgis.build_36
```
But it does not work actually. But I did not invest more time...

## GetLayerGraphic Request

A GetLayerGraphic Request looks like this:
`http://qgis.demo/cgi-bin/qgis_mapserv.fcgi?MAP=/home/david/qgis_projects/server_test/server_test.qgs&SERVICE=WMS&REQUEST=GetLegendGraphic&LAYER=wald&FORMAT=image/png&WIDTH=20&HEIGHT=20&legend_options=fontName:Times%20New%20Roman;fontAntiAliasing:true;fontColor:0x000033;fontSize:14;bgColor:0xFFFFEE;dpi:180`

Mehr dazu hier: https://gitlab.com/signenotes/opengisch/projects_notes/blob/master/quake_mapunits_GetLegend.md

##Notes, wie ich das QGIS_SERver mit qgis.demo konfigurierte:

```
http://qgis.demo/cgi-bin/qgis_mapserv.fcgi?MAP=/home/david/dev/qgis/QGIS_Server/tests/testdata/qgis_server/test_project.qgs&SERVICE=WMS&REQUEST=GetLegendGraphic&FORMAT=image/png&LAYER=testlayer%20%C3%A8%C3%A9&STYLE=default&SLD_VERSION=1.1.0

QUERY_STRING="MAP=/home/david/dev/qgis/QGIS_Server/tests/testdata/qgis_server/test_project.qgs&SERVICE=WMS&REQUEST=GetCapabilities" /home/david/dev/qgis/build-QGIS_Server-Desktop_Debug-Debug/output/bin/qgis_mapserv.fcgi

<VirtualHost *:80>
  ServerAdmin webmaster@localhost
  ServerName qgis.demo

  DocumentRoot /var/www/html

  # Apache logs (different than QGIS Server log)
  ErrorLog ${APACHE_LOG_DIR}/qgis.demo.error.log
  CustomLog ${APACHE_LOG_DIR}/qgis.demo.access.log combined

  # Longer timeout for WPS... default = 40
  FcgidIOTimeout 120

  FcgidInitialEnv LC_ALL "en_US.UTF-8"
  FcgidInitialEnv PYTHONIOENCODING UTF-8
  FcgidInitialEnv LANG "en_US.UTF-8"

  # QGIS log (different from apache logs) see https://docs.qgis.org/testing/en/docs/user_manual/working_with_ogc/ogc_server_support.html#qgis-server-logging
  FcgidInitialEnv QGIS_SERVER_LOG_FILE /var/log/qgis/qgisserver.log
  FcgidInitialEnv QGIS_SERVER_LOG_LEVEL 0

  FcgidInitialEnv QGIS_DEBUG 1

  # default QGIS project
  # FcgidInitialEnv QGIS_PROJECT_FILE /home/david/qgis_projects/server_test/server_test.qgs

  # QGIS_AUTH_DB_DIR_PATH must lead to a directory writeable by the Server's FCGI process user
  FcgidInitialEnv QGIS_AUTH_DB_DIR_PATH "/home/david/qgis_server/qgisserverdb/"
  FcgidInitialEnv QGIS_AUTH_PASSWORD_FILE "/home/david/qgis_server/qgisserverdb/qgis-auth.db"

  # See https://docs.qgis.org/testing/en/docs/user_manual/working_with_vector/supported_data.html#pg-service-file
  SetEnv PGSERVICEFILE /home/david/.pg_service.conf
  FcgidInitialEnv PGPASSFILE "/home/david/.pgpass"

  # Tell QGIS Server instances to use a specific display number
  #FcgidInitialEnv DISPLAY ":99"

  # if qgis-server is installed from packages in debian based distros this is usually /usr/lib/cgi-bin/
  # run "locate qgis_mapserv.fcgi" if you don't know where qgis_mapserv.fcgi is
  ScriptAlias /cgi-bin/ /home/david/dev/qgis/build-QGIS_Server-Desktop_Debug-Debug/output/bin/
  <Directory "/home/david/dev/qgis/build-QGIS_Server-Desktop_Debug-Debug/output/bin/">
    AllowOverride None
    Options +ExecCGI -MultiViews -SymLinksIfOwnerMatch
    Order allow,deny
    Allow from all
    Require all granted
  </Directory>

 <IfModule mod_fcgid.c>
 FcgidMaxRequestLen 26214400
 FcgidConnectTimeout 60
 </IfModule>

</VirtualHost>

```
