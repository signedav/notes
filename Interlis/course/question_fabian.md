
I have kind of a confusion considering inheritances. Best explainable with an example. I have a model:
```
MODEL City_V1_1 (en) =

  TOPIC Constructions =
    CLASS Building  =
      Name : MANDATORY TEXT*99;
    END Building;
    CLASS Street  =
      Name : MANDATORY TEXT*99;
    END Street;
    
    ASSOCIATION Building_Street =
      Building -- {1..*} Building;
	    Street -- {1} Street;
    END Building_Street;

  END Constructions;

END City_V1_1.
```

And I would like to have my own model, using all the element from City_V1_1 but with additional attribute `Description` in `Constructions.Building`.

```

MODEL DavesCity_V1_1 (en) AT "mailto:localhost" VERSION "2021-07-20" =
  IMPORTS City_V1_1;
  TOPIC Constructions EXTENDS City_V1_1.Constructions =
    CLASS Building (EXTENDED) =
      Description : TEXT*99;
    END Building;
  END Constructions;

END DavesCity_V1_1.
```

I create the database with ili2db and I get a schema with every table from Model `City_V1_1` and from `DavesCity_V1_1` means: Building, Building, Street and Cooperation. But why I got Building two times, I still have the original. Why? I would like to have my own version of it "instead" and not "additionally".

If this is not possible and just how it is, then fine, but I wonder if it's never needed to "replace" a class with it's inheritance.

And anyway I wonder what would have been the differences with:

```
  TOPIC Constructions =
    DEPENDS ON City_V1_1.Constructions;

    CLASS Building EXTENDS City_V1_1.Constructions.Building =
```

or 
```
  TOPIC Constructions EXTENDS City_V1_1.Constructions = 

    CLASS Building2 EXTENDS City_V1_1.Constructions.Building =
```

Would be great if you have an explanation. 

Another use case would be, that I would like to have only my own version of `building` in the schema. No tables from `City_V1_1` at all, but still inherit from the `buliding` class of it. Is that even possible?

Thanks a lot and kind regards
Dave