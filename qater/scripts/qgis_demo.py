import os
import sys
from qgis.core import (
    QgsVectorLayer,
    QgsRasterLayer,
    QgsPoint,
    QgsPointXY,
    QgsProject,
    QgsGeometry,
    QgsMapRendererJob,
    QgsApplication,
    QgsProviderRegistry,
    QgsCoordinateReferenceSystem,
)
from qgis.gui import (
    QgsMapCanvas,
    QgsVertexMarker,
    QgsMapCanvasItem,
    QgsRubberBand,
)
from PyQt5 import QtGui

# Supply the path to the qgis install location
# This is supplied by the environment variable
# Invoke this script using "C:\OSGeo4W64\bin\python-qgis"
# QgsApplication.setPrefixPath("C:\\OSGeo4W64\\apps\\qgis", True)
APP_ICON = "graphics/airports.ico"

# Create a reference to the QgsApplication.
# Setting the second argument to True enables the GUI.  We need
# this since this is a custom application.
qgs = QgsApplication([], True)

# daves Hack
qgs.initQgis()

# setup icon in bundle mode
icon_path = os.getenv("APP_ICON", APP_ICON)
qgs.setWindowIcon(QtGui.QIcon(icon_path))

# Write your code here to load some layers, use processing
# algorithms, etc.
canvas = QgsMapCanvas()
canvas.setWindowTitle("Airport Viewer")
canvas.show()

vlayer = QgsVectorLayer(
    os.getenv("TEST_DATA", "testdata/Australia_Airports.geojson"),
    "Airports layer",
    "ogr",
)
if not vlayer.isValid():
    print("Vector layer failed to load!")

ecw_file = os.getenv("TEST_ECW", "testdata/64002.ecw")
ecw_layer = QgsRasterLayer(ecw_file, "Canberra 100K Map", "gdal")

if ecw_layer.isValid():
    ecw_layer.setCrs(
        QgsCoordinateReferenceSystem("EPSG:28355")
    )
    QgsProject.instance().addMapLayer(ecw_layer)
else:
    print("ECW layer failed to load!")

urlWithParams = "type=xyz&url=https://a.tile.openstreetmap.org/%7Bz%7D/%7Bx%7D/%7By%7D.png&zmax=19&zmin=0&crs=EPSG3857"
rlayer = QgsRasterLayer(urlWithParams, "OpenStreetMap", "wms")

if rlayer.isValid():
    QgsProject.instance().addMapLayer(rlayer)
else:
    print("XYZ layer failed to load!")

# set extent to the extent of our layer
canvas.setExtent(vlayer.extent())

# set the map canvas layer set
canvas.setLayers([vlayer, ecw_layer, rlayer])

# set canvas icon
canvas.setWindowIcon(QtGui.QIcon(icon_path))

qgs.exec_()

# Finally, exitQgis() is called to remove the
# provider and layer registries from memory
qgs.exitQgis()
