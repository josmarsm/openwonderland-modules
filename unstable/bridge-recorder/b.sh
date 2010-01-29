#!/bin/sh
VOICEBRIDGE_DIR=/Users/bh37721/jvoicebridge/
echo BRIDGE RECORDER
echo building all
./buildall.csh > b.out 2>& 1
echo built all
grep -i fail b.out
echo copying files
./cp.sh
echo
echo VOICEBRIDGE
cd $VOICEBRIDGE_DIR
./b.sh
