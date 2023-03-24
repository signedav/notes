-- PROCEDURE: geometry_fun.insertcollection()

-- DROP PROCEDURE geometry_fun.insertcollection();

CREATE OR REPLACE PROCEDURE geometry_fun.insertgeometry(
	)
LANGUAGE 'sql'
AS $BODY$
INSERT INTO geometry_fun.geometry_layer (name, geom) VALUES ( 'line1', ST_GeomFromText('LINESTRING(1 2, 3 4)', 2056) );
INSERT INTO geometry_fun.geometry_layer (name, geom) VALUES ( 'line2', ST_GeomFromText('LINESTRING(3 4, 4 5)', 2056) );
INSERT INTO geometry_fun.geometry_layer (name, geom) VALUES ( 'point1', ST_GeomFromText('POINT(1 2)', 2056) );
INSERT INTO geometry_fun.geometry_layer (name, geom) VALUES ( 'point2', ST_GeomFromText('POINT(0 3)', 2056) );
$BODY$;


call geometry_fun.insertgeometry();

SELECT * FROM geometry_fun.geometry_layer;