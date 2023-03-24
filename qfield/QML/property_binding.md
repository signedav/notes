It's called property bindings.

An example:

```
  qmlObject.someProperty = 5
  // qmlObject.someProperty is set to the value 5

  myProperty = 3
  qmlObject.someProperty = myProperty
  // qmlObject.someProperty is bound to myProperty

  myProperty = 7
  // qmlObject.someProperty now also has the value 7
  
  // Bind the editor widgte feature the the current feature of the attributeTableModel
  editorWidget.feature = attributeTableModel.currentFeature

  // In C++
  QgsAttributeTableModel::setCurrentFeature( feature )
  {
    mCurrentFeature = feature; // Will change the internal value (and be used on any subsequent evaluation of properties bound to this
    emit currentFeatureChanged(); // Will force any bound properties to re-evaluate
  }
```

See https://github.com/opengisch/QField/pull/502#discussion_r263999350

### Aliases
goes to both sides

```
property alias color: rectangle.border.color

Rectangle {
    id: rectangle
}

```

means main object color is depending on the rectangle.border as well..

See: https://doc.qt.io/qt-5/qtqml-syntax-objectattributes.html#property-aliases

Btw.

This works:
```
id: formPopup
property alias superstate: form.state
FeatureForm {
        id: form
}
```
This does not work:
```
id: formPopup
property var superstate

FeatureForm {
        id: form
	state: formPopup.superstate
```
Suddenly the binding is broken. Why?

Somewhere in the FeatureForm it says:
```
state = 'Edit'
```

So the conclusion: Since an alias binds on both sides a binding does not break when we set the "depending" object (like state = 'Edit')


### Some links
https://doc.qt.io/qt-5/qtqml-syntax-propertybinding.html#debugging-overwriting-of-bindings

http://cdn2.hubspot.net/hubfs/149513/Roadshow_US/Best_Practices_in_Qt_Quick.pdf 
