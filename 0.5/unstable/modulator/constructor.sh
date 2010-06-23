echo "Enter constructor"
echo "Modulator path = $1"
echo "New module path = $2"
echo "New module name = $3"

mkdir $2
mkdir -p $2/src/classes/org/jdesktop/wonderland/modules/$3/client
mkdir -p $2/src/classes/org/jdesktop/wonderland/modules/$3/client/resources
mkdir -p $2/src/classes/org/jdesktop/wonderland/modules/$3/common
mkdir -p $2/src/classes/org/jdesktop/wonderland/modules/$3/server
mkdir -p $2/lib
mkdir -p $2/nbproject/private
mkdir -p $2/art

sed s/modulator/$3/ < $1/build.xml > $2/build.xml
cp $1/lib/jmejtree_jme2.jar $2/lib/jmetree_jme2.jar
cp $1/lib/propertytable.jar $2/lib/propertytable.jar
cp $1/lib/trident.jar $2/lib/trident.jar
cp $1/nbproject/nb.properties $2/nbproject/nb.properties
cp $1/nbproject/private/private.xml $2/nbproject/private/private.xml
cp $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/resources/startup.js $2/src/classes/org/jdesktop/wonderland/modules/$3/client/resources/startup.js
cp -r $1/art/modulator $2/art/$3.dae

sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/my.module.properties > $2/my.module.properties
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorSingleNode.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3SingleNode.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorThreeNodes.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3ThreeNodes.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/AnimationProcessorComponent.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/AnimationProcessorComponent.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorCell.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3Cell.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorCellFactory.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3CellFactory.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorQuaternionAnimation.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3QuaternionAnimation.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorQuaternionInterpolator.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3QuaternionInterpolator.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorRenderer.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3Renderer.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorRotationAnimation.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3RotationAnimation.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/ModulatorVectorInterpolator.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/$3VectorInterpolator.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/common/ModulatorCellClientState.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/common/$3CellClientState.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/common/ModulatorCellServerState.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/common/$3CellServerState.java
sed -e s/modulator/$3/g -e s/Modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/server/ModulatorCellMO.java > $2/src/classes/org/jdesktop/wonderland/modules/$3/server/$3CellMO.java
sed s/modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/resources/Bundle.properties > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/resources/Bundle.properties
sed s/modulator/$3/g < $1/src/classes/org/jdesktop/wonderland/modules/modulator/client/resources/module.properties > $2/src/classes/org/jdesktop/wonderland/modules/$3/client/resources/module.properties

