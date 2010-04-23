WONDERLAND_DIR=/Users/bh37721/src/wonderland
VOIP_MODULES_DIR=/Users/bh37721/jvoicebridge/voip/modules

echo copying bridge-recorder.jar to $VOIP_MODULES_DIR
cp dist/bridge-recorder.jar $VOIP_MODULES_DIR

echo copying scannotation-1.0.2.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/common/scannotation-1.0.2.jar $VOIP_MODULES_DIR

echo copying sgs-client.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/sgs/sgs-client-0.9.10/lib/sgs-client.jar $VOIP_MODULES_DIR

echo copying commons-codec-1.3.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/common/commons-codec-1.3.jar $VOIP_MODULES_DIR

echo copying sgs-shared-1.7.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/sgs/sgs-client-0.9.10/lib/sgs-shared-1.7.jar $VOIP_MODULES_DIR

echo copying commons-httpclient-3.0.1.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/common/commons-httpclient-3.0.1.jar $VOIP_MODULES_DIR

echo copying slf4j-api-1.4.0.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/sgs/sgs-client-0.9.10/lib/slf4j-api-1.4.0.jar $VOIP_MODULES_DIR

echo copying commons-logging-1.1.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/common/commons-logging-1.1.jar $VOIP_MODULES_DIR

echo copying slf4j-jdk14-1.4.0.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/sgs/sgs-client-0.9.10/lib/slf4j-jdk14-1.4.0.jar $VOIP_MODULES_DIR

echo copying contentrepo-client.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/modules/foundation/content-repository/build/client/contentrepo-client.jar $VOIP_MODULES_DIR

echo copying webdav-client.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/modules/tools/webdav/build/client/webdav-client.jar $VOIP_MODULES_DIR

echo copying jdom-1.0.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/modules/tools/webdav/lib/jdom-1.0.jar $VOIP_MODULES_DIR

echo copying webdavclient4j-core-0.92.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/modules/tools/webdav/lib/webdavclient4j-core-0.92.jar $VOIP_MODULES_DIR

echo copying wonderland-client.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/build/lib/wonderland-client.jar $VOIP_MODULES_DIR

echo copying mina-core-1.1.0.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/ext/sgs/sgs-client-0.9.10/lib/mina-core-1.1.0.jar $VOIP_MODULES_DIR

echo copying wonderland-common.jar to $VOIP_MODULES_DIR
cp $WONDERLAND_DIR/core/build/lib/wonderland-common.jar $VOIP_MODULES_DIR

