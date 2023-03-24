```

DROP TABLE test_delete_rule.reach_text;

CREATE TABLE test_delete_rule.reach_text
(
  id integer NOT NULL,
  text text
)
WITH (
  OIDS=FALSE
);
ALTER TABLE test_delete_rule.reach
  OWNER TO postgres;

CREATE OR REPLACE FUNCTION  test_delete_rule.check_empty_text()
RETURNS trigger AS $BODY$
BEGIN
	IF EXISTS (SELECT NEW.text) THEN
		RAISE NOTICE 'text exists';
	END IF;
	IF new.text IS NULL THEN
		RAISE NOTICE 'text is null';
	END IF;
	IF new.text='' THEN
		RAISE NOTICE 'text is empty';
	END IF;
	RETURN NEW;
END;
$BODY$
LANGUAGE 'plpgsql';

-- DROP TRIGGER check_empty ON test_delete_rule.reach_text;

CREATE TRIGGER check_empty BEFORE INSERT OR UPDATE ON test_delete_rule.reach_text 
FOR EACH ROW EXECUTE PROCEDURE test_delete_rule.check_empty_text();
```


```
INSERT INTO  test_delete_rule.reach_text (id, text) VALUES (1, NULL);
> 
NOTICE:  text exists
NOTICE:  text is null
```
```
INSERT INTO  test_delete_rule.reach_text (id, text) VALUES (1, '');
> 
NOTICE:  text exists
NOTICE:  text is empty
```
```
INSERT INTO  test_delete_rule.reach_text (id) VALUES (1);
> 
NOTICE:  text exists
NOTICE:  text is null
```
Fazit: Gaht (eso) nöd. Vilicht häsch no anderi Idee...