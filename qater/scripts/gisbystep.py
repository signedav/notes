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
    QgsLayerTreeModel,
)
from qgis.gui import (
    QgsMapCanvas,
    QgsVertexMarker,
    QgsMapCanvasItem,
    QgsRubberBand,
    QgsLayerTreeView
)
from PyQt5 import QtGui, QtWidgets

# 1. Was ist das genau?
qgs = QgsApplication([], True)

# 2. Was ist das genau?
qgs.initQgis()


canvas = QgsMapCanvas()
canvas.xyCoordinates.connect(lambda coords: coord_label.setText(f"{coords.x()}, {coords.y()}"))

layertreemodel = QgsLayerTreeModel(QgsProject.instance().layerTreeRoot())
layertree = QgsLayerTreeView()
layertree.setModel(layertreemodel)

coord_label = QtWidgets.QLabel()

window = QtWidgets.QWidget()
layout = QtWidgets.QGridLayout()
layout.addWidget(canvas)
layout.addWidget(layertree)
layout.addWidget(coord_label)
window.setLayout(layout)

window.show()

# vectorlayer
vlayer = QgsVectorLayer(
    os.getenv("TEST_DATA", "testdata/Australia_Airports.geojson"),
    "Airports",
    "ogr",
)
QgsProject.instance().addMapLayer(vlayer)

# rasterlayer
urlWithParams = "type=xyz&url=https://a.tile.openstreetmap.org/%7Bz%7D/%7Bx%7D/%7By%7D.png&zmax=19&zmin=0&crs=EPSG3857"
rlayer = QgsRasterLayer(urlWithParams, "OpenStreetMap", "wms")

QgsProject.instance().addMapLayer(rlayer)

# set the map canvas layer set
canvas.setLayers([vlayer,rlayer])

canvas.setExtent(vlayer.extent())

# 3. Was ist das genau?
qgs.exec_()
    