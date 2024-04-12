Used eg. for connecting projects locally to a docker postgis-db:

On startup of QGIS this message appears:

> could not translate host name to address name or service not known


```
sudo vim /etc/hosts
```

```
127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4 demo-postgis infra-visu-postgis
::1         localhost localhost.localdomain localhost6 localhost6.localdomain6
```                                                                                                 