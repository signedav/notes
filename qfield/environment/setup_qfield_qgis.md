Im Build des aktuellen QGIS Projekts:
```
cmake -DCMAKE_INSTALL_PREFIX=/home/dave/qfield_devenv/qgis_masterbuild/ ../QGIS
ninja install
```

Wenn keine Rechte, dann mal checken
`ls -alh /home/david/local`

oder einfach mit `sudo`...

Dann im config.pri den Pfad konfigurieren...
```
android {
  # To build for android you need OSGEO4A
  OSGEO4A_DIR = /home/david/dev/OSGeo4A
}
!android {
  # To build for a PC, you need Qt5 builds of QGIS, QWT and QScintilla
  QGIS_INSTALL_PATH = /home/david/qfield_devenv/qgis_masterbuild/
}
```

Und den Ordner /3rdParty/tesselate klonen von hier:
https://github.com/opengisch/tessellate/tree/cd2ace1eda3460f772f4704a7dff146a2fa8ebca

## Missing modules installieren auf 5.12
```
  sudo apt install qml-module-qtquick-controls2 
  sudo apt install qml-module-qtpositioning
  sudo apt install qml-module-qt-labs-settings
  sudo apt install qml-module-qtquick-dialogs 
  sudo apt install qml-module-qt-labs-folderlistmodel 
  
  sudo apt install qtquickcontrols2-5-dev
  sudo apt install libqt5sensors5-dev

```

## Additional info...
Worktree von QField:
`git worktree add -b work_qfield ../work_QField origin/master`
und im Ordner work_QField:
`git submodule update --init --recursive`

