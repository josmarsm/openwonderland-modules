#!/bin/sh
echo building all
./buildall.csh > b.out 2>& 1
echo built all
grep -i fail b.out
echo copying files
./cp.sh
