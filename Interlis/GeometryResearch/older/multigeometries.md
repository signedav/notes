### Scenario 1: normal having three multi-geometry rows in the same table (in PostgreSQL) - means having 3 layers loaded from it:
```
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
```
and
```
    CLASS EverySpot =
        Name: TEXT;
        Points: MultiPoint;
        Lines: MultiLine;
        Surfaces: MultiSurface;
    END EverySpot;
```
Three layers:
- EverySpot (Points)
- EverySpot (Lines)
- EverySpot (Surfaces)

*So there can be created features with either points, lines (x)or polygons*

### Scenario 2: having a multi-geometry struct used as geometry in the table - means having 3 geometry layers connected to a non geometry layer:
```
      STRUCTURE MultiGeometry =
        Points: MultiPoint;
        Lines: MultiLine;
        Surfaces: MultiSurface;
      END MultiGeometry; 
```
and
```
        CLASS EverySpotMultiGeometry =
          Name: TEXT;
          Geometry: MultiGeometry;
        END EverySpotMultiGeometry;
```
Four layers:
- MultiGeometry (Points)
- MultiGeometry (Lines)
- MultiGeometry (Surfaces)
- EverySpotMultiGeometry (without any geometry)

> Unexpected but I guess it's how it needs to be is, that I can append multiple MultiGeometry of each type to the EverySpotMultiGeometry without having a BAG OF


### Scenario 3: having a struct containing multiple structs containing single geometries - means having 3 geometry layers connected to a non geometry layer:
```
      STRUCTURE SingleGeometry =
        Point: GeometryCHLV95_V1.Coord2;
        Line: Line;
        Surface: Surface;
      END SingleGeometry; 
    
      STRUCTURE MultiSingleGeometry = 
        Geometry: BAG {0..*} OF SingleGeometry;
      END MultiSingleGeometry;
```
and 
```
        CLASS EverySpotMultiSingleGeometry =
          Name: TEXT;
          Geometry: MultiSingleGeometry;
        END EverySpotMultiSingleGeometry;
```
Five layers:
- SingleGeometry (Point)
- SingleGeometry (Line)
- SingleGeometry (Surface)
- MultiSingleGeometry
- EverySpotMultiSingleGeometry


### Scenario 4: Same as 2 but having a BAG OF expecting the same behavior without the unexpected part
```
      !!@ili2db.mapping=MultiGeometry
      STRUCTURE MultiGeometry =
        Points: MultiPoint;
        Lines: MultiLine;
        Surfaces: MultiSurface;
      END MultiGeometry; 
```
and
```
        CLASS EverySpotMultiGeometry =
          Name: TEXT;
          Geometry: BAG {0..*} OF MultiGeometry;
        END EverySpotMultiGeometry;
```
Four layers:
- MultiGeometry (Points)
- MultiGeometry (Lines)
- MultiGeometry (Surfaces)
- EverySpotMultiGeometry (without any geometry)

> Expected that it can have multiple childs of every geometry type


