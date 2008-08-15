
// these are global values to maintain the index values,
// and adjust the proper
var zoomLevel = 0;
var zoomLevelIndex = 0;
var zoomLevelSize = 0;

var stepX;
var stepZ;
var offsetX;
var offsetZ;
var maxTileX;
var maxTileY;
var tileWSize;
var tileHSize;
var mapConfig = new Array();
var xmlDoc=null;

// used to control moving the map div
var dragging = false;
var top;
var left;
var dragStartTop;
var dragStartLeft;

function startMove(event) {
  // necessary for IE
  if (!event) event = window.event;

  if( event.button == 2 ) {
    mapClick();
    return false;
  } else {
    dragStartLeft = event.clientX;
    dragStartTop = event.clientY;

    var innerDiv = document.getElementById("innerDiv");

    innerDiv.style.cursor = "move";

    top = stripPx(innerDiv.style.top);
    left = stripPx(innerDiv.style.left);

    dragging = true;

    return false;
  }
}

function processMove(event) {
  if (!event) event = window.event;  // for IE
  var innerDiv = document.getElementById("innerDiv");
  if (dragging) {
    innerDiv.style.top = top + (event.clientY - dragStartTop);
    innerDiv.style.left = left + (event.clientX - dragStartLeft);  
  }
  checkTiles();
}

function stopMove() {
  var innerDiv = document.getElementById("innerDiv");
  innerDiv.style.cursor = "";
  dragging = false;
}

/*
 * Checks to see which tiles are visible, and which need to be added or removed
 */
function checkTiles() {
  // check which tiles should be visible in the inner div
  var visibleTiles = getVisibleTiles();

  // add each tile to the inner div, checking first to see
  // if it has already been added
  var innerDiv = document.getElementById("innerDiv");
  var visibleTilesMap = {};      
  for (i = 0; i < visibleTiles.length; i++) {
    var tileArray = visibleTiles[i];
    
    if(!(tileArray[0] < 0 || tileArray[1] < 0 )) {
      var tileName = "x" + tileArray[0] + "y" + tileArray[1];
      visibleTilesMap[tileName] = true;
      var img = document.getElementById(tileName);
      if (!img) {
        img = document.createElement("img");
        img.src = "resources/tiles/" + zoomLevel + "/" + tileName + ".png";
        img.style.position = "absolute";
        img.style.left = (tileArray[0] * tileWSize) + "px";
        img.style.top = (tileArray[1] * tileHSize) + "px";
        img.style.zIndex = 0;
        img.setAttribute("id", tileName);
        innerDiv.appendChild(img);
      }
    }
  }
}

/*
 * Generates a list of visible tiles
 */
function getVisibleTiles() {
  var tilesX;
  var tilesY;
  var innerDiv = document.getElementById("innerDiv");

  var mapX = stripPx(innerDiv.style.left);
  var mapY = stripPx(innerDiv.style.top);

  if( tileWSize == 0 || tileHSize == 0 ) {
    //check the image size
    var checkImg = new Image();
    checkImg.src = "resources/tiles/" + zoomLevel + "/x0y0.png";

    tileWSize = checkImg.width;
    tileHSize = checkImg.height;
  }

  var startX = Math.abs(Math.floor(mapX / tileWSize)) - 1;
  var startY = Math.abs(Math.floor(mapY / tileHSize)) - 1;

  if( browser.isIE ) {
    tilesX = Math.ceil(document.body.clientHeight / tileWSize) + 1;
    tilesY = Math.ceil(document.body.clientWidth / tileHSize) + 1;
  } else {
    tilesX = Math.ceil(window.innerWidth / tileWSize) + 1;
    tilesY = Math.ceil(window.innerHeight / tileHSize) + 1;
  }

  var visibleTileArray = [];
  var counter = 0;
  for (x = startX; x <= maxTileX && x <= (tilesX + startX);  x++) {
    for (y = startY; y <= maxTileY && y <= (tilesY + startY); y++) {
      if( x >= 0 && y >= 0 ) {
        visibleTileArray[counter++] = [x, y];
      }
    }
  }
  return visibleTileArray;
}

function stripPx(value) {
  if (value == "") return 0;
  return parseFloat(value.substring(0, value.length - 2));
}

function setInnerDivSize(width, height) {
  var innerDiv = document.getElementById("innerDiv");
  innerDiv.style.width = width;
  innerDiv.style.height = height;
}

/*
 *This method is used to go to the more detail of the current map.
 */
function zoomIn() {
  //check the current zoom, and move to a smaller ( zoom in )
  var tmp = zoomLevelIndex;
  
  if( ++tmp > zoomLevelSize - 1 ) {
    alert("Cannot zoom in any further.");
  } else {
    zoomLevelIndex++;
    zoomLevel=mapConfig[zoomLevelIndex]["level"];
    
    var innerDiv = document.getElementById("innerDiv");
    
    saveAvatars();
    
    while(innerDiv.firstChild) {
      innerDiv.removeChild(innerDiv.firstChild);
    }
    
    updateMapSpecs();  
    checkTiles();       
    loadAvatars();
  }
}

/*
 *This method is used to go to the less detail of the current map.
 */
function zoomOut() {
  //check the current zoom, and move to a larger ( zoom out )
  var tmp = zoomLevelIndex;

  if( --tmp < 0 ) {
    alert("Cannot zoom out any further.");
  } else {
    zoomLevelIndex--; 
    zoomLevel=mapConfig[zoomLevelIndex]["level"];

    var innerDiv = document.getElementById("innerDiv");
    
    saveAvatars();
    
    while(innerDiv.firstChild) {
      innerDiv.removeChild(innerDiv.firstChild);
    } 
    
    updateMapSpecs();
    checkTiles();
    loadAvatars();
  }
}

/*
 *Funtion is used to open and read in the information from the xml file.
 */
function initTileMap() {
  var path = "resources/tiles/tiles.xml";

  if( browser.isIE ) {
    xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
    
    if( xmlDoc == null ) {
      xmlDoc=new ActiveXObject("Msxml2.XMLDOM");    
    }
    
    xmlDoc.async="false";
    xmlDoc.onreadystatechange = function () {
                                  if (xmlDoc.readyState == 4) loadConfig()
                                };                            
    xmlDoc.load(path);
  } else if( browser.isMac ) {
    xmlDoc = window.frames['xmlTiles'].document.documentElement;
    loadConfig();
  } else {
    xmlDoc=document.implementation.createDocument("","",null);
    xmlDoc.async="false";
    xmlDoc.onload = loadConfig;
    xmlDoc.load(path);
  }

  checkTiles();
}

/*
 * Changes the map values based on the zoom level, and the number of tiles
 */
function updateMapSpecs() {
  var tmp = mapConfig[zoomLevelIndex];
  //tile sizes set
  maxTileX = tmp["maxTileX"];
  maxTileY = tmp["maxTileY"];
 
  //generate the offset for the map
  offsetX = (tileWSize * maxTileX) / tmp["boundsX"];
  offsetZ = (tileHSize * maxTileY) / tmp["boundsY"];

  //now calculate the step values
  stepX = tileWSize/tmp["unitDiffX"];
  stepZ = tileHSize/tmp["unitDiffY"];
}

/*
 * Load the information from the xml file that was created when the tiles where made
 */
function loadConfig() {
  var levels = xmlDoc.getElementsByTagName("tile");
  
  var origin_x=0;
  var origin_y=0;
  var bounds_x=0;
  var bounds_y=0;
  var max_tile_x=0;
  var max_tile_y=0;
  var unit_diff_x=0;
  var unit_diff_y=0;
  
  zoomLevelSize = levels.length;
  
  for( i = 0; i < levels.length; i++ ) {
    var zoom = levels[i].getAttribute("id");
    var zoom_data = levels[i].childNodes;

    for( j = 0; j < zoom_data.length; j++ ) {
      var tag = zoom_data[j].nodeName;
      
      if( tag == "origin" ) {  //origin
        var origin_nodes = zoom_data[j].childNodes;
        
        for( k = 0; k < origin_nodes.length; k++ ) {
          if( origin_nodes[k].nodeName.toLowerCase() == "x" ) {
            origin_x = origin_nodes[k].firstChild.nodeValue;
          }else if( origin_nodes[k].nodeName.toLowerCase() == "y" ) {
            origin_y = origin_nodes[k].firstChild.nodeValue;
          }
        }
      } else if ( tag == "bounds" ) {  //bounds
        var bounds_nodes = zoom_data[j].childNodes;
        
        for( k = 0; k < bounds_nodes.length; k++ ) {
          if( bounds_nodes[k].nodeName.toLowerCase() == "x" ) {
            bounds_x = bounds_nodes[k].firstChild.nodeValue;
          }else if( bounds_nodes[k].nodeName.toLowerCase() == "y" ) {
            bounds_y = bounds_nodes[k].firstChild.nodeValue;
          }
        }
      } else if ( tag == "maxTile" ) {  //maxTile
        var max_tile_nodes = zoom_data[j].childNodes;
        
        for( k = 0; k < max_tile_nodes.length; k++ ) {
          if( max_tile_nodes[k].nodeName.toLowerCase() == "x" ) {
            max_tile_x = max_tile_nodes[k].firstChild.nodeValue;
          }else if( max_tile_nodes[k].nodeName.toLowerCase() == "y" ) {
            max_tile_y = max_tile_nodes[k].firstChild.nodeValue;
          }
        }        
      } else if ( tag == "unitDiff" ) {  //unitDiff
        var unit_diff_nodes = zoom_data[j].childNodes;
        
        for( k = 0; k < unit_diff_nodes.length; k++ ) {
          if( unit_diff_nodes[k].nodeName.toLowerCase() == "x" ) {
            unit_diff_x = unit_diff_nodes[k].firstChild.nodeValue;
          }else if( unit_diff_nodes[k].nodeName.toLowerCase() == "y" ) {
            unit_diff_y = unit_diff_nodes[k].firstChild.nodeValue;
          }
        } 
      }
    }
    //now i set the value of the array structure
    var tmpData = new Array();
    tmpData["level"] = zoom;
    tmpData["originX"] =  origin_x; 
    tmpData["originY"] =  origin_y;
    tmpData["boundsX"] =  bounds_x; 
    tmpData["boundsY"] =  bounds_y; 
    tmpData["maxTileX"] =  max_tile_x;
    tmpData["maxTileY"] =  max_tile_y;                 
    tmpData["unitDiffX"] =  unit_diff_x;
    tmpData["unitDiffY"] =  unit_diff_y;                

    mapConfig[i] = tmpData;
    
    if( i == 0 ) {
      //set the initial values
      zoomLevelIndex = 0;
      zoomLevel = zoom;

      var checkImg = new Image();
      checkImg.src = "resources/tiles/" + zoomLevel + "/x0y0.png";

      tileWSize = checkImg.width;
      tileHSize = checkImg.height;

      //tile sizes set
      maxTileX = max_tile_x;
      maxTileY = max_tile_y;

      //generate the offset for the map
      offsetX = (tileWSize * maxTileX) / bounds_x;
      offsetZ = (tileHSize * maxTileY) / bounds_y;

      //now calculate the step values
      stepX = tileWSize/unit_diff_x;
      stepZ = tileHSize/unit_diff_y;

      //set to the origin, from the xml file
      positionX = origin_x;
      positionZ = origin_y;

    }
  }
}

/*
 * Stores the avatars, and their current position
 */
function saveAvatars() {
  initPos = false;
  var outerDiv = document.getElementById("outerDiv");
  
  var avatar = document.createElement("div");
  avatar.setAttribute("id", "saved_avatar_positions");
  
  var avatarsDiv = document.getElementById("avatars");
  
  var names = avatarsDiv.childNodes;
  
  for( i=0; i < names.length; i++ ) {
    var nametag = names[i].getAttribute("id");
    var pos = document.getElementById(nametag+"_position");
    var x = pos.firstChild.firstChild.nodeValue;
    var z = pos.firstChild.nextSibling.firstChild.nodeValue;
    
    var avatar_name = document.createElement("div");
    avatar_name.setAttribute("id", nametag + "_position");
    avatar_name.setAttribute("name", nametag);
    avatar_name.setAttribute("x", x);
    avatar_name.setAttribute("z", z);
    
    avatar.appendChild(avatar_name);
  }
  outerDiv.appendChild(avatar);
}

/*
 * Used to place the avatars back into the map. Typically done after a zoom in, or a zoom out.
 */
function loadAvatars() {
  var outerDiv = document.getElementById("outerDiv");
  var avatarsDiv = document.getElementById("saved_avatar_positions");
  var names = avatarsDiv.childNodes;
  
  for( i=0; i < names.length; i++ ) {
    var nametag = names[i].getAttribute("name");
    var x = names[i].getAttribute("x");
    var z = names[i].getAttribute("z");
  
    cbCreateUserLocation(nametag,x,z);
  }
  outerDiv.removeChild(avatarsDiv); 
}

function moveUp() {
  if( stepX == 0 || stepZ == 0) {
    updateMapSpecs();
  }
  positionX = parseInt(positionX);
  positionZ = parseInt(positionZ) - parseInt(stepZ/2);
  
  Servlet.goToLocation(positionX,0,positionZ);
}

function moveDown() {
  if( stepX == 0 || stepZ == 0) {
    updateMapSpecs();
  }
  positionX = parseInt(positionX);
  positionZ = parseInt(positionZ) + parseInt(stepZ/2);
  
  Servlet.goToLocation(positionX,0,positionZ);
}

function moveLeft() {
  if( stepX == 0 || stepZ == 0) {
    updateMapSpecs();
  }
  positionX = parseInt(positionX) - parseInt(stepX/2);
  positionZ = parseInt(positionZ);
  
  Servlet.goToLocation(positionX,0,positionZ);
}

function moveRight() {
  if( stepX == 0 || stepZ == 0) {
    updateMapSpecs();
  }
  positionX = parseInt(positionX) + parseInt(stepX/2);
  positionZ = parseInt(positionZ);
  
  Servlet.goToLocation(positionX,0,positionZ);
}
