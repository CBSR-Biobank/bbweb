#!/bin/bash

# this script requires GNU global to be installed

OUTFILE='gtags.files'
find app  -type f -name \*.scala -print > $OUTFILE
find test -type f -name \*.scala -print >> $OUTFILE
find app  -type f -name \*.js    -print >> $OUTFILE
find test -type f -name \*.js    -print >> $OUTFILE

#ctags -eL $OUTFILE
gtags -v --gtagslabel ctags
