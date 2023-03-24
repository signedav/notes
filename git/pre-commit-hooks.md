Contributing to QGIS
====================

QGIS is an open source project and we appreciate contributions very much.

Proper formatting
-----------------

Before making a pull request, please make sure your code is properly formatted
by running the prepare commit script **before** issuing `git commit`.

    ./scripts/prepare-commit.sh

This can be automated by setting up the pre-commit hook properly.

    ln -s ../../scripts/prepare-commit.sh .git/hooks/pre-commit

Getting your pull request merged
--------------------------------

This is a volunteer project, so sometimes it may take a little while to merge
your pull request.

There is a [guide with hints for getting your pull requests merged](https://docs.qgis.org/testing/en/docs/developers_guide/git.html#pull-requests)
in the developers guide.

Ältere Notizen
====================


Für die Styles:

`vim .git/hooks`

und dort zuunterst:

`pre-commit@                       --> scripts/prepare-commit.sh `

Früher notierte ich mir folgendes:
Layouten mit Script

`./scripts/prepare-commit.sh`

Nicht mehr nötig, da es hinzugefügt zu

`ln -s ../../scripts/prepare-commit.sh .git/hooks/pre-commit`

**Oder falls das alles nicht geht:**

Ersetzten von `.git/hooks/pre-commit` mit dem File `../../scripts/prepare-commit.sh`
