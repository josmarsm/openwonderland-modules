echo "Enter snapease.sh"
echo "snappy path = $1"
echo "New module path = $2"
echo "New module name = $3"
echo "Snapshot directory name = $4"

mkdir $2

mkdir -p $2/build
mkdir -p $2/build/classes
mkdir -p $2/build/lib
mkdir -p $2/nbproject
mkdir -p $2/art
mkdir -p $2/wfs
mkdir -p $2/dist

mkdir -p $2/temp

cd $2/temp; $1/save_region.sh $4; tar xvfz *tgz

sed -e s/snappy/$3/g < $1/build.xml > $2/build.xml
sed -e s/snappy/$3/g -e s/Snappy/$3/g < $1/nbproject/project.xml > $2/nbproject/project.xml
cp $1/nbproject/nb.properties $2/nbproject/nb.properties

sed -e s/snappy/$3/g -e s/Snappy/$3/g -e s/snappy-description/$3-description/g < $1/my.module.properties > $2/my.module.properties
sed -e s/snappy/$3/g -e s/snappy-description/$3-description/g < $1/build/moduleInfo.xml > $2/build/moduleInfo.xml
cp $1/build/moduleRequires.xml $2/build/moduleRequires.xml

apath=`cd $2/temp; find ./ -name art -print`
echo "art path = $apath" 
for i in `ls $apath` 
do
  ls -l $apath/$i
  mkdir $2/art/$i
  depfile=`cd $apath/$i; find ./ -name '*dep' -print`
  ldrfile=`cd $apath/$i; find ./ -name '*ldr' -print`
  sed -e "s@<modelURL>wlcontent:.*art@<modelURL>wla://$3@g" -e "s@<loaderDataURL>wlcontent:.*art@<loaderDataURL>wla://$3@g" < $2/temp/$apath/$i/$depfile > $2/art/$i/$depfile
  cp $apath/$i/$ldrfile $2/art/$i/$ldrfile
  if [ -f $apath/$i/$i.gz ]; then
    cp $apath/$i/$i.gz $2/art/$i/$i.gz
  fi
  if [ -d $apath/$i/models ]; then
    mkdir $2/art/$i/models
    cp -r $apath/$i/models $2/art/$i
  fi
done

wfspath=`cd $2/temp; find ./ -name world-wfs -print`
echo "wfs path = $wfspath"
for i in `cd $wfspath; find ./ -name '*-wlc.xml' -print` 
do
  thepath=`dirname $i`
  mkdir -p $2/wfs/$3\-wfs/$thepath
  sed -e "s@<deployedModelURL>wlcontent:.*art@<deployedModelURL>wla://$3@g" < $2/temp/$wfspath/$i > $2/wfs/$3\-wfs/$i
done 
