If you create a widget e.g. like this (testqgsvaluerelationwidgetwrapper.cpp):
```
QgsValueRelationWidgetWrapper w_favoriteauthors( vl_json, vl_json->fields().indexOf( QStringLiteral( "json_content" ) ), nullptr, nullptr );

```
Set the feature:
```
  w_favoriteauthors.setFeature( vl_json->getFeature( 1 ) );
```
then here it's a value relation means with table stuff. 
```
  w_favoriteauthors.mTableWidget->item( 0, 0 )->setCheckState( Qt::Checked);

```
**AND NOW IT'S NOWWHERE SAVED**

You need to do the following:

1. Save it on level AttributeTable-Dialoge Level
2. Commit it on layer -> then it's saved to GPKG (or whereever)

But, we dont have the AttributeTable-Dialoge here... So we have to make a shorcut around:
`QgsAttributeTable->saveEdits()` -> `QgsVectorLayer->changeAttributeValue()`

Directly store it on `mChangedAttributeValues` then it's in editBuffer...
```
  vl_json->changeAttributeValue( 1, 4, w_favoriteauthors.value() );
```
and then commit the changes (like saved).
```
  vl_json->commitChanges();
```

And be avare that editing is active...
