Download from `https://planet.osm.ch/` this huge `switzerland.pbf` package and convert it to an osm:

```
osmconvert --verbose --drop-version switzerland.pbf -o=switzerland.p5m

osmfilter switzerland.o5m --verbose --keep="( boundary=administrative and ( admin_level=2 or admin_level=3 ) ) or highway=motorway or highway=trunk or highway=primary or place=state" -o=switzerland.osm

```

Then cut the features out and save them in new layers or, make allready a cutten out part of the map from `https://www.openstreetmap.org/export#map=15/47.5127/9.4287`

Now I use right click on the layer `export -> save features as` and then I select the objects of interests. I checked the style filters on the persisting projects citybees or QGEP for what object are needed. Actually here we can take them from a specific part of the map as well.

I import the styles from the persisting objects or I create it myselve.