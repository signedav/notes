Ich han mal widr Problem mit Kompiliere - wänn ich Q_GADGET und Q_ENUM verwände.
`undefined reference to 'QgsAtttributeEditorContext::staticMetaObject'` 

mkuhn [12:04 PM]
MOC_HDR im CMakeLists.txt

signedav [12:05 PM]
also döt iträge
okay.

mkuhn [12:05 PM]
und im HDRS usenä


----


han gad es anders Problem, wenn ich d'Klassedefinition vo `QmlExpression` ufgrund vo dim Input is cpp tuen, han ich en compile Error "undefined reference to `vtable for QmlExpression"
seit der das gad öppis?

mkuhn [5:32 PM]
oh ja
QObject müend dur de MOC (meta object compiler)
und de wird nur uf headers usgfüert
(cf CMakeLists.txt CORE_HDRS vs CORE_MOC_HDRS)


