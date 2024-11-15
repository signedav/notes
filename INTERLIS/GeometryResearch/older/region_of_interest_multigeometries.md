## Use case
Assets should have multiple region of interests (and back) what can be multiple geometries of every type
## Inheritance
### ILI
```
INTERLIS 2.3;

MODEL RegionOfInterest_Inheritance (en) 
AT "https://signedav.github.io/usabilitydave/models"
VERSION "2020-06-22" =
  
    IMPORTS GeometryCHLV95_V1, Units;
    IMPORTS UNQUALIFIED INTERLIS;
  
    DOMAIN
        Line = POLYLINE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2;
        Surface = SURFACE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2 WITHOUT OVERLAPS > 0.005;
        
        !! BAG OF with INTERLIS 2.3 only possible with STRUCTURE
        STRUCTURE PointStructure = 
            Point: GeometryCHLV95_V1.Coord2;
        END PointStructure;
        
        !!@ili2db.mapping=MultiPoint
        STRUCTURE MultiPoint =
            Points: BAG {1..*} OF PointStructure;
        END MultiPoint;
        
        STRUCTURE LineStructure = 
            Line: Line;
        END LineStructure;
        
        !!@ili2db.mapping=MultiLine
        STRUCTURE MultiLine =
            Lines: BAG {1..*} OF LineStructure;
        END MultiLine;
        
        STRUCTURE SurfaceStructure = 
            Surface: Surface;
        END SurfaceStructure;
        
        !!@ili2db.mapping=MultiSurface
        STRUCTURE MultiSurface =
            Surfaces: BAG {1..*} OF SurfaceStructure;
        END MultiSurface;
        
    TOPIC Assets =

        CLASS RegionOfInterest =
        Name: TEXT;
        END RegionOfInterest;

        CLASS StudySection EXTENDS RegionOfInterest =
            Geometry : MultiLine;
        END StudySection;

        CLASS StudyArea EXTENDS RegionOfInterest =
            Geometry : MultiSurface;
        END StudyArea;

        CLASS StudyLocation EXTENDS RegionOfInterest =
            Geometry : MultiPoint;
        END StudyLocation;

        CLASS Asset =
            Name: TEXT;
        END Asset;
        
        ASSOCIATION Asset_RegionOfInterest =
            Asset -- {1..*} Asset;
            RegionOfInterest -- {1..*} RegionOfInterest;
        END Asset_RegionOfInterest;

    END Assets; !! of TOPIC

END RegionOfInterest_Inheritance. !! of MODEL
```
### UML
![inheritance_uml](images/uml_inheritance.png)
### PostgreSQL
![inheritance_pg](images/pg_inheritance.png)
### QGIS
![inheritance_qgis](images/qgis_inheritance.png)
### Conclusion: 
Okay. But migth be an overkill

## Multiple Geometries
### ILI
```
INTERLIS 2.3;

MODEL RegionOfInterest_MultiGeometry (en) 
AT "https://signedav.github.io/usabilitydave/models"
VERSION "2020-06-22" =
  
    IMPORTS GeometryCHLV95_V1, Units;
    IMPORTS UNQUALIFIED INTERLIS;
  
    DOMAIN
        Line = POLYLINE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2;
        Surface = SURFACE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2 WITHOUT OVERLAPS > 0.005;
        
        !! BAG OF with INTERLIS 2.3 only possible with STRUCTURE
        STRUCTURE PointStructure = 
            Point: GeometryCHLV95_V1.Coord2;
        END PointStructure;
        
        !!@ili2db.mapping=MultiPoint
        STRUCTURE MultiPoint =
            Points: BAG {1..*} OF PointStructure;
        END MultiPoint;
        
        STRUCTURE LineStructure = 
            Line: Line;
        END LineStructure;
        
        !!@ili2db.mapping=MultiLine
        STRUCTURE MultiLine =
            Lines: BAG {1..*} OF LineStructure;
        END MultiLine;
        
        STRUCTURE SurfaceStructure = 
            Surface: Surface;
        END SurfaceStructure;
        
        !!@ili2db.mapping=MultiSurface
        STRUCTURE MultiSurface =
            Surfaces: BAG {1..*} OF SurfaceStructure;
        END MultiSurface;
        
        STRUCTURE RegionOfInterest =
            StudyLocation: MultiPoint;
            StudySection: MultiLine;
            StudyArea: MultiSurface;
        END RegionOfInterest; 

    TOPIC Assets =

        CLASS Asset =
            Name: TEXT;
            RegionOfInterest: BAG {0..*} OF RegionOfInterest;
        END Asset;
        
    END Assets; !! of TOPIC

END RegionOfInterest_MultiGeometry. !! of MODEL
```
### UML
![geometries_uml](images/uml_geometries.png)
### PostgreSQL
![geometries_pg](images/pg_geometries.png)
### QGIS
![geometries_qgis](images/qgis_geometries.png)
### Conclusion: 
It's not many to many - it's only one to many. A geometry can only have one asset as parent.

## Relations
### ILI
```
INTERLIS 2.3;

MODEL RegionOfInterest_Relations (en) 
AT "https://signedav.github.io/usabilitydave/models"
VERSION "2020-06-22" =
  
    IMPORTS GeometryCHLV95_V1, Units;
    IMPORTS UNQUALIFIED INTERLIS;
  
    DOMAIN
        Line = POLYLINE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2;
        Surface = SURFACE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2 WITHOUT OVERLAPS > 0.005;
        
        !! BAG OF with INTERLIS 2.3 only possible with STRUCTURE
        STRUCTURE PointStructure = 
            Point: GeometryCHLV95_V1.Coord2;
        END PointStructure;
        
        !!@ili2db.mapping=MultiPoint
        STRUCTURE MultiPoint =
            Points: BAG {1..*} OF PointStructure;
        END MultiPoint;
        
        STRUCTURE LineStructure = 
            Line: Line;
        END LineStructure;
        
        !!@ili2db.mapping=MultiLine
        STRUCTURE MultiLine =
            Lines: BAG {1..*} OF LineStructure;
        END MultiLine;
        
        STRUCTURE SurfaceStructure = 
            Surface: Surface;
        END SurfaceStructure;
        
        !!@ili2db.mapping=MultiSurface
        STRUCTURE MultiSurface =
            Surfaces: BAG {1..*} OF SurfaceStructure;
        END MultiSurface;
        
    TOPIC Assets =

        CLASS StudySection =
            Geometry : MultiLine;
        END StudySection;

        CLASS StudyArea =
            Geometry : MultiSurface;
        END StudyArea;

        CLASS StudyLocation =
            Geometry : MultiPoint;
        END StudyLocation;

        CLASS Asset =
            Name: TEXT;
        END Asset;
        
        ASSOCIATION Asset_RegionOfInterest =
            Asset -- {1..*} Asset;
            StudySection -- {1..*} StudySection;
            StudyArea -- {1..*} StudyArea;
            StudyLocation -- {1..*} StudyLocation;
        END Asset_RegionOfInterest;

    END Assets; !! of TOPIC

END RegionOfInterest_Relations. !! of MODEL
```
### UML
![relations_uml](images/uml_relations.png)
### PostgreSQL
![inheritance_pg](images/pg_relations.png)
### QGIS
![inheritance_qgis](images/qgis_relations.png)
### Conclusion
Is what postgresql does with inheritances


