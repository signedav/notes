## Requirement
We want to have multiple geometries of different type linked to a specific object. It's maybe interesting to have one quality attribute per recorded geometry, but maybe not needed.

### 1. Object GeometryOfInterest inherited by three different GeometryObjects having types and is linked to the object (Geom_Res_GeometryOfInterest)

```
INTERLIS 2.3;

MODEL Geom_Res_GeometryOfInterest (en) 
AT "https://signedav.github.io/usabilitydave/models"
VERSION "2020-06-22" =
  
    IMPORTS GeometryCHLV95_V1, Units;
    IMPORTS UNQUALIFIED INTERLIS;
  
    DOMAIN
        Line = POLYLINE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2;
        Surface = SURFACE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2 WITHOUT OVERLAPS > 0.005;
        
    TOPIC DM =
        CLASS TheObject =
            Name: TEXT;
        END TheObject;

        CLASS GeometryOfInterest (ABSTRACT)=
            !! could have additional attributes like quality here
        END GeometryOfInterest;

        CLASS PointGeometry
        EXTENDS GeometryOfInterest =
            Geometry : MANDATORY GeometryCHLV95_V1.Coord2;
        END PointGeometry;

        CLASS LineGeometry
        EXTENDS GeometryOfInterest =
            Geometry : MANDATORY Line;
        END LineGeometry;

        CLASS SurfaceGeometry
        EXTENDS GeometryOfInterest =
            Geometry : MANDATORY Surface;
        END SurfaceGeometry; 

        ASSOCIATION GeometryOfInterest_TheObject =
            TheObject -<#> {1} TheObject;
            GeometryOfInterest -- {1..*} GeometryOfInterest;
        END GeometryOfInterest_TheObject;
    END DM;
END Geom_Res_GeometryOfInterest.
```
+ We can have a quality attribute
- seems very much engineered and clumbsy
+ physical implementation is nice. Having three geometry tables directly connected by the object.
+ handling in QGIS is nice 

### Three different GeometryObjects having types are linked to the object (Geom_Res_NoGeometryOfInterest) (could have quality in association) I don't know why we decided against that. 
```
MODEL Geom_Res_NoGeometryOfInterest (en) 
AT "https://signedav.github.io/usabilitydave/models"
VERSION "2020-06-22" =
  
    IMPORTS GeometryCHLV95_V1, Units;
    IMPORTS UNQUALIFIED INTERLIS;
  
    DOMAIN
        Line = POLYLINE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2;
        Surface = SURFACE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2 WITHOUT OVERLAPS > 0.005;
        
    TOPIC DM =
        CLASS TheObject =
            Name: TEXT;
        END TheObject;

        CLASS PointGeometry =
            Geometry : MANDATORY GeometryCHLV95_V1.Coord2;
        END PointGeometry;

        CLASS LineGeometry =
            Geometry : MANDATORY Line;
        END LineGeometry;

        CLASS SurfaceGeometry =
            Geometry : MANDATORY Surface;
        END SurfaceGeometry; 

        ASSOCIATION Geometries_TheObject = 
            !! could have additional attributes like quality here
            TheObject -<#> {1} TheObject;
            PointGeometry -- {1..*} PointGeometry;
            LineGeometry -- {1..*} LineGeometry;
            SurfaceGeometry -- {1..*} SurfaceGeometry;
        END Geometries_TheObject;
    END DM;
END Geom_Res_NoGeometryOfInterest.
```
+ We can have a quality attribute
+ smaller
- less elegant
- physical implementation has a referencetable in between that can be confusing. Geometries does not need to be associated to other geometries.
+ handling in QGIS is nice (since we can make direct connections in the relation editor widget)

### 3. Object GeometryOfInterest having multiple geometries and is linked to the object (Geom_Res_GeometryOfInterest_MultiTypeGeom)
```
MODEL Geom_Res_GeometryOfInterest_MultiTypeGeom (en) 
AT "https://signedav.github.io/usabilitydave/models"
VERSION "2020-06-22" =
  
    IMPORTS GeometryCHLV95_V1, Units;
    IMPORTS UNQUALIFIED INTERLIS;
  
    DOMAIN
        Line = POLYLINE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2;
        Surface = SURFACE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2 WITHOUT OVERLAPS > 0.005;
        
    TOPIC DM =
        CLASS TheObject =
            Name: TEXT;
        END TheObject;

        CLASS GeometryOfInterest=           
            !! could have additional attributes like quality here
            Point: GeometryCHLV95_V1.Coord2;
            Line: Line;
            Surface: Surface;
        END GeometryOfInterest;

        ASSOCIATION GeometryOfInterest_TheObject =
            TheObject -<#> {1} TheObject;
            GeometryOfInterest -- {1..*} GeometryOfInterest;
        END GeometryOfInterest_TheObject;
    END DM;
END Geom_Res_GeometryOfInterest_MultiTypeGeom.
```
+ We can have a quality attribute
+ smallest of the ones with quality
- model baker connects only to the instance with point and linked there to other layers. Maybe it's a bug with Geopackage.

### 4. Object having multiple geometries of multiple types (Geom_Res_Object_MultiBag) 
```
MODEL Geom_Res_Object_MultiBag (en) 
AT "https://signedav.github.io/usabilitydave/models"
VERSION "2020-06-22" =
  
    IMPORTS GeometryCHLV95_V1, Units;
    IMPORTS UNQUALIFIED INTERLIS;
  
    DOMAIN
        Line = POLYLINE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2;
        Surface = SURFACE WITH (STRAIGHTS) VERTEX GeometryCHLV95_V1.Coord2 WITHOUT OVERLAPS > 0.005;

        STRUCTURE PointStructure = 
            Point: GeometryCHLV95_V1.Coord2;
        END PointStructure;
            
        STRUCTURE LineStructure = 
            Line: Line;
        END LineStructure;
        
        STRUCTURE SurfaceStructure = 
            Surface: Surface;
        END SurfaceStructure;

    TOPIC DM =
        CLASS TheObject=
            Name: TEXT;
            !! could NOT have additional attributes like quality here
            Point: BAG {1..*} OF PointStructure;
            Line: BAG {1..*} OF LineStructure;
            Surface: BAG {1..*} OF SurfaceStructure;
        END TheObject;
    END DM;
END Geom_Res_Object_MultiBag.
```
+ We CANNOT have a quality attribute
+ smallest one and straight forward
- empty geometry layers.