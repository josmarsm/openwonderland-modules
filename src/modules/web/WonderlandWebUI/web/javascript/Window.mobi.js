
// these are global values to maintain the index values,
// and adjust the proper
var zoomLevel = 0;
var zoomLevelIndex = 0;
var zoomLevelSize = 0;

var stepX;
var stepY;
var offsetX;
var offsetY;
var maxTileX;
var maxTileY;
var tileWSize;
var tileHSize;
var mapConfig = new Array();
var xmlDoc;

// START:checktiles
function checkTiles() {
  // check which tiles should be visible in the inner div
  var visibleTiles = getVisibleTiles();

  // add each tile to the inner div, checking first to see
  // if it has already been added
  var innerDiv = document.getElementById("innerDiv");
  var visibleTilesMap = {};      
  for (i = 0; i < visibleTiles.length; i++) {
    var tileArray = visibleTiles[i];
    
    if(!(tileArray[0] < 0 || tileArray[1] < 0)) {
      // START:imgZoomLevel
      var tileName = "x" + tileArray[0] + "y" + tileArray[1];
      // END:imgZoomLevel
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
// END:checktiles

function getVisibleTiles() {
  var tilesX;
  var tilesY;

  var innerDiv = document.getElementById("innerDiv");

  var mapX = stripPx(innerDiv.style.left);
  var mapY = stripPx(innerDiv.style.top);
//  window.document.write("mapX: " + mapX + "<br></br>");
//  window.document.write("mapY: " + mapY + "<br></br>");

  if( tileWSize == 0 || tileHSize == 0 ) {
    //check the image size
    var checkImg = new Image();
    checkImg.src = "resources/tiles/" + zoomLevel + "/x0y0.png";

    tileWSize = checkImg.width;
    tileHSize = checkImg.height;
  }

  var startX = Math.abs(Math.floor(mapX / tileWSize)) - 1;
  var startY = Math.abs(Math.floor(mapY / tileHSize)) - 1;
//  window.document.write("startX: " + startX + "<br></br>");
//  window.document.write("startY: " + startY + "<br></br>");

  
  if( browser.isIE ) {
   tilesX = Math.ceil(document.body.clientHeight / tileWSize) + 1;
   tilesY = Math.ceil(document.body.clientWidth / tileHSize) + 1;
  } else {
    tilesX = Math.ceil(window.innerWidth / tileWSize) + 1;
    tilesY = Math.ceil(window.innerHeight / tileHSize) + 1;
  }
  
//  window.document.write("tilesX: " + tilesX + "<br></br>");
//  window.document.write("tilesY: " + tilesY + "<br></br>");

  var visibleTileArray = [];
  var counter = 0;
  for (x = startX; x <= maxTileX && x <= (tilesX + startX); x++) {
    for (y = startY; y <= maxTileY && y <= (tilesY + startY); y++) {
      if( x >= 0 && y >= 0 ) {
//      window.document.write("visibleTileArray["+ counter +"] = " + "[" + x + "," + y + "] <br></br>");
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
    
    loadAvatars();
    updateMapSpecs();  
    checkTiles();    
  }
}

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
    loadAvatars();
    updateMapSpecs();
    checkTiles();
  }
}

function initTileMap() {
  var path = "resources/tiles/tiles.xml";

  if( browser.isIECompatible) {
    xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
    xmlDoc.async="false";
    xmlDoc.onreadystatechange = function () {
                                  if (xmlDoc.readyState == 4) loadConfig()
                                };
  } else {
    xmlDoc=document.implementation.createDocument("","",null);
    xmlDoc.async="false";
    xmlDoc.onload = loadConfig;
  }
  xmlDoc.load(path);
  checkTiles();
}

function initialPosition() {
  var innerDiv = document.getElementById("innerDiv");
  var viewOffsetX; //value to adjust left value of view window
  var viewOffsetY; //value to adjust top value of view window
  var viewWidth; //value in px
  var viewHeight; //value in px
  var tmp = mapConfig[0];
  var posX = tmp["originX"];
  var posY = tmp["originY"];

  //tile sizes set
  maxTileX = tmp["maxTileX"];
  maxTileY = tmp["maxTileY"];
 
  //generate the offset for the map
  offsetX = (tileWSize * maxTileX) / tmp["boundsX"];
  offsetY = (tileHSize * maxTileY) / tmp["boundsY"];

  //now calculate the step values
  stepX = tileWSize/tmp["unitDiffX"];
  stepY = tileHSize/tmp["unitDiffY"];
  
  //centers the avatar based on the size of the window
  if( browser.isIE ) {
    viewWidth = (document.body.clientWidth / 2);
    viewHeight = (document.body.clientHeight / 2);
  } else {
    viewWidth = (window.innerWidth / 2);
    viewHeight = (window.innerHeight / 2);
  }
  
  //adjust the position based on the mapping conversion
  //this will apply to the position of the upper left hand corner
  //so i will need to subtract 1/2 width, and 1/2 height to get 
  //the correct positioning
  viewOffsetX = -1*(parseInt(getVworldToWebX(posX)) - (parseInt(viewWidth) - parseInt(30)));
  viewOffsetY = -1*(parseInt(getVworldToWebY(posY)) - (parseInt(viewHeight) - parseInt(30)));
  
  //adjust the view window
  innerDiv.style.left = viewOffsetX + "px";
  innerDiv.style.top = viewOffsetY + "px";  

  checkTiles();
}

function updateMapSpecs() {
  var tmp = mapConfig[zoomLevelIndex];
  //tile sizes set
  maxTileX = tmp["maxTileX"];
  maxTileY = tmp["maxTileY"];
 
  //generate the offset for the map
  offsetX = (tileWSize * maxTileX) / tmp["boundsX"];
  offsetY = (tileHSize * maxTileY) / tmp["boundsY"];

  //now calculate the step values
  stepX = tileWSize/tmp["unitDiffX"];
  stepY = tileHSize/tmp["unitDiffY"];
}

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
      offsetY = (tileHSize * maxTileY) / bounds_y;

      //now calculate the step values
      stepX = tileWSize/unit_diff_x;
      stepY = tileHSize/unit_diff_y;

      //set to the origin, from the xml file
      positionX = origin_x;
      positionY = origin_y;
    }
  }
}

function saveAvatars() {
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
//  if( stepX == 0 || stepY == 0) {
//    updateMapSpecs();
//  }
  positionX = parseInt(positionX);
  positionY = parseInt(positionY) - parseInt(stepY/2);
  
  Servlet.goToLocation(positionX,0,positionY);
}

function moveDown() {
//  if( stepX == 0 || stepY == 0) {
//    updateMapSpecs();
//  }
  positionX = parseInt(positionX);
  positionY = parseInt(positionY) + parseInt(stepY/2);
  
  Servlet.goToLocation(positionX,0, positionY);
}

function moveLeft() {
//  if( stepX == 0 || stepY == 0) {
//    updateMapSpecs();
//  }
  positionX = parseInt(positionX) - parseInt(stepX/2);
  positionY = parseInt(positionY);
  
  Servlet.goToLocation(positionX,0,positionY);
}

function moveRight() {
//  if( stepX == 0 || stepY == 0) {
//    updateMapSpecs();
//  }
  positionX = parseInt(positionX) + parseInt(stepX/2);
  positionY = parseInt(positionY);
  
  Servlet.goToLocation(positionX,0,positionY);
}
