## Overview

- The mainfolder is in the repo folder.
- It contains the plugin folder.
- And this contains an `__init.py__`.
- This usually calles what have to be done. Eg. exec a Dialoge.
- And it sets the icons etc. to the toolbar. 
- And then there is the main file e.g. `KontrollblattDialog.py`
- And there is all the magic.
- The GUI is created there (or you can use ui files and design it in QT creator).
- And everything that has to be done...

## __init.py__
```
from qgis.PyQt.QtCore import Qt
from .KontrollblattDialog import KontrollblattDialog
from PyQt5.QtWidgets import QAction, QMessageBox
from PyQt5.QtGui import QIcon
from . import resources

def classFactory(iface):
    return Kontrollblatt(iface)


class Kontrollblatt(object):

    def __init__(self, iface):
        self.iface = iface

    def initGui(self):
        self.action = QAction(u'Bauminventar Kontrollblatt', self.iface.mainWindow())
        self.action.setIcon(QIcon(":/plugins/kontrollblatt_sh/resources/icon.svg"));
        self.action.triggered.connect(self.run)
        self.iface.addToolBarIcon(self.action)

    def unload(self):
        self.iface.removeToolBarIcon(self.action)
        del self.action

    def run(self):
        layer = self.iface.activeLayer()

        if( layer.name() == 'kontrollblatt'):
            self.dlg = KontrollblattDialog( self.iface )
            self.dlg.exec()
        else:
            QMessageBox.information(None, u'Kontrollblatt SH', u'Das Plugin ist nur verfügbar für den Layer "kontrollblatt" mit den Feldern:\n  - erledigt_datum (date)\n  - kontrolleur (string)')


```

## Check out
The minimal plugin of wonder-sk:
https://github.com/wonder-sk/qgis-minimal-plugin

Or a simple plugin of Kuhn:
https://github.com/opengisch/quick_attribution

Or my little firsty:
https://github.com/opengisch/qgis_kontrollblatt_sh

## PyQGIS Cookbook
https://docs.qgis.org/3.4/pdf/en/QGIS-3.4-PyQGISDeveloperCookbook-en.pdf
https://docs.qgis.org/testing/en/docs/pyqgis_developer_cookbook/


## PyQgis Commands
https://gitlab.com/signenotes/opengisch/technical_notes/blob/master/qgis/pyqgis_commandos.md

# Deploy
Follow this description and check the examples:

https://github.com/opengisch/plugin_ci/tree/0ab4b476e60da8b81e921b8fee8185588474f7e6

And yes: Use *travis.org* (not travis.com)

For https://github.com/opengisch/qgis_kontrollblatt_sh I added the env-vars in the travis.org instead to create the secure keys you can see in the examples.

And then create Release (without creating tag previously...)

# Testing
To check where the plugins are go to QGIS:

`Settings -> User Profiles -> Open Active Profile Folder`

Go there and add a symbolic link something like this: `ln -s /home/david/dev/opengisch/projectgenerator projectgenerator`

Test it in QGIS...

Für nosetests müssen die Env-Vars definiert werden:
```
export QGIS_PREFIX_PATH=/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output

export LD_LIBRARY_PATH=/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output/lib
export PYTHONPATH=/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output/python/:/home/dave/dev/qgis/build-QGIS-Desktop-Debug/output/python/plugins:/home/dave/dev/qgis/QGIS/tests/src/python:

nosetests3 test_export.py 
```

Oder check im jeweiligen Repository . Evtl mit Dockertests (wenn Dbs involviert sind...)