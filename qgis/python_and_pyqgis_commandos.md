```
project = QgsProject.instance()

layers = project.mapLayersByName('test')
testlayer = None

for x in layers:
  testlayer = x
  
import os
os.path.isfile(testlayer.source())
QgsDataSourceUri(testlayer.dataProvider().dataSourceUri()).database()
```



```

project = QgsProject.instance()
layer = QgsProject.instance().mapLayer('[% @layer_id %]')
if not layer.isEditable():
    qgis.utils.iface.messageBar().pushMessage( 'Cannot duplicate feature in not editable mode on layer {layer}'.format( layer=layer.name() ) )
else:
    features=[]
    if len('[% $id %]')>0:
        features.append( layer.getFeature( [% $id %] ) )
    else:
        for x in layer.selectedFeatures():
            features.append( x )
    feature_count=0
    children_info=''
    featureids=[]
    for f in features:
        result=QgsVectorLayerUtils.duplicateFeature(layer, f, project, 0 )
        featureids.append( result[0].id() )
        feature_count+=1
        for ch_layer in result[1].layers():
            children_info+='{number_of_children} children on layer {children_layer}\n'.format( number_of_children=str( len( result[1].duplicatedFeatures(ch_layer) ) ), children_layer=ch_layer.name() )
            ch_layer.selectByIds( result[1].duplicatedFeatures(ch_layer) )
    layer.selectByIds( featureids )
    qgis.utils.iface.messageBar().pushMessage( '{number_of_features} features on layer {layer} duplicated with\n{children_info}'.format( number_of_features=str( feature_count ), layer=layer.name(), children_info=children_info ) )
```
    
```
    iface.activeLayer()
    selectedFeatures()
``` 
    
    In actions use this:
```
    qgis.utils.iface
```    
    

# To get value of field:
```
layer = iface.activeLayer()
layer
<qgis._core.QgsVectorLayer object at 0x7f3432374678>

layer.selectedFeatures()
[<qgis._core.QgsFeature object at 0x7f3430749e58>]

layer.selectedFeatures()[0]
<qgis._core.QgsFeature object at 0x7f3430749ee8>

feature=layer.selectedFeatures()[0]

feature["faelljahr"]
NULL
```    
    
# Get a signal from the map tools    
```
mapCanvas.mapToolChanged.connect(set_plugin_active_again_and_tell_user_that_no_selection_was_detected)
```    
# Create rule-based expression on label
But issue that rule-based has to be selected first
```
stammdatenLayer = QgsProject.instance().mapLayersByName('stammdaten')[0]
#Configure label settings
settings = QgsPalLayerSettings()
settings.fieldName = 'Lagegenauigkeit'
textFormat = QgsTextFormat()
textFormat.setSize(10)
settings.setFormat(textFormat)
#create and append a new rule
root = QgsRuleBasedLabeling.Rule(QgsPalLayerSettings())
rule = QgsRuleBasedLabeling.Rule(settings)
rule.setDescription(settings.fieldName)
rule.setFilterExpression('"T_id">618')
root.appendChild(rule)
#Apply label configuration
rules = QgsRuleBasedLabeling(root)
stammdatenLayer.setLabeling(rules)
stammdatenLayer.triggerRepaint()
```

# Create graduated symbols on layers
```
symbol = QgsSymbol.defaultSymbol(stammdatenLayer.geometryType())
symbol.setColor(QColor('green'))
rng = QgsRendererRange(10,20,symbol,"Test")
ranges = []
ranges.append(rng)
renderer = QgsGraduatedSymbolRenderer('faelldatum!=NULL',ranges)
stammdatenLayer.setRenderer(renderer)
iface.mapCanvas().refresh()
```  


#Häckchen setzen in der Legende
```
        renderer = self.stammdatenLayer.renderer()
        ltl = QgsProject.instance().layerTreeRoot().findLayer(self.stammdatenLayer.id())
        ltm = self.iface.layerTreeView().model()
        legendNodes = ltm.layerLegendNodes(ltl)
        for legendNode in legendNodes:
            if legendNode.data(0) == 'Bäume gefällt':
                #Qt.Unchecked = 0, Qt.CheckStateRole = 10
                legendNode.setData(0, 10)
```

# GPKG mit Python in Konsole

```

>>> import sqlite3
>>> conn =sqlite3.connect("test_3.gpkg")
>>> cursor = conn.cursor()
>>> cursor.execute("PRAGMA table_info(t_ili2db_model)")
<sqlite3.Cursor object at 0x7f88e890a5e0>
>>> table_info=cursor.fetchall()
>>> print(table_info)
[(0, 'file', 'TEXT(250)', 1, None, 0), (1, 'iliversion', 'TEXT(3)', 1, None, 2), (2, 'modelName', 'TEXT', 1, None, 1), (3, 'content', 'TEXT', 1, None, 0), (4, 'importDate', 'DATETIME', 1, None, 0)]

>>> for row in table_info:
...   print(row)
... 
(0, 'file', 'TEXT(250)', 1, None, 0)
(1, 'iliversion', 'TEXT(3)', 1, None, 2)
(2, 'modelName', 'TEXT', 1, None, 1)
(3, 'content', 'TEXT', 1, None, 0)
(4, 'importDate', 'DATETIME', 1, None, 0)

>>> for row in table_info:
...   print(row[1])
... 
file
iliversion
modelName
content
importDate
```





















