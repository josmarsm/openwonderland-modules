#!/bin/bash 

# save_region.sh: a script that copies the content of a WFS snapshot and all associated artwork
# into a tar file. To save a particular region, X and Z offsets can be set in this script.
#
# usage: save_region.sh input_dir (output_dir)
#   input_dir: the wfs snapshot to save a copy of
#   output_dir: the optional directory to write output to. If no directory is specified, the
#               current directory will be used
#
# To use this script to save only a particular area of the world, change the values of the 
# XCENTER, XRADIUS, ZCENTER, and ZRADIUS variables. The values of X and Z center denote 
# where the center of the snapshot will be (i.e. this point will be at (0, 0) in the captured
# snapshot). The radius variables describe how far in each dimension to include objects. The 
# range will be a circle centered at the center with the given radius values.
#
# The DESC variable can be set to change the description in the snapshot.
#

grep=grep
tar=tar

# the center and radius in the x dimension
XCENTER=0
XRADIUS=100000

# the center and radius in the z dimension
ZCENTER=0
ZRADIUS=100000

# the description of this snapshot
#DESC="Default World - Town"

# calculated values
XMIN=$(($XCENTER - $XRADIUS)) 
XMAX=$(($XCENTER + $XRADIUS))
XOFF=$((0 - $XCENTER))

ZMIN=$(($ZCENTER - $ZRADIUS))
ZMAX=$(($ZCENTER + $ZRADIUS))
ZOFF=$((0 - $ZCENTER))


# copy all wfs files
copy_wfs () {
    if [ "$DESC" ]; then
        cat "$origdir/snapshot.xml" | sed -e "s|<description>.*</description>|<description>$DESC</description>|" > "$1/snapshot.xml"
    else
        cp "$origdir/snapshot.xml" "$1/snapshot.xml"
    fi

    for FILE in $origdir/world-wfs/*-wlc.xml; do
       in_range "$FILE" | while read RESULT; do
           if [ "$RESULT" ]; then
               copy_wfs_file "$1" "$RESULT"  
               copy_content "$RESULT"
               copy_models "$RESULT"
               copy_children "$1" "$RESULT"
           fi 
       done     
    done
}

# copy a single WFS file
copy_wfs_file () {
    i=`echo $2 | sed -e "s|$origdir|$1|"`
    cat "$2" | awk -v XOFF=$XOFF -v ZOFF=$ZOFF -v FILE="$1" 'BEGIN {f=0;t=0}
    {
      if ($0~"<position-component>") {f=1}
      if (f==1 && $0~"<translation>") {t=1;print}
      if (t==1 && $0~"<x>") {sub(/<x>/,""); sub(/<\/x>/,""); x=$1 + XOFF; print "<x>" x "</x>"}
      if (t==1 && $0~"<y>") {print}
      if (t==1 && $0~"<z>") {sub(/<z>/,""); sub(/<\/z>/,""); z=$1 + ZOFF; print "<z>" z "</z>"}
      if ($0~"</translation>") {t=0;p=1}
      if ($0~"</position-component>") {f=0}
      if (t==0) {print}
    }' > "$i"
}

# copy all children of a given WFS directory
copy_children () {
    wld=`echo $2 | sed -e "s|-wlc.xml|-wld|"`
    if [ -d "$wld" ]; then
       dirname=`echo $wld | sed -e 's|.*/\(.*\)|\1|'`
       cp -R "$wld" "$1/world-wfs/$dirname"

       find "$wld" -name '*-wlc.xml' | while read CHILD; do
           copy_content "$CHILD"
           copy_models "$CHILD"
       done 
    fi
}

# copy all content referred to with wlcontent:// URLs
copy_content () {
    cat "$1" | $grep "wlcontent://" | sed -e 's|.*wlcontent://users@.*\:[0-9]*/\(.*\)<.*|\1|' | sed -e 's|.*wlcontent://users/\(.*\)<.*|\1|' | while read RESULT; do

        if [ "$RESULT" ]; then
            # find directory name
            dir=`echo $RESULT | sed -e 's|\(.*\)/.*|\1|'` 
            mkdir -p "$tmpdir/run/content/users/$dir"
 
            echo "Copying content from $wldir/run/content/users/$RESULT to $tmpdir/run/content/users/$dir"
            cp "$wldir/run/content/users/$RESULT" "$tmpdir/run/content/users/$RESULT"
        fi
    done
}

# copy all files associated with a deployed model
copy_models () {
    cat "$1" | $grep "deployedModelURL>wlcontent://" | cut -d ':' -f 3 | sed -e 's|[0-9]*/\(.*\)/.*</.*>|/\1|' | while read RESULT; do

        if [ "$RESULT" ]; then 
            echo "Copying model from $wldir/run/content/users/$RESULT to $tmpdir/run/content/users"

            mkdir -p "$tmpdir/run/content/users/$RESULT"
            cp -R "$wldir/run/content/users/$RESULT"/* "$tmpdir/run/content/users/$RESULT"
        fi
    done
}

in_range () {
   cat "$1" | awk -v XMIN=$XMIN -v XMAX=$XMAX -v ZMIN=$ZMIN -v ZMAX=$ZMAX -v FILE="$1" 'BEGIN {f=0;t=0}
   {
      if ($0~"<position-component>") {f=1}
      if (f==1 && $0~"<translation>") {t=1}
      if (t==1 && $0~"<x>") {sub(/<x>/,""); sub(/<\/x>/,""); x=$1}
      if (t==1 && $0~"<y>") {sub(/<y>/,""); sub(/<\/y>/,""); y=$1}
      if (t==1 && $0~"<z>") {sub(/<z>/,""); sub(/<\/z>/,""); z=$1}
      if ($0~"</translation>") {t=0}
      if ($0~"</position-component>") {f=0}
   }
   END {if(x >=XMIN && x <= XMAX && z >= ZMIN && z <= ZMAX) { print FILE }}'
}

# first argument: directory to take a snapshot of
origdir=$1
outdir=`pwd`

# find the Wonderland directory this wfs is a child of
wldir=`echo $1 | sed -e 's|\(.*\)/wfs/.*|\1|'`
echo "Wonderland directory $wldir"

# create the temporary directory to copy to
tmpdir=/tmp/wlsave
rm -rf $tmpdir

# create subdirectories we will need
mkdir -p $tmpdir/wfs/snapshots
mkdir -p $tmpdir/run/content/users

# copy WFS files -- this assumes a WFS snapshot
if [ "$2" ]; then
    wfssnapshotdir=$2
else
    wfssnapshotdir=`echo $1 | sed -e 's|.*/\(.*\)|\1|'`
fi

wfsdir=$tmpdir/wfs/snapshots/$wfssnapshotdir
echo "Copying WFS from $1 to $wfsdir"
mkdir -p $wfsdir/world-wfs
copy_wfs "$wfsdir"

# copy all content from the content repository
#copy_content

# special-case copying of files related to deployed models
#copy_models


# determine the output directory and file name
outfile=`echo $1 | sed -e 's|.*/\(.*\)|\1|'`-`date +%m%d%Y`.tgz

# create the output file by tarring the temporary directory
echo "Writing to $outfile"
cd $tmpdir
$tar czf $outdir/$outfile *


