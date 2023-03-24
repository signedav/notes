# Single Paragraph Abstract
Der *Model Baker* ist ein QGIS Plugin, mit dem sich QGIS Projekte schnell aus einem bestehenden INTERLIS Modell erzeugen lassen. Model Baker verwendet *ili2db* (Link), um ein INTERLIS Modell in eine PostGIS oder Geopackage Datenbank zu importieren und zusätzliche Metainformationen, um den Ebenenbaum, Feldwidgets mit Bedingungen, Formularlayouts, Relationen und vieles mehr automatisch zu konfigurieren. Es ist ein Open Source Projekt und der Code ist frei verfügbar: https://github.com/opengisch/QgisModelBaker

# Was ist Model Baker?
Der Model Baker ist ein QGIS Plugin, mit dem sich ein QGIS Projekt schnell aus einem physikalischen Datenmodell erstellen lässt. Der Model Baker analysiert die existierende Struktur und konfiguriert ein QGIS Projekt mit allen verfügbaren Informationen. Durch diese Automatisierung kann der Konfigurationsaufwand massiv gesenkt werden.

Modelle, die in INTERLIS definiert wurden, bieten zusätzliche Metainformationen wie Domains, Einheiten von Attributen oder objektorientierte Definitionen von Tabellen. Dies kann genutzt werden, um die Projektkonfiguration noch weiter zu optimieren. Der Model Baker verwendet [ili2db](https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst), um ein INTERLIS Modell in eine physikalische Datenbank zu importieren und die Metainformationen, um Ebenenbaum, Feldwidgets mit Bedingungen, Formularlayouts, Relationen und vieles mehr zu konfigurieren.

Ausserdem lässt sich der Model Baker auch als Framework für andere Projekte verwenden. Das Plugin [Asistente LADM-COL](https://github.com/SwissTierrasColombia/Asistente-LADM-COL), das für die [kolumbianische Umsetzung des Land Administration Domain Model (LADM)](https://www.proadmintierra.info/) erstellt wurde, nutzt den Model Baker als Library, um möglichst viel der spezifischen Lösung als QGIS Kernfunktionalität umzusetzen.


# Altes zeug
### Was ist Model Baker?
Der *Model Baker* ist ein QGIS Plugin, mit dem sich QGIS Projekte schnell und mit wenigen Mausklicks aus einem bestehenden INTERLIS Modell erzeugen lassen. Model Baker verwendet *ili2db* (Link), um ein INTERLIS Modell in eine PostGIS oder Geopackage Datenbank zu importieren und zusätzliche Metainformationen, um den Ebenenbaum, Feldwidgets mit Bedingungen, Formularlayouts, Relationen und vieles mehr automatisch zu konfigurieren.

#### Model Baker für die Projektgenerierung
Der Model Baker kann nicht nur QGIS Projekte aufgrund von INTERLIS Modellen erstellen. Er analysiert die existierende Struktur beliebiger PostGIS und GeoPackage Datenmodellen und konfiguriert ein QGIS Projekt mit allen verfügbaren Informationen. Durch diese Automatisierung kann der initiale Projektkonfigurationsaufwand massiv gesenkt werden. 

#### Optimierung auf INTERLIS
Modelle, die in Interlis definiert wurden bieten zusätzliche Metainformationen wie Domains, Einheiten von Attributen oder objektorientierte Definitionen von Tabellen. Dies kann genutzt werden um die Projektkonfiguration noch weiter zu optimieren. In kürzester Zeit steht einem Nutzer damit die umfassende Unterstützung für den gesamten Schweizer Geodatenkatalog zur Verfügung, der bereits im Interlis Format vorhanden ist.

#### Model Baker als Framework / Library
Darüber hinaus hat es sich als praktisches Framework für komplexere Projekte bewährt. Beispielsweise baut die [kolumbianische Umsetzung des Land Administration Domain Model (LADM)](https://www.proadmintierra.info/) auf Interlis in Kombination mit dem Projektgenerator auf. Während viele Fachschalen für fortgeschrittene Funktionen auf eigenen Python-Code setzen, wird in diesem Projekt der Ansatz gewählt, möglichst viel als QGIS Kernfunktionalitäten umzusetzen. Die verfügbaren Mittel werden sowohl für die Pluginentwicklung als auch für die Verbesserung von QGIS Funktionen eingesetzt. Damit sind viele Resultate für das ganze QGIS Ökosystem von Nutzen, ob in Kombination mit oder losgelöst vom Projektgenerator.
