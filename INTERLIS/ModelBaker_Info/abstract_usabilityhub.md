Die Idee des UsabILIty Hub ist es, für Implementierte INTERLIS Modelle Zusatzinformationen automatisch übers Web zu empfangen. So wie wir Modelle durch die Anbindung der Datei ilimodels.xml von http://models.interlis.ch und den verknüpften Repositories erhalten können, können wir die Zusatzinformationen mit der Datei ilidata.xml auf dem UsabILIty Hub (derzeit https://models.opengis.ch) und den verknüpften Repositories erhalten. Einstellungen für Tools werden in einer Metakonfigurationsdatei konfiguriert, ebenso wie Links zu Toppingfiles, die Informationen zu GIS Projektes enthalten (wie zBs. Symbologien oder Legendenstrukturen). Somit bestehen diese Zusatzinformationen meistens aus einer Metakonfiguration und beliebig vielen Toppings. 


Empfangen von Metainformation automatisch übers Web.

Die Idee des UsabILIty Hub ist es, für Implementierte INTERLIS Modelle Zusatzinformationen automatisch übers Web zu empfangen. So wie wir jetzt Modelle durch die Anbindung der Datei ilimodels.xml von http://models.interlis.ch - und mit der Datei ilisite.xml die Modelle vieler anderen Repositories - erhalten können, können wir die Zusatzinformationen mit der Datei ilidata.xml auf dem UsabILIty Hub (derzeit https://models.opengis.ch) - und mit der Datei ilisite.xml die Modelle vieler anderen Repositories - erhalten.

Konfigurationen für ili2db, Modelbaker und andere Tools sind im
Metakonfigurationsfile (INI-File) definiert.
Genauso kann dieses File auch Links (Ids) enthalten zu:
- anderen Metakonfigurationsfiles
- Toppingfiles
- Katalogen

Diese Toppingfiles enthalten wiederum Informationen zu
Symbologien und Formularkonfigurationen (QML), Legendenlayout
und Layer-Reihenfolge (YAML) etc.

Metakonfigurationsfiles (INI-File) können im ilidata.xml über den Modellnamen (oder auch die Id) gefunden werden.

Toppingfiles können im ilidata.xml über die Id gefunden werden.


---
# UsabILIty Hub
The idea of the UsabILItyHub is to receive meta data like ili2db settings, layer styles and orders etc. automatically over the web. 

Like we can now receive models by connecting the ilimodels.xml of http://models.interlis.ch and with it's ilisite.xml many other repositories, we will be able to get this meta data with the file ilidata.xml on the UsabILItyHub usabilityhub.opengis.ch

Find more information (in German) at https://usabilityhub.opengis.ch/


