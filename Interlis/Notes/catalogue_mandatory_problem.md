When I have a model using CatalogueObjects_V1 as catalogue base and I extend (1) the MandatoryCatalogueReference and (2) set the attribute referencing it to MANDATORY it still creates me a table without any NOT NULL constraint on those attributes.

See for current discussion https://github.com/opengisch/QgisModelBaker/issues/623

```
INTERLIS 2.3;

MODEL Gebaeude (de)
AT "https://signedav.github.io/usabilitydave/"
VERSION "2021-12-21"  =
  IMPORTS CatalogueObjects_V1;

  TOPIC Katalog =

    CLASS MaterialItem
    EXTENDS CatalogueObjects_V1.Catalogues.Item =
      Code : MANDATORY TEXT;
      Bezeichnung : TEXT;
    END MaterialItem;

    STRUCTURE MaterialItemRef
    EXTENDS CatalogueObjects_V1.Catalogues.MandatoryCatalogueReference =
      /* Reference is MANDATORY because it extends mandatory Rerference in MandatoryCatalogueReference */
      Reference (EXTENDED) : REFERENCE TO (EXTERNAL) MaterialItem;
    END MaterialItemRef;


  END Katalog;

  TOPIC Gebaeude =
    DEPENDS ON Gebaeude.Katalog;

    /* In diesem Fall ist Material in der DB nullable.*/
    CLASS Haus =
      Name: TEXT;
      Material : MANDATORY Gebaeude.Katalog.MaterialItemRef;
    END Haus;

  END Gebaeude;

END Gebaeude.
```

We do have `ili2db.ili.attrCardinalityMin` in `t_ili2db_meta_attrs` for `Gebaeude.Haus.Material` but it cannot be mapped that simple. 

Claude said:
> scheint mir ein Mangel in ili2db zu sein. Im Moment wird diese Referenz immer NULLable abgebildet.
> Grundsätzlich ist aber trotz MANDATORY im ili-Modell in der DB evtl. ein NULLable nötig (wenn wegen den Abbildungsregeln die Zielklasse in mehrere Tabellen abgebildet wird; im Beispiel nicht der Fall).

### 1. Critical Case: 

**Smart1Inheritance with SuperClass having attributes of subclasses**

Having a super class containing attributes of multiple sub classes should still be nullable.
```
  TOPIC Gebaeude =
    DEPENDS ON Gebaeude.Katalog;

    CLASS Objekt =
      Name: MANDATORY TEXT;
    END Objekt;

    /* In diesem Fall ist Material in der DB nullable und muss es wohl auch sein bei Smart Inheritance 1*/
    CLASS Haus EXTENDS Objekt=
      Material : MANDATORY Gebaeude.Katalog.MaterialItemRef;
    END Haus;
    
    CLASS Statue EXTENDS Objekt =
      Thema: MANDATORY TEXT;
    END Statue;

  END Gebaeude;
```

See https://github.com/opengisch/QgisModelBaker/issues/623#issuecomment-1171115079

**As well as Smart1Inheritance with SuperClass having attributes optional and subclasses are extending mandatory**

Claude said:
> der Fall wo in der Super-Klasse das Attribut OPTIONAL ist (aber nicht wirklich anders, könnte man auch als Variante zu "kommt in der Super-Klasse nicht vor" betrachten)

```
  TOPIC Gebaeude =
    DEPENDS ON Gebaeude.Katalog;

    CLASS Objekt =
      Name: MANDATORY TEXT;
      Material : Gebaeude.Katalog.MaterialItemRef;
    END Objekt;

    /* In diesem Fall ist Material in der DB nullable und muss es wohl auch sein bei Smart Inheritance 1*/
    CLASS Haus EXTENDS Objekt=
      Material (EXTENDED): MANDATORY Gebaeude.Katalog.MaterialItemRef;
    END Haus;
    
    CLASS Statue EXTENDS Objekt =
      Thema: MANDATORY TEXT;
    END Statue;

  END Gebaeude;
```

#### Sollution maybe:

We do have `ili2db.ili.attrCardinalityMin` in `t_ili2db_meta_attrs` for the current tables class, but as well we might need to check `t_ili2db_trafo` if the current class is built as `superClass`.

### 2. Critical Case: 

Claude said:
> Und bei Referenzen: das Referenz-Attribute (oder die Rolle) ist MANDATORY, aber die Ziel-Klasse der Referenz wird in verschiedene Tabellen abgebildet (dann braucht es mehrere FK-Spalten (und jede muss in der DB optional sein, weil nur eine davon einen Wert hat)

Something like this:

```
  TOPIC Katalog =

    CLASS MaterialItem
    EXTENDS CatalogueObjects_V1.Catalogues.Item =
      Code : MANDATORY TEXT;
      Bezeichnung : TEXT;
    END MaterialItem;

    CLASS MaterialItem1
    EXTENDS MaterialItem=
      Value : TEXT;
    END MaterialItem1;

    STRUCTURE MaterialItemRef
    EXTENDS CatalogueObjects_V1.Catalogues.MandatoryCatalogueReference =
      /* Reference is MANDATORY because it extends mandatory Rerference in MandatoryCatalogueReference */
      Reference (EXTENDED) : REFERENCE TO (EXTERNAL) MaterialItem;
    END MaterialItemRef;

  END Katalog;
```

See https://github.com/opengisch/QgisModelBaker/issues/623#issuecomment-1172062349
