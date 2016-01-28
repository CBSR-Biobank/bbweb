#!/bin/bash

# this script requires GNU global to be installed

TMPFILE="$(mktemp)"
find app  -type f -name \*.scala -print > $TMPFILE
find test -type f -name \*.scala -print >> $TMPFILE
find app  -type f -name \*.js    -print >> $TMPFILE
find test -type f -name \*.js    -print >> $TMPFILE

echo "TMPFILE is: $TMPFILE"

#ctags-exuberant -eL $TMPFILE
gtags -v --gtagslabel=ctags -f $TMPFILE
