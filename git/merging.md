Meistens sind es ja einigeCommits, seit dem Konflikt deshalb zuerst alles zu einem Commit machen:

Im aktuellen branch sein.
```
git rebase -i HEAD^^^^^^^^^^^^^^^^^^^ 
```
wobie die ^ die commits zurückzählen glaube ich
dort alle unter dem obersten des Projektes pick mit f ersetzen.
raus mit :x
```
git rebase upstream/master
git mergetool
```

dann mergen, wobei bei "meld" das mittlere das ist, wie es sein soll. Links ist das vom master und rechts das eigene mit den neuen änderungen. 

dann nicht commit sondern: 
```
git rebase --continue
```

und dann wieder ein pull...

**Neuliche Diskusion mit Kuhn**
Entweder:
```
git checkout master
git pull
git checkout -b nativemerge
git merge origin/nativefileselector
```
oder auch:
```
git fetch
git checkout origin/nativefileselector
git rebase origin/master
```
Ich schlug dann vor das:
`git checkout master` `git pull` `git checkout nativefileselector` `git rebase origin/master`

Und Matthias sagt, es sei in etwa das gleiche nähmlich:
```git pull```
isch gleich
```
git fetch
git merge origin/master
```

# Und so hats funktioniert:

```
git checkout master 
git pull
git checkout offline_editing
git rebase origin/master
git mergetool
```
dann im Meld alles in die Mitte fixen.
```
git rebase --continue
```
Dann mit `git status` hat man nicht viel gesehen allerdings mit `git log` das "neuere" commits anderer Menschen weiter unten drin sind...
```
git push ghdave offline_editing
```
hat nicht gklapt, deshalb force:
```
git -f push ghdave offline_editing
```


```


