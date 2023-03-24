It's mostly because there is a mess with the installations.

E.g. this error:
```
Python error: couldn't load plugin 'processing' (python error)
```

I removed everything:
```
 sudo apt-get autoremove qgis
 sudo apt-get autoremove python-qgis 
```
Maybe a `purge`

And then there has been some more depending files. I made this:
```
 sudo rm -rf /usr/share/qgis/
```

And then reinstalled qgis:
```
sudo apt install qgis python-qgis qgis-plugin-grass
```

Not sure if it's the smart way - but Rock'n'Roll.
