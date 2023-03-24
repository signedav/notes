### some naming conventions
- kein Abweichen von der ersten Normalform – d.h. niemals strukturierte Daten in einzelnen Datenbankfeldern
- so normalisiert wie möglich (insbesondere niemals Daten redundant speichern) – nur in Ausnahmefällen denormalisiert lassen (z.B. Anrede “Herr/Frau” nicht in separate Tabelle auslagern)
- grundsätzlich alle Bezeichner in Kleinbuchstaben; Unterstriche sind erlaubt (“person” statt “Person”)
- grundsätzlich alle Bezeichner in der gleichen Sprache
- Tabellen und Attributnamen im Singular (“person” statt “personen”)
- Attributnamen (außer Fremdschlüssel) mit Präfix, der Kurzform des Tabellennamens wiederspiegelt (“pers_name” statt “name”)
- Primärschlüssel ist immer eine separate Spalte namens Präfix + “_id” (“pers_id”) vom Typ Serial (PostgreSQL) bzw. Integer Auto Increment (MySQL)
- Fremdschlüsselnamen werden inklusive Präfix von der referenzierten Tabelle übernommen (“pers_id” bleibt in jeder referenzierenden Tabelle “pers_id”)
- keine Abkürzungen (“pers_geburtsjahr” statt “pers_geb”)
- keine “Anreicherung” von Attributnamen (“pers_geburtsjahr” statt “pers_geburtsjahr_int”)
- Spalten mit Booleschen Werte so benennen, dass sofort klar ist, wofür true/false stehen (“maennlich” statt “geschlecht”)

Quelle: https://amor.cms.hu-berlin.de/~kunert/blog/2016/04/19/sql-namenskonventionen/


### PRIMARY and FOREIGNKEY names

Postgres creates it like this:
```
CREATE TABLE test.catwithitems
(
    id integer NOT NULL DEFAULT nextval('test.catwithitems_id_seq'::regclass),
    name text COLLATE pg_catalog."default",
    item_id integer,
    CONSTRAINT catwithitems_pkey PRIMARY KEY (id),
    CONSTRAINT catwithitems_item_id_fkey FOREIGN KEY (item_id)
        REFERENCES test.items (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
```