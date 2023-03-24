# Linux 
Installed Fedora 29 with USB Stick made with Fars Windows Kiste

# PostgreSQL
Source: https://fedoraproject.org/wiki/PostgreSQL
```
sudo dnf install postgresql-server postgresql-contrib
sudo systemctl enable postgresql
sudo postgresql-setup --initdb --unit postgresql
sudo postgresql-setup --initdb --unit postgresql
```
and in `/var/lib/pgsql/data/pg_hba.conf`:
```
  host    all             all             127.0.0.1/32            md5
  host    all             all             ::1/128                 md5
```

## Postgis
```
sudo dnf install postgis.x86_64
```
(installation from things like `postgis25_11` did not work)


## PG Admin
```
sudo dnf install pgadmin3
```
Could not connect first to localhost with `postgres`

so I did:
```
sudo -u postgres psql
```
and there:
```
ALTER USER postgres PASSWORD 'postgres';
```

# Google Chrome 
Source: https://www.linuxbabe.com/fedora/install-google-chrome-fedora-workstation
```
sudo dnf install fedora-workstation-repositories
sudo dnf config-manager --set-enabled google-chrome
sudo dnf install google-chrome-stable -y
```

# QGIS Installation
## Normal QGIS local installation

```

```
# Development environment
I don't realy remeber every fuck I had to do... But I try to write down the most important stuff.
## QT
To install the QT I downloaded the `run` file from the official QT download page. I had to set it to executable and then I ran `./qt...run`
Fine. I had it then in the folder `home/signedav/Qt...` etc.
Is it needed? 

## QGIS on QT Creator
To configure the QGIS stuff I followed this instruction (there is a section for Fedora): https://htmlpreview.github.io/?https://github.com/qgis/QGIS/blob/master/doc/INSTALL.html#toc4

**Important** You have to add the QT path to the installed qt manually, it solves a lot of pain...

and I disabled clang at Help -> About plugins... not sure if this is good...

```
CMAKE_CXX_COMPILER:STRING=%{Compiler:Executable:Cxx}
CMAKE_C_COMPILER:STRING=%{Compiler:Executable:C}
CMAKE_PREFIX_PATH:STRING=%{Qt:QT_INSTALL_PREFIX}
QT_QMAKE_EXECUTABLE:STRING=%{Qt:qmakeExecutable}
```

# GIT
Source: https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/
```
ssh-keygen -t rsa -b 4096 -C "david@opengis.ch"
```
(Passphrase ist usual Password)

Für proper Copy to Clipboard:
```
sudo dnf install xclip
xclip -sel clip < ~/.ssh/id_rsa.pub

```
Then clone of my branch of qgis e.g.

The `.git/config` of QGIS is:
```
[core]
        repositoryformatversion = 0
        filemode = true
        bare = false
        logallrefupdates = true
[remote "origin"]
        url = git@github.com:QGIS/QGIS.git
        fetch = +refs/heads/*:refs/remotes/origin/*
[remote "ghdave"]
        url = git@github.com:signedav/QGIS.git
        fetch = +refs/heads/*:refs/remotes/origin/*
[branch "master"]
        remote = origin
        merge = refs/heads/master
```

# Freeze 
I did this, I found it somewhere, no idea if it helps:
```
sudo dnf --enablerepo updates-testing update gjs

```
Touchpad deaktivieren:
```
[signedav@localhost ~]$ xinput list
⎡ Virtual core pointer                    	id=2	[master pointer  (3)]
⎜   ↳ Virtual core XTEST pointer              	id=4	[slave  pointer  (2)]
⎜   ↳ xwayland-pointer:17                     	id=6	[slave  pointer  (2)]
⎜   ↳ xwayland-relative-pointer:17            	id=7	[slave  pointer  (2)]
⎜   ↳ xwayland-touch:17                       	id=9	[slave  pointer  (2)]
⎣ Virtual core keyboard                   	id=3	[master keyboard (2)]
    ↳ Virtual core XTEST keyboard             	id=5	[slave  keyboard (3)]
    ↳ xwayland-keyboard:17                    	id=8	[slave  keyboard (3)]
[signedav@localhost ~]$ xinput disable 9

```
Evtl. auch Keyboard problem...

```
Feb 12 01:43:54 localhost.localdomain google-chrome.desktop[1726]: [2295:2295:0212/014354.569145:ERROR:textfield.cc(1777)] Not implemented reached in virtual bool views::Textfield::Shou>
Feb 12 01:42:48 localhost.localdomain gnome-shell[1221]: Connection to xwayland lost
Feb 12 01:42:45 localhost.localdomain google-chrome.desktop[1726]: [2341:2341:0212/014245.893854:ERROR:sandbox_linux.cc(364)] InitializeSandbox() called with multiple threads in process>
Feb 12 01:42:43 localhost.localdomain gnome-shell[1726]: The property vignette_sharpness doesn't seem to be a normal object property of [0x55fc46600940 StWidget] or a registered special>
Feb 12 01:42:43 localhost.localdomain gnome-shell[1726]: The property brightness doesn't seem to be a normal object property of [0x55fc46600940 StWidget] or a registered special property
Feb 12 01:42:43 localhost.localdomain gnome-shell[1726]: The property vignette_sharpness doesn't seem to be a normal object property of [0x55fc463c5890 StWidget] or a registered special>
Feb 12 01:42:43 localhost.localdomain gnome-shell[1726]: The property brightness doesn't seem to be a normal object property of [0x55fc463c5890 StWidget] or a registered special property
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: GNOME Shell started at Tue Feb 12 2019 01:42:40 GMT-0500 (EST)
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: STACK_OP_ADD: window 0x1800001 already in stack
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: STACK_OP_ADD: window 0x1800001 already in stack
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported maximum keycode 374, clipping.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Error looking up permission: GDBus.Error:org.freedesktop.portal.Error.NotFound: No entry for geolocation
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: nma_mobile_providers_database_lookup_cdma_sid: assertion 'sid > 0' failed
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: clutter_actor_destroy: assertion 'CLUTTER_IS_ACTOR (self)' failed
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Object St.Widget (0x55fc464c5740), has been already deallocated — impossible to access it. This might be caused by the object ha>
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: clutter_actor_destroy: assertion 'CLUTTER_IS_ACTOR (self)' failed
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #9   55fc44be9350 i   resource:///org/gnome/shell/ui/background.js:522 (7f8c10c0bdc0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #8   7ffd369dd620 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #7   55fc44be93d0 i   resource:///org/gnome/shell/ui/layout.js:600 (7f8c10c045e0 @ 90)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #6   7ffd369dc680 I   resource:///org/gnome/gjs/modules/_legacy.js:82 (7f8c10fb0b80 @ 71)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #5   55fc44be9450 i   resource:///org/gnome/shell/ui/layout.js:655 (7f8c10c04670 @ 533)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #4   7ffd369db780 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #3   55fc44be9508 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:225 (7f8bdbc35>
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #2   55fc44be9588 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc35>
lines 8-31
Feb 12 01:42:43 localhost.localdomain gnome-shell[1726]: The property brightness doesn't seem to be a normal object property of [0x55fc463c5890 StWidget] or a registered special property
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: GNOME Shell started at Tue Feb 12 2019 01:42:40 GMT-0500 (EST)
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: STACK_OP_ADD: window 0x1800001 already in stack
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: STACK_OP_ADD: window 0x1800001 already in stack
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported maximum keycode 374, clipping.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Error looking up permission: GDBus.Error:org.freedesktop.portal.Error.NotFound: No entry for geolocation
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: nma_mobile_providers_database_lookup_cdma_sid: assertion 'sid > 0' failed
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: clutter_actor_destroy: assertion 'CLUTTER_IS_ACTOR (self)' failed
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Object St.Widget (0x55fc464c5740), has been already deallocated — impossible to access it. This might be caused by the object having been destroyed from C code>
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: clutter_actor_destroy: assertion 'CLUTTER_IS_ACTOR (self)' failed
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #9   55fc44be9350 i   resource:///org/gnome/shell/ui/background.js:522 (7f8c10c0bdc0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #8   7ffd369dd620 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #7   55fc44be93d0 i   resource:///org/gnome/shell/ui/layout.js:600 (7f8c10c045e0 @ 90)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #6   7ffd369dc680 I   resource:///org/gnome/gjs/modules/_legacy.js:82 (7f8c10fb0b80 @ 71)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #5   55fc44be9450 i   resource:///org/gnome/shell/ui/layout.js:655 (7f8c10c04670 @ 533)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #4   7ffd369db780 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #3   55fc44be9508 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:225 (7f8bdbc35280 @ 10)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #2   55fc44be9588 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc353a0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #1   7ffd369da640 b   self-hosted:261 (7f8c10fc1dc0 @ 223)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #0   55fc44be9608 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc35430 @ 15)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: == Stack trace for context 0x55fc448161a0 ==
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #9   55fc44be9350 i   resource:///org/gnome/shell/ui/background.js:522 (7f8c10c0bdc0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #8   7ffd369dd620 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #7   55fc44be93d0 i   resource:///org/gnome/shell/ui/layout.js:600 (7f8c10c045e0 @ 90)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #6   7ffd369dc680 I   resource:///org/gnome/gjs/modules/_legacy.js:82 (7f8c10fb0b80 @ 71)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #5   55fc44be9450 i   resource:///org/gnome/shell/ui/layout.js:655 (7f8c10c04670 @ 533)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #4   7ffd369db780 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #3   55fc44be9508 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:225 (7f8bdbc35280 @ 10)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #2   55fc44be9588 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc353a0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #1   7ffd369da640 b   self-hosted:261 (7f8c10fc1dc0 @ 223)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #0   55fc44be9608 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc35430 @ 15)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: == Stack trace for context 0x55fc448161a0 ==
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Object St.Widget (0x55fc46394f90), has been already deallocated — impossible to access it. This might be caused by the object having been destroyed from C code>
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: Telepathy is not available, chat integration will be disabled.
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: glamor: No eglstream capable devices found
Feb 12 01:42:36 localhost.localdomain gnome-shell[1221]: nma_mobile_providers_database_lookup_cdma_sid: assertion 'sid > 0' failed
Feb 12 01:42:28 localhost.localdomain gnome-shell[1221]: JS WARNING: [resource:///org/gnome/shell/ui/windowManager.js 1564]: reference to undefined property "MetaWindowXwayland"
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: > Warning:          Unsupported maximum keycode 374, clipping.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:28 localhost.localdomain gnome-shell[1221]: Error looking up permission: GDBus.Error:org.freedesktop.portal.Error.NotFound: No entry for geolocation
Feb 12 01:42:27 localhost.localdomain gnome-shell[1221]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 01:42:27 localhost.localdomain gnome-shell[1221]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 01:42:27 localhost.localdomain gnome-shell[1221]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
lines 8-70
Feb 12 01:42:43 localhost.localdomain gnome-shell[1726]: The property brightness doesn't seem to be a normal object property of [0x55fc463c5890 StWidget] or a registered special property
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: GNOME Shell started at Tue Feb 12 2019 01:42:40 GMT-0500 (EST)
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: STACK_OP_ADD: window 0x1800001 already in stack
Feb 12 01:42:42 localhost.localdomain gnome-shell[1726]: STACK_OP_ADD: window 0x1800001 already in stack
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported maximum keycode 374, clipping.
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Error looking up permission: GDBus.Error:org.freedesktop.portal.Error.NotFound: No entry for geolocation
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: nma_mobile_providers_database_lookup_cdma_sid: assertion 'sid > 0' failed
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: clutter_actor_destroy: assertion 'CLUTTER_IS_ACTOR (self)' failed
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Object St.Widget (0x55fc464c5740), has been already deallocated — impossible to access it. This might be caused by the object having been destroyed from >
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: clutter_actor_destroy: assertion 'CLUTTER_IS_ACTOR (self)' failed
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #9   55fc44be9350 i   resource:///org/gnome/shell/ui/background.js:522 (7f8c10c0bdc0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #8   7ffd369dd620 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #7   55fc44be93d0 i   resource:///org/gnome/shell/ui/layout.js:600 (7f8c10c045e0 @ 90)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #6   7ffd369dc680 I   resource:///org/gnome/gjs/modules/_legacy.js:82 (7f8c10fb0b80 @ 71)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #5   55fc44be9450 i   resource:///org/gnome/shell/ui/layout.js:655 (7f8c10c04670 @ 533)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #4   7ffd369db780 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #3   55fc44be9508 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:225 (7f8bdbc35280 @ 10)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #2   55fc44be9588 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc353a0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #1   7ffd369da640 b   self-hosted:261 (7f8c10fc1dc0 @ 223)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #0   55fc44be9608 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc35430 @ 15)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: == Stack trace for context 0x55fc448161a0 ==
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #9   55fc44be9350 i   resource:///org/gnome/shell/ui/background.js:522 (7f8c10c0bdc0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #8   7ffd369dd620 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #7   55fc44be93d0 i   resource:///org/gnome/shell/ui/layout.js:600 (7f8c10c045e0 @ 90)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #6   7ffd369dc680 I   resource:///org/gnome/gjs/modules/_legacy.js:82 (7f8c10fb0b80 @ 71)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #5   55fc44be9450 i   resource:///org/gnome/shell/ui/layout.js:655 (7f8c10c04670 @ 533)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #4   7ffd369db780 b   resource:///org/gnome/gjs/modules/signals.js:128 (7f8c10fc18b0 @ 386)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #3   55fc44be9508 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:225 (7f8bdbc35280 @ 10)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #2   55fc44be9588 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc353a0 @ 17)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #1   7ffd369da640 b   self-hosted:261 (7f8c10fc1dc0 @ 223)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: #0   55fc44be9608 i   /usr/share/gnome-shell/extensions/background-logo@fedorahosted.org/extension.js:232 (7f8bdbc35430 @ 15)
Feb 12 01:42:41 localhost.localdomain org.gnome.Shell.desktop[1726]: == Stack trace for context 0x55fc448161a0 ==
Feb 12 01:42:41 localhost.localdomain gnome-shell[1726]: Object St.Widget (0x55fc46394f90), has been already deallocated — impossible to access it. This might be caused by the object having been destroyed from >
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: Telepathy is not available, chat integration will be disabled.
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 01:42:40 localhost.localdomain gnome-shell[1726]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:39 localhost.localdomain org.gnome.Shell.desktop[1726]: glamor: No eglstream capable devices found
Feb 12 01:42:36 localhost.localdomain gnome-shell[1221]: nma_mobile_providers_database_lookup_cdma_sid: assertion 'sid > 0' failed
Feb 12 01:42:28 localhost.localdomain gnome-shell[1221]: JS WARNING: [resource:///org/gnome/shell/ui/windowManager.js 1564]: reference to undefined property "MetaWindowXwayland"
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: > Warning:          Unsupported maximum keycode 374, clipping.
Feb 12 01:42:28 localhost.localdomain org.gnome.Shell.desktop[1221]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:28 localhost.localdomain gnome-shell[1221]: Error looking up permission: GDBus.Error:org.freedesktop.portal.Error.NotFound: No entry for geolocation
Feb 12 01:42:27 localhost.localdomain gnome-shell[1221]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 01:42:27 localhost.localdomain gnome-shell[1221]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 01:42:27 localhost.localdomain gnome-shell[1221]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 01:42:27 localhost.localdomain gnome-shell[1221]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 01:42:26 localhost.localdomain org.gnome.Shell.desktop[1221]: Errors from xkbcomp are not fatal to the X server
Feb 12 01:42:26 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   This warning only shows for the first high keycode.
Feb 12 01:42:26 localhost.localdomain org.gnome.Shell.desktop[1221]: >                   X11 cannot support keycodes above 255.
Feb 12 01:42:26 localhost.localdomain org.gnome.Shell.desktop[1221]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 01:42:26 localhost.localdomain org.gnome.Shell.desktop[1221]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 01:42:26 localhost.localdomain org.gnome.Shell.desktop[1221]: glamor: No eglstream capable devices found

```

```
lhost.localdomain gnome-shell[5823]: JS WARNING: [resource:///org/gnome/shell/ui/windowManager.js 1564]: reference to undefined property "MetaWindowXwayland"
Feb 12 05:53:43 localhost.localdomain org.gnome.Shell.desktop[5823]: Errors from xkbcomp are not fatal to the X server
Feb 12 05:53:43 localhost.localdomain org.gnome.Shell.desktop[5823]: >                   This warning only shows for the first high keycode.
Feb 12 05:53:43 localhost.localdomain org.gnome.Shell.desktop[5823]: >                   X11 cannot support keycodes above 255.
Feb 12 05:53:43 localhost.localdomain org.gnome.Shell.desktop[5823]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 05:53:43 localhost.localdomain org.gnome.Shell.desktop[5823]: >                   X11 cannot support keycodes above 255.
Feb 12 05:53:43 localhost.localdomain org.gnome.Shell.desktop[5823]: > Warning:          Unsupported maximum keycode 374, clipping.
Feb 12 05:53:43 localhost.localdomain org.gnome.Shell.desktop[5823]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 05:53:43 localhost.localdomain gnome-shell[5823]: Error looking up permission: GDBus.Error:org.freedesktop.portal.Error.NotFound: No entry for geolocation
Feb 12 05:53:43 localhost.localdomain gnome-shell[5823]: nma_mobile_providers_database_lookup_cdma_sid: assertion 'sid > 0' failed
Feb 12 05:53:43 localhost.localdomain gnome-shell[5823]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 05:53:43 localhost.localdomain gnome-shell[5823]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 05:53:43 localhost.localdomain gnome-shell[5823]: g_filename_to_utf8: assertion 'opsysstring != NULL' failed
Feb 12 05:53:43 localhost.localdomain gnome-shell[5823]: g_dir_open_with_errno: assertion 'path != NULL' failed
Feb 12 05:53:42 localhost.localdomain org.gnome.Shell.desktop[5823]: Errors from xkbcomp are not fatal to the X server
Feb 12 05:53:42 localhost.localdomain org.gnome.Shell.desktop[5823]: >                   This warning only shows for the first high keycode.
Feb 12 05:53:42 localhost.localdomain org.gnome.Shell.desktop[5823]: >                   X11 cannot support keycodes above 255.
Feb 12 05:53:42 localhost.localdomain org.gnome.Shell.desktop[5823]: > Warning:          Unsupported high keycode 372 for name <I372> ignored
Feb 12 05:53:42 localhost.localdomain org.gnome.Shell.desktop[5823]: The XKEYBOARD keymap compiler (xkbcomp) reports:
Feb 12 05:53:41 localhost.localdomain org.gnome.Shell.desktop[5823]: glamor: No eglstream capable devices found

```
```
```
```
```
```
```
```
```
```
```
```
```
```
```
```
