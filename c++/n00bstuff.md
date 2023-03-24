### Pointers
```
var is 25 at address 1776
foo = &var means foo gets 1776 (address of var)
bar = *foo means bar gets 25 (value pointed by the address)
```

### const

variables that don't need to be changed - better compiled.
```
const int Constant1
```
See http://duramecho.com/ComputerInformation/WhyHowCppConst.html

### Parameters
```
void Subroutine1(int Parameter1)
{ printf("%d",Parameter1);}
```
accepts the parameter passed to it in the default C & C++ way - which is a copy. Therefore the subroutine can read the value of the variable passed to it but not alter it because any alterations it makes are only made to the copy and are lost when the subroutine ends. 

The addition of an ‘&’ to the parameter name in C++ (which was a very confusing choice of symbol because an ‘&’ in front of variables elsewhere in C generates pointers!) causes the actual variable itself, rather than a copy, to be used as the parameter in the subroutine and therefore can be written to thereby passing data back out the subroutine. Therefore
```

void Subroutine3(int &Parameter1) 
{ Parameter1=96;}
```
would set the variable it was called with to 96. This method of passing a variable as itself rather than a copy is called a ‘reference’ in C++.

That way of passing variables was a C++ addition to C. To pass an alterable variable in original C, a rather involved method was used. This involved using a pointer to the variable as the parameter then altering what it pointed to was used. For example
```
void Subroutine4(int *Parameter1) 
{ *Parameter1=96;}
```
works but requires the every use of the variable in the called routine altered like that and the calling routine also altered to pass a pointer to the variable. It is rather cumbersome.

Und um dann die Variable direkt zu übergeben, sie allerdings nicht anpassbar zu machen, macht man `void Subroutine4(big_structure_type const &Parameter1)` weshalb wird es denn überhaupt mit & übergeben? Womöglich, damit es nicht Kopien erestellen muss.

### Casts
https://en.cppreference.com/w/cpp/language/static_cast

### References vs. pointers
https://www.educba.com/c-plus-plus-reference-vs-pointer/

### Forward declaration
Like:
```
class QgsAbstractGeometrySimplifier;
class QgsActionManager;
```
Be aware you only can create pointers from it. Otherwise do not use forward declaration and just `#include` the headers with the definition.

## Iterators with QT

### Some notes:
This works actually, not sure why. I think it's because a copy of the list is created and then the original list is edited.
```
  QgsFeatureList validFeatures = newFeatures;
  QgsFeatureList invalidFeatures;
  for ( const QgsFeature &f : qgis::as_const( validFeatures ) )
  {
    for ( int idx = 0; idx < pasteVectorLayer->fields().count(); ++idx )
    {
      QStringList errors;
      if ( !QgsVectorLayerUtils::validateAttribute( pasteVectorLayer, f, idx, errors, QgsFieldConstraints::ConstraintStrengthHard, QgsFieldConstraints::ConstraintOriginNotSet ) )
      {
        invalidFeatures << f;
        validFeatures.removeOne( f );
        break;
      }
    }
  }
```
But it's not nice. I tried it like this:
```
  for ( auto it = validFeatures.begin(); it != validFeatures.end(); it++ )
  {
    for ( int idx = 0; idx < pasteVectorLayer->fields().count(); ++idx )
    {
      QStringList errors;
      if ( !QgsVectorLayerUtils::validateAttribute( pasteVectorLayer, *it, idx, errors, QgsFieldConstraints::ConstraintStrengthHard, QgsFieldConstraints::ConstraintOriginNotSet ) )
      {
        invalidFeatures << *it;
        validFeatures.erase(it);
        break;
      }
    }
  }
```
And finally did it like this:
```
  QMutableListIterator<QgsFeature> it(validFeatures);
  while ( it.hasNext() )
  {
    QgsFeature &f = it.next();
    for ( int idx = 0; idx < pasteVectorLayer->fields().count(); ++idx )
    {
      QStringList errors;
      if ( !QgsVectorLayerUtils::validateAttribute( pasteVectorLayer, f, idx, errors, QgsFieldConstraints::ConstraintStrengthHard, QgsFieldConstraints::ConstraintOriginNotSet ) )
      {
        invalidFeatures << f;
        it.remove();
        break;
      }
    }
  }
```

Btw. in `QgsFixAttributeDialog` I have the mCurrentFeature what is an iterator. This is instead storing globally an index...

## Const lists
e.g. before making iteration over it. What is the nicest to use:
```
  const QgsFields fields { pasteVectorLayer->fields() };
  const auto fields = pasteVectorLayer->fields();
  const auto fields { pasteVectorLayer->fields() }
  const QgsFields fields = pasteVectorLayer->fields();
  
```
And why not:
```
for ( const QgsField &field : qgis::as_const( pasteVectorLayer ) )->fields() )
```
(does not work, since `fields()` returns here anyway const. So this:)
```
for ( const QgsField &field : pasteVectorLayer->fields() )
```

## Question regarding CMakeList.txt

Why is there a SET(QGIS_GUI_SRCS in CMakeList of gui but not a SET(QGIS_APP_HDRS in CMakeList of app?
