#!/bin/bash

# this script requires GNU global to be installed

TMPFILE="$(mktemp)"
find app -type f -name \*.scala -print > $TMPFILE
find test -type f -name \*.scala -print >> $TMPFILE

#find app -type f -print > $TMPFILE
#find test -type f -print >> $TMPFILE

#gtags -v -f $TMPFILE --gtagslabel ctags
#cat $TMPFILE | while read line; do etags -a $line; done

ctags -eL $TMPFILE

