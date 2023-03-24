Als Template für Pluginentwicklung nimm am besten ein sogenanntes:

`miniPlugin`

Irgendwo auf Github verfügbar von Martin Dobias.

Zum sehen wo die Plugins gespeichert werden müssen, geht man auf QGIS 3:

`Settings -> User Profiles -> Open Active Profile Folder`
dann iste es irgendwie: 

QGIS 2.18 installation hat vermutlich irgendwie `.qgis2/python/plugins` oder so. Zumindest dort habe ich die Plugins, wenn ich die akutelle Source des QGIS 2.18.18 debugge...

Am besten erstellt man von diesem Ordner aus einen Link zum `dev/opengisch/whatever` mit `ln -s`
Irgendwie so: `ln -s /home/david/dev/opengisch/projectgenerator projectgenerator`

Chasch au mal de Probiere im Moment:
`/home/david/.local/share/QGIS/QGIS3/profiles/default/python/plugins`