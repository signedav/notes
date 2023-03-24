Normal style like this:

```
VisibilityFadingRow {
  id: fadingRow
  
  RubberbandModel {
    id: rubberbandModel
    onVertexCountChanged: {
      console.log( "vertex changed )
    }
  }
}
```

If outside the object but inside the parent you cannot make:
```
  rubberbandModel.onVertexCountChanged: {
     console.log( "vertex changed )
  }
```
No error, but no working neither.

So do that:
```
VisibilityFadingRow {
  id: fading Row
  
  property RubberbandModel rubberbandModel
  
  Connections {
      target: rubberbandModel
      onVertexCountChanged: {
        console.log( "vertex changed )
      }
  }
}
```

But what does not work - and I dont understand - if whe have a FadingRow.qml containing this:
```
VisibilityFadingRow {
  id: fadingRow
  
  property RubberbandModel rubberbandModel
```

And we create it like this in e.g. qgismobileapp.qml:
```
FadingRow{
  id: fadingRow
}
```
this does not work:

```
FadingRow{
  id: fadingRow
  
  Connections {
      target: rubberbandModel
      onVertexCountChanged: {
        console.log( "vertex changed )
      }
  }
}
```

We have to do that:

```
VisibilityFadingRow {
  id: fading Row
  
  signal rubberbandModelVertexCountChanged
  
  property RubberbandModel rubberbandModel
  
  Connections {
      target: rubberbandModel
      onVertexCountChanged: {
        console.log( "vertex changed )
      }
  }
}
```

and that:

```
FadingRow{
  id: fadingRow
  
  onRubberbandVertexCountChanged: {
    console.log( "vertex changed )
  }
}
```


