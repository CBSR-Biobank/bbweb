#!/bin/bash

# this script requires GNU global to be installed

TMPFILE="$(mktemp)"
find app  -type f -name \*.scala -o -name \*.js > $TMPFILE
find test -type f -name \*.scala -o -name \*.js >> $TMPFILE

echo "TMPFILE is: $TMPFILE"

ctags --output-format=etags -eL $TMPFILE

# - having problems setting up gnu global to use universal ctags
# - commenting out for now
#gtags -v --gtagslabel new-ctags -f $TMPFILE
