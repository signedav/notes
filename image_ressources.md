If you want to use ressources in plugin you have to have in code of eg. `projectgenerator/qgs_project_generator.py`

```
from .resources import *

[...]


self.__generate_action = QAction( QIcon(':/plugins/projectgenerator/projectgenerator-icon.svg'),

```

The projectgenerator-icon.svg is in the folder `projectgenerator/resources`

And you have to create the `projectgenerator/resources.qrc` looking like this:

```
<RCC>
  <qresource prefix="/plugins/projectgenerator" >
     <file alias="projectgenerator-icon.svg">resources/projectgenerator-icon.svg</file>
  </qresource>
</RCC>
```

and the projectgenerator/resources.py created by command: 
```
pyrcc5 -o resources.py resources.qrc

```