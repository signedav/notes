## Commandos (normales Vorgehen)

Ins GitHub Verzeichnis gehen

    cd dev/cpp/QGIS/
    
Status abfragen, listet die Files mit Änderungen auf
    
    82  git status

Auschecken von Folder überschreibt die geänderten Dateien
    
    83  git checkout -- src/gui/

Änderungen anzeigen zwischen commit und working tree

    85  git diff 

Auschecken (erstellen) eines Branches

    86  git checkout -b stringliteral

Adden in die Commit-Liste

    88  git add .

Lokales commiten

    90  git commit
    g
Anzeigen von lokalen und nicht-lokalen Änderungen

    93  git log

Hochladen der Commits

    103  git push -u ghdave stringliteral

... oder wenn der Branch schon mit dem Fork verknüpft nur noch...

    120  git push
  
... um den Committext nochmals zu bearbeiten
    
    git commit --amend


## Sonstige Commands

Adden meiner Fork

    96  git remote add ghdave git@github.com:signedav/QGIS.git

Anpassen der Git Config

    98  gedit .git/config
(ersetze origin mit qgis)

oder mit diesem command:
    git remote add ghdave git@github.com:signedav/QGIS.git | sed -i 's/origin/qgis/g' 

oder im Browser fork und dann:

    git clone git@github.com:signedav/QGEP.git
 

Layouten mit Script

    106  ./scripts/prepare-commit.sh

Nicht mehr nötig, da es hinzugefügt zu 

    117  ln -s ../../scripts/prepare-commit.sh .git/hooks/pre-commit


## Commit Message

```
QStringLiteral is for performance improvement
Fix #123256
# Please enter the commit message for your changes. Lines starting
# with '#' will be ignored, and an empty message aborts the commit.
# On branch stringliteral
# Changes to be committed:
#       modified:   src/gui/qgscodeeditorpython.cpp
#
```

## backporten auf zBs. alte Releases
--> checkout Release branch

    git checkout release-2_18
    
klappte bei mir mal nicht, das hingegen ist gegangen:
`git fetch origin release-3_2:release-3_2`

`git pull`

klappte bei mir mal nicht, das hingegen ist gegangen:

` git pull origin release-3_4 `

neuer branch von dort aus erstellen

    git checkout -b neuerbranch
    
die andern änderungen anzeigen

    git log alterbranch
    
mit cherry-pick die commits kopieren

    git cherry-pick <nummerdescommits>

evtl noch bisschen squashen:

git rebase -i <after-this-commit>

## Neuer Ordner mit altem Branch

cd QGIS_properties

git init

git clone git@github.com:signedav/QGIS.git

cd QGIS

git checkout propertieslayout

git pull

... und fertig :-)

## NEUER BRANCH um etwas anderes zu machen
Bin jetzt auf dem duplicateFeatureCpp

1 wännd willsch chasch pushe (meh als backup, musch ja no kein pull request mache)

2 nd dänn machsch `git checkout master`
(evtl `git pull`)

3`git checkout -b dupclicateFeaturePy`

4 git add ... git commit ...

5 git push ...

pull request

und nacher `git checkout duplicateFeatureCpp`


## Checkout branch von PR von jemand anderem:
```
git fetch origin pull/ID/head:BRANCHNAME
```
Switch to the new branch that's based on this pull request:
```
[master] $ git checkout BRANCHNAME
```
Switched to a new branch 'BRANCHNAME'

Bsp mit Issue #7909 von m-kuhn:gpkg_offline_editing
```
git fetch origin pull/7909/head:gpkg_offline_editing
```
```
[master] $ git checkout gpkg_offline_editing
```


## Wenn git kaputt
```
Create a backup of the corrupt directory:
cp -R foo foo-backup
Make a new clone of the remote repository to a new directory:
git clone git@www.mydomain.de:foo foo-newclone

-- evt. mach das hier gar nicht:
Delete the corrupt .git subdirectory:
rm -rf foo/.git
Move the newly cloned .git subdirectory into foo:
mv foo-newclone/.git foo
Delete the rest of the temporary new clone:
rm -rf foo-newclone
```

## Save Way:
Mach neuer Clone vom QGIS\QGIS:
`[david@localhost qgis]$ git clone git@github.com:qgis/QGIS.git QGIS-3_0`
Füge deinen Fork zum Config:
`[david@localhost QGIS-3_0]$ vim .git/config`
und dort so:
```
[remote "ghdave"]
        url = git@github.com:signedav/QGIS.git
        fetch = +refs/heads/*:refs/remotes/ghdave/*
```
Und dann kannst du den master checkouten (wenn du nicht schon hast) + neuer Branch erstellen.

`[david@localhost QGIS-3_0]$ git checkout -b tralala`

Und dann kannst du die Änderung auf deinen Fork pushen:

`[david@localhost QGIS-3_0]$ git push ghdave tralala`

*Also der Unterschied zum sonst pushen (wie in QField) ist eigentlich nur dass du den ghdave hinzugefügt hast*

**Wenn jetzt änderungen von extern zu meinem Branch hinzugekommen sind. Muss ich den pullen. Allerdings mit dem Remotebranch. So:"
```
git push ghdave translation_project

```

## Checkout PR branch of someone else:

Listen der verfügbaren Branches
```
git ls-remote --refs origin
```
dann auschecken:

```
git fetch origin pull/123/head:pr/123
git checkout pr/123
```

Source: https://github.community/t5/How-to-use-Git-and-GitHub/Checkout-a-branch-from-a-fork/td-p/77

oder des Repos:

```
git fetch git@github.com:3nids/QGIS.git edit-widget-specific-config
```