# Boot goes to emergency mode

Instead of starting it goes to emergency mode. It says press ctrl-D to boot in default mode but this does not work - back to emergency mode.

I disabled Secure Boot but I think this had no influences.

I pressed `esc` during startup to get more information. I got this somewhere in red:
```
[FAILED]Failed to start File System Check on /dev/mapper/fedora-home.
See 'systemctl status "systemd-fsck@dev-mapper-fedora\\x2dhome.service"' ...
```

I checked `systemctl status "systemd-fsck@dev-mapper-fedora\\x2dhome.service"`

and it says:

```
[...]
UNEXPECTED INCONSISTENCY: run fsck MANUALLY. 
(i.e., without -a or -p option)
fsck failed with error code 4
[...]
Failed to start File System Check on /dev/mapper/fedora-home
[...]
```

Also startete ich:
```
fsck -c -p /dev/mapper/fedora-home
```
da ich nicht richtig gelesen hatte, dann später:
```
fsck -c -f /dev/mapper/fedora-home
```

Und dann fragte es ob es soll fixen. Ich so YES YES YES...

Und wieder gut :-)

Mehr Info auch hier: https://forums.fedoraforum.org/archive/index.php/t-299624.html