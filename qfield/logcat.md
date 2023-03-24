```
adb logcat -s QField
```

Dann hast du output wie in QT Creator...

und so kannst du dort auch schön reindebugingmessageschreiben:
```
qDebug() << "List contains : " <<  FeatureListModel::data( index, FeatureListModel::KeyFieldRole ).toString();
```