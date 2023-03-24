## Baskets and relations to extended classes

See here as well (https://github.com/opengisch/QgisModelBaker/issues/671)

The current implementation creates a basket per topic / dataset intersection. QGIS Model Baker filters the selectable baskets per topic of the layer. This leads to issues on extended classes that references (over the base class) to objects in the base class. See the scenarios below.

### Scenario 1 - Cross-Topic References - Works fine with baskets:

On creating this model with Qgis Model Baker:
```
INTERLIS 2.3;

MODEL City_V1_1 (en) AT "mailto:localhost" VERSION "2021-07-20" =

  TOPIC Constructions =
    CLASS Building  =
      Name : MANDATORY TEXT*99;
    END Building;
    CLASS Street  =
      Name : MANDATORY TEXT*99;
    END Street;
    CLASS Cooperation  =
      Name : MANDATORY TEXT*99;
    END Cooperation;
  END Constructions;

  TOPIC Nature =
    DEPENDS ON City_V1_1.Constructions;
    CLASS Park =
      Description : TEXT*99;
    END Park;
    ASSOCIATION Park_Street =
      Street (EXTERNAL) -- {0..1} City_V1_1.Constructions.Street;
      Park -- {0..*} Park;
    END Park_Street;
  END Nature;

END City_V1_1.

```
It creates baskets for each topic. Means for `Constructions` and `Nature`. The class `Park` is in the topic `Nature` and the `Street` in `Constructions`. This means they are in different baskets.

Exporting with:

```
java -jar /home/dave/dev/opengisch/QgisModelBaker/QgisModelBaker/libs/modelbaker/iliwrapper/bin/ili2gpkg-4.6.1/ili2gpkg-4.6.1.jar --dbfile /home/dave/qgis_project/lg_contacts_test/citytest_assoc2.gpkg --export --exportTid --models City_V1_1 /home/dave/qgis_project/inheritance/citytest/noinherit_assoc_export.xtf
```

Gives no error.


And the (valid) data looks like this:
```
<?xml version="1.0" encoding="UTF-8"?><TRANSFER xmlns="http://www.interlis.ch/INTERLIS2.3">
<HEADERSECTION SENDER="ili2gpkg-4.6.1-63db90def1260a503f0f2d4cb846686cd4851184" VERSION="2.3"><MODELS><MODEL NAME="City_V1_1" VERSION="2021-07-20" URI="mailto:localhost"></MODEL></MODELS></HEADERSECTION>
<DATASECTION>
<City_V1_1.Constructions BID="4e043e69-d001-472b-af09-5f009922a4c1">
<City_V1_1.Constructions.Street TID="123"><Name>Main Street</Name></City_V1_1.Constructions.Street>
</City_V1_1.Constructions>
<City_V1_1.Nature BID="ced60f5f-0419-4b10-9f7a-201163ddd165">
<City_V1_1.Nature.Park TID="3"><Description>Citygreen</Description><Street REF="123"></Street></City_V1_1.Nature.Park>
</City_V1_1.Nature>
</DATASECTION>
</TRANSFER>
```

### Scenario 2 - EXTENDED TOPIC - Trouble with baskets:

On creating this model with Qgis Model Baker:
```
INTERLIS 2.3;

MODEL Bund_City_V1_1 (en) AT "mailto:localhost" VERSION "2021-07-20" =

  TOPIC Constructions =
    CLASS Building  =
      Name : MANDATORY TEXT*99;
    END Building;
    CLASS Street  =
      Name : MANDATORY TEXT*99;
    END Street;
    CLASS Cooperation  =
      Name : MANDATORY TEXT*99;
    END Cooperation;

    ASSOCIATION Building_Street =
      Street -- {0..1} Street;
      Building -- {0..*} Building;
    END Building_Street;
  END Constructions;

END Bund_City_V1_1.

MODEL Kanton_City_V1_1 (en) AT "mailto:localhost" VERSION "2021-07-20" =
  IMPORTS Bund_City_V1_1;
  TOPIC Constructions EXTENDS Bund_City_V1_1.Constructions =
    CLASS Building (EXTENDED) =
      Description : TEXT*99;
    END Building;
  END Constructions;

END Kanton_City_V1_1.
```

It creates baskets for each topic. Means for `Bund_City_V1_1.Constructions` and `Kanton_City_V1_1.Construtions`. The derived class `Building` is in the topic `Kanton_City_V1_1.Construtions` and the `Street` in `Bund_City_V1_1.Constructions`. This means they are in different baskets.

Exporting with:
```
java -jar /home/dave/dev/opengisch/QgisModelBaker/QgisModelBaker/libs/modelbaker/iliwrapper/bin/ili2gpkg-4.6.1/ili2gpkg-4.6.1.jar --dbfile /home/dave/qgis_project/lg_contacts_test/citytest_assoc.gpkg --export --exportTid --models Bund_City_V1_1;Kanton_City_V1_1 /home/dave/qgis_project/inheritance/citytest/inherit_assoc_export.xtf
```

Gives the error:
```
Error: line 0: Kanton_City_V1_1.Constructions.Building: tid 2: No object found with OID 123 in basket 7d7558e7-194c-4787-9aa3-fb6a993a304f.
```

And the (invalid) data looks like this:
```
<?xml version="1.0" encoding="UTF-8"?><TRANSFER xmlns="http://www.interlis.ch/INTERLIS2.3">
<HEADERSECTION SENDER="ili2gpkg-4.6.1-63db90def1260a503f0f2d4cb846686cd4851184" VERSION="2.3"><MODELS><MODEL NAME="Bund_City_V1_1" VERSION="2021-07-20" URI="mailto:localhost"></MODEL><MODEL NAME="Kanton_City_V1_1" VERSION="2021-07-20" URI="mailto:localhost"></MODEL></MODELS></HEADERSECTION>
<DATASECTION>
<Bund_City_V1_1.Constructions BID="325a144f-6d2c-4a17-8924-6a83fff8541b">
<Bund_City_V1_1.Constructions.Street TID="123"><Name>Mainstreet</Name></Bund_City_V1_1.Constructions.Street>
</Bund_City_V1_1.Constructions>
<Kanton_City_V1_1.Constructions BID="7d7558e7-194c-4787-9aa3-fb6a993a304f">
<Kanton_City_V1_1.Constructions.Building TID="2"><Name>Citybuilding No.1</Name><Street REF="123"></Street><Description>Nice Building</Description></Kanton_City_V1_1.Constructions.Building>
</Kanton_City_V1_1.Constructions>
</DATASECTION>
</TRANSFER>
```

#### Solution:
Assuming this behavior is perfectly correct from ili2db / interlis side we do need to put all the data from the base model into the basket of the extended model. Means something like this:
```
<Kanton_City_V1_1.Constructions BID="7d7558e7-194c-4787-9aa3-fb6a993a304f">
<Bund_City_V1_1.Constructions.Street TID="123"><Name>Mainstreet</Name></Bund_City_V1_1.Constructions.Street>
<Kanton_City_V1_1.Constructions.Building TID="2"><Name>Citybuilding No.1</Name><Street REF="123"></Street><Description>Nice Building</Description></Kanton_City_V1_1.Constructions.Building>
</Kanton_City_V1_1.Constructions>
```
### Scenario 3 - Extend all classes

Another solution would be this way of modelling.
```
INTERLIS 2.3;

MODEL Bund_City_V1_1 (en) AT "mailto:localhost" VERSION "2021-07-20" =
  TOPIC Constructions =
    CLASS Building  =
      Name : MANDATORY TEXT*99;
    END Building;
    CLASS Street  =
      Name : MANDATORY TEXT*99;
    END Street;
    CLASS Cooperation  =
      Name : MANDATORY TEXT*99;
    END Cooperation;
    ASSOCIATION Building_Street =
      Street -- {0..1} Street;
      Building -- {0..*} Building;
    END Building_Street;
  END Constructions;

END Bund_City_V1_1.
MODEL Kanton_City_V1_1 (en) AT "mailto:localhost" VERSION "2021-07-20" =
  IMPORTS Bund_City_V1_1;
  TOPIC Constructions EXTENDS Bund_City_V1_1.Constructions =
    CLASS Building (EXTENDED) =
      Description : TEXT*99;
    END Building;
    CLASS Street (EXTENDED) =
    END Street;
  END Constructions;
END Kanton_City_V1_1.
```

Here we extend the `Street` without any changes. But with this we have the `Street` in the topic `Kanton_City_V1_1` and so we have it in the basket:
```
<Kanton_City_V1_1.Constructions BID="405b6f5c-6cb5-4c0e-b1ab-a4bd30ffe7fe">
<Kanton_City_V1_1.Constructions.Street TID="123"><Name>canton street</Name></Kanton_City_V1_1.Constructions.Street>
<Kanton_City_V1_1.Constructions.Building TID="2"><Name>city building</Name><Street REF="123"></Street><Description>nice building</Description></Kanton_City_V1_1.Constructions.Building>
</Kanton_City_V1_1.Constructions>
```

See model https://models.geo.zg.ch/ARV/ZG_Nutzungsplanung_V1_1.ili?

> I found out that this model from Zug (in call with Manuel), that the `EXTENDED` here is maybe because of the OID and not the Basket-Situation

### Korrespondenz mit Peter Staub
Ich:
> Entweder also muss der Model Baker erlauben, dass man referenzierte Klassen des Basistopics im Basket des erweiterten Topics speichert (wie es bei den Kantonsdaten der Fall war, die du mir zu GL_Wildruhezonen gegeben hast und im Issue unter Scenario 2 beschrieben ist) oder man muss den User motivieren in seinen Modellen alle referenzierten Klassen ohne Änderungen zu erweitern (wie es Manuel im Zuger Modell gemacht hat und in Scerario 3 beschrieben ist).
Welches Vorgehen wär deiner Meinung nach das Richtige?

Er:
> M.E. sollte man grundsätzlich immer vermeiden, eine bestimmte Modellierungsart zu suggerieren. Es können immer «solche und solche Modelle» vorkommen.
Bei den Erweiterungen würde ich mich deshalb _nicht_ darauf verlassen, dass nach Methode Zug verfahren wird! INTERLIS lässt «EXTENDS» und «(EXTENDED)» zu, deshalb müsste auch der Model Baker damit so sauber wie möglich umgehen können…
 
Ich:
> Ich geh mit dir einig, dass der Modelbaker mit beidem umgehen können soll und mindestens in der Auswahl der Klassen aus dem Basistopic auch den Basket des erweiterten Topics enthalten soll. 
Was würdest du denn empfehlen in der modellierung? Landesgeologie zBs. hat gerade diesen Fall bei einem internen Modell, das noch nicht definitiv ist.
Die "Methode-Zug" ist meiner Meinung nach konsistenter bei der Verwendung von Baskets, als konzeptionelles Datenmodell hingegen erscheint mir das erweitern von Klassen nur um sie im erweiterten Topic zu haben aber auch etwas "clumbsy".

Er: 
> Also, wenn man ein Topic erweitert, dann sind alle Klassen des erweiterten Basismodells sowieso auch verfügbar!
Man muss sie also nicht explizit mit (EXTENDED) noch «reinziehen».
Das ist vielleicht eine Fehlkonzeption der Vererbung.

Siehe dazu auch die Antwort von Claude unten...
 

>(EXTENDED) ist nur zu verwenden, wenn man zu einer bestehenden Klasse z.B. weitere Attribute hinzufügen will oder insbesondere dann, wenn man bereits definierte Attribute erweitert (z.B. TEXT zu TEXT*25). Die Grundsemantik der erweiterten Klasse bleibt dabei gleich.

> EXTENDS ist dann zu wählen, wenn man die Grundsemantik verändert bzw. erweitert und grundsätzlich neue Eigenschaften modelliert. Insbesondere dann wenn abstrakte Klassen aus einem Basismodell zu mehreren konkreten Klassen mit unterschiedlichen Eigenschaften ausmodelliert werden.


Ich:

> Was mich aber verwirrt ist, dass die vererbten Klassen (die nicht erweitert wurden sondern mit dem erweiterten Topic mitkamen - im WRZ von GL zBs. die Teilgebiete) noch immer im Topic des Basismodells sind (zBs. im exportierten XTF) und doch nicht im eigenen Basket des Basistopics sein dürfen, sondern in dem des erweiterten Topics (da die Objekte ja mit denen der erweiterten Klassen verknüpft sind). Das wiederspricht der Struktur, dass en Basket nicht Daten aus mehr aIs einem Topic umfasst. Ich denke das könnte der Grund sein, weshalb Zug diese unschönen Erweiterungen machte. So sind alle benutzten Klassen im erweiterten Topic verfügbar. Aber eben, grundsätzlich gehören ja - wie du sagst - diese Klassen des Basismodells zum erweiterten Topic. Komisch dann aber, dass sie als Objekte des Basismodells gespeichert werden (im XTF).

### Korrespondenz mit Manuel Kaufmann

Ich: 
> Wenn ich eine Klasse A erweitere, die aber noch immer auf eine Klasse B des Basismodelles referenziert, hab ich ein Problem, da Klasse B nicht in meinem Basket liegt. Es gibt zwei Möglichkeiten das zu lösen:
> 1. Ich schau dass die Einträge der Klasse B im gleichen Basket der Klasse A sind. Dies ist nicht so schön, weil ich dann Daten mehrerer Topics im gleichen Basket habe.
> 2. Ich erweitere auch die Klasse B. Auch wenn ich sie dadurch nicht verändere. Ich hab dann Klasse B in meinem Basket und somit keine Probleme.
> Der zweite Ansatz wird hier verwendet https://models.geo.zg.ch/ARV/ZG_Nutzungsplanung_V1_1.ili oder weshalb sonst werden die Klassen alle erweitert ohne verändert zu werden? Was hältst du vom ersten Ansatz? Ist das auch ein gangbarer Weg oder eher bad practice in der Modellierung?

Er: 
> Ich kann mich erinnern, dass ich mich an dem https://models.geo.zg.ch/ARV/ZG_Nutzungsplanung_V1_1.ili ziemlich die Zähnen ausgebissen hatte.
> Ziel war ja, dass ili2db aus dem erweiterten kantonalen Modell, sauber die «darunterliegenden» Daten des MGDM des Bundes exportieren kann, die dann dem Bund geliefert werden.
> Ich hatte damals Claude befragt und auch gesehen, dass es Peter Staub gleich gemacht hat (er hat wahrscheinlich auch Claude konsultiert ;-)
> Siehe auch: https://models.geo.gl.ch/pdf/GL_Nutzungsplanung_V1_4.pdf (Seite 11 unten)
> Grundsätzlich muss man immer im Kopf behalten: TOPIC (Modell) = Basket (Daten). Man kann nicht Daten von mehreren Topics in ein Basket versorgen bzw. umgekehrt Daten in verschiedenen Baskets halten, wenn sie gemäss Modell in einem Topic definiert sind.
> Wenn man ein ganzes Modell erweitert, läuft dann dass darauf hinaus, dass alle Klassen und Topics erweitert werden müssen, die man im neuen Modell auch über die neuen Topics/Klassen erfassen will, auch wenn darin inhaltlich de facto nichts dazu kommt.
> Ich habe die diversen Issues diesbezüglich kommentiert (auch derjenige mit den doppelten Namen).
Bei deinem Beispiel https://github.com/opengisch/QgisModelBaker/issues/671  ist das Hauptproblem aber noch ein anderes: Die erfassten Daten (Scenario 2) stimmen nicht mit der definierten Association überein (beide beteiligten Klassen müssen im selben Basket sein). Hier muss die Klasse erweitert werden, weil sonst die Association nicht mehr erfüllt werden kann.

Ich:
> Die Erweiterungen der einzelnen Klassen machen bei deinem Modell natürlich auch schon nur wegen der hinzugefügten OID Sinn. Das hab ich bisher übersehen. 
> Und ja, so liegen alle Daten im gleichen Basket und es ist so wie es sein soll (Daten-Basket = Struktur-Topic).

> Doch mein Beispiel im Scenario 2 funktioniert auch und die Daten sind gemäss ili2db valide. Es müssen einfach alle Daten der (nicht erweiterten) Basisklasse street dem gleichen Basket zugewiesen werden, wie die referenzierenden Buildings. Es sieht dann so aus wie im Issue gequotet. (Aber eben: Daten-Basket != Struktur-Topic, doch da die street ja durch die Topic-Erweiterung irgendwie auch zur Erweiterung gehört, wenn auch in der Struktur der Basisklasse).

> Grundsärzlich betrachtet, sind ja bei einer Erweiterung des Topics alle Klassen verfügbar. Konzeptionell erwartet man also nicht, dass man alle Klassen, die man verwenden möchte erweitern muss, ohne etwas dran zu ändern. Dies macht man explizit im Gedanken an die physikalische Umsetzung und die Funtionsweise mit den Baskets. Oder liege ich da ganz falsch?

> Da das Modell https://models.geo.gl.ch/ili/GL_Wildruhezonen_V1_2020-03-31.ili so wie mein Scenario 2 aufgebaut ist, habe ich auch mal bei Peter nachgefragt. Allerdings eher, ob Model Baker diese Art unterstützen können muss. Und er sagte mir, dass man auch von solchen Modellierungsarten ausgehen können soll. Auf die spezifische Frage, ob dies denn okay sei mit den Daten mehrerer Topics im selben Basket, hat er noch nicht geantwortet.

Und im Telefongespräch kamen wir dann drauf, dass das Scenario 2 eigentlich cooler ist. Problem nur dass Basket != Topic. Aber siehe Claudes Response.

Er war etwas verwundert, dass die Classen des Basismodells nicht automatisch vererbt wurden. Denkt er habe es vermutlich deshalb EXTENDED. Ausserdem aber werden seine Klassen auch wegen den OID EXTENDED.
Aussage von mir: Es ist nicht so schön, wenn der Modeller sich darum kümmern muss, wie die Daten dann abgebildet werden.
Aussage von ihm: Es ist aber auch nicht so toll, wenn der Benutzer sich darum kümmern muss. Da macht der Modeller besser ein stabiles Ding für ihn.


### Korrespondenz mit Claude Eisenhut

Ich hab das Problem beschrieben. Und dann gefragt:
> Und ich arbeite mit Baskets. Wenn ich ein Topic pro Basket und umgekehrt habe, komme ich auf diese (invaliden) Daten. Invalid deshalb, weil die Referenz <Street REF="123"></Street> auf ein Objekt ausserhalb des Topics zeigt.

```
<Bund_City_V1_1.Constructions BID="325a144f-6d2c-4a17-8924-6a83fff8541b">
<Bund_City_V1_1.Constructions.Street TID="123"><Name>Mainstreet</Name></Bund_City_V1_1.Constructions.Street>
</Bund_City_V1_1.Constructions>
<Kanton_City_V1_1.Constructions BID="7d7558e7-194c-4787-9aa3-fb6a993a304f">
<Kanton_City_V1_1.Constructions.Building TID="2"><Name>Citybuilding No.1</Name><Street REF="123"></Street><Description>Nice Building</Description></Kanton_City_V1_1.Constructions.Building>
</Kanton_City_V1_1.Constructions>
```

Er sagte dann dazu:
> Das ist nicht was der Nutzer erwartet...aber ohne --createBasketCol kann ili2db beim Export nur aufgrund des Topics in dem die Klasse definiert ist, den Basket "erfinden".

Und dann zeigte ich ihm den Output wo alles im Basket `Kanton_City_V1_1.Constructions` liegt, wenn auch im Format des `Bund_City_V1_1.Constructions`.

Er meint:
> Ja, darum erzwingen neue ili2db (>=4.7.0) Versionen bei diesem Modell --createBasketCol. Damit Objekte der nicht erweiterten Klassen dem richtigen Basket zugeorndet werden können.

Und dann:

> Ein Basket ist eine Instanz eines TOPICs. Ein Basket kann Objekte enthalten, die gemäss dem TOPIC zulässig sind. Und Objekte sind Instanzen einer Klasse. Und die Klassen dieser Objekte können in dem TOPIC (des Baskets) oder einem Basis-TOPIC dieses TOPICs definiert sein. Darum haben dann die XML-Elementnamen der Objekte verschiedene MODEL+/TOPIC-Namen. (Ein ähnliches Problem entsteht bei ili-2.4 mit dem XML-Namesapces der Attribute; die dann pro Objekt verschieden sein können, je nachdem in welcher Klasse das Attribut definiert wurde)

Zur Lösung mit allen Klassen `EXTENDED` haben meinte er:
> Das finde ich falsch, weil diese Modellierung nicht fachlich motiviert ist; aber kann man nicht verhindern.
(Das macht nur Sinn wenn man so eine eigenständige Kodierung der Assoziation verhindern kann)