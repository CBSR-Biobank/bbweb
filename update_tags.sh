#!/bin/bash

# this script requires Universal Ctags to be installed

TMPFILE_SCALA="$(mktemp)"
TMPFILE_JS="$(mktemp)"
find app  -type f -name \*.scala > $TMPFILE_SCALA
find test -type f -name \*.scala >> $TMPFILE_SCALA
find app  -type f -name \*.js > $TMPFILE_JS
find test -type f -name \*.js >> $TMPFILE_JS

echo "TMPFILE_SCALA is: $TMPFILE_SCALA"
echo "TMPFILE_JS is: $TMPFILE_JS"

exctags --output-format=etags -eL $TMPFILE_SCALA -f SCALA_TAGS
exctags --output-format=etags -eL $TMPFILE_JS -f JS_TAGS

# - having problems setting up gnu global to use universal ctags
# - commenting out for now
#gtags -v --gtagslabel new-ctags -f $TMPFILE
