<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
		<META HTTP-EQUIV="EXPIRES" CONTENT="Mon, 22 Jul 2002 11:12:01 GMT"/>
		<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, max-age=3, no-store, must-revalidate,proxy-revalidate"/>
		<title>Project Wonderland World Builder</title>
		<link href="main.css" rel="stylesheet" type="text/css" />
		<!--[if IE]><link href="ie.css" rel="stylesheet" type="text/css" /><![endif]-->	
		<script src="javascript/prototype.js" type="text/javascript"></script>
		<script src="javascript/scriptaculous.js" type="text/javascript"></script>
		<script src="javascript/utility.js" type="text/javascript"></script>
		<script type="text/javascript" language="javascript">
		
			//define functions
			
			function buildGrid() {
				var gridHTML = "";
				var gridSquareTop = 0;
				var gridSquareLeft = 0;
				for (i=0;i<gridRows;i++) {
					for (ii=0;ii<gridColumns;ii++) {
						gridHTML += "<div class=\"gridSquare\" style=\"width:" + (gridSize-2) + "px; height:" + (gridSize-2) + "px; top:" + gridSquareTop + "px; left:" + gridSquareLeft + "px\">&nbsp;</div>\n";
						gridSquareLeft += (gridSize-1);
					}
					gridSquareTop += (gridSize-1);
					gridSquareLeft = gridLeft;
				}
				$('grid').setStyle({
					width: gridWidth,
					height: gridHeight
				});
				$('world').setStyle({
					width: gridWidth,
					height: gridHeight
				});
				$('grid').update(gridHTML);
			}
			
			function submitCatalogURL(e) {
				if (e.keyCode == Event.KEY_RETURN) {
					getCatalog($('catalogtextfield').getValue());
				}
			}
			
			function getCatalog(url) {
				new Ajax.Request(url, {
					method:'get',
					onSuccess: function(data) {
						var catalogData = data.responseText.evalJSON();
						var libraries = new Array();
						for (entry=0;entry<catalogData.catalog.length;entry++) {
							for (name=0;name<catalogData.catalog[entry].library.length;name++) {
								libraries.push(catalogData.catalog[entry].library[name]);
								}
							}
						libraries = libraries.uniq();
						libraries = libraries.sort();
						buildLibraries(catalogData,libraries);
					},
					onFailure: function() {
						alert("Failed to load catalog from " + url);
						$('libraryswitch').update("<option>(No catalog loaded)</option>");
					}
				});
			}
			
			function buildLibraries(catalogData,libraries) {
				var updateHTML = "";
				for (library=0;library<libraries.length;library++) {
					updateHTML += "<option value=\"" + libraries[library] + "\">" + libraries[library] + "</option>";
				}
				$('libraryswitch').update(updateHTML);
				currentCatalogData = catalogData;
				getLibrary($('libraryswitch').getValue());
			}
			
			function getLibrary(libraryName) {
				$('library').update('');
				var catalogIndex = 0;
				var top = 10;
				while (currentCatalogData.catalog[catalogIndex]) {
					for (i=0;i<currentCatalogData.catalog[catalogIndex].library.length;i++) {
						if (libraryName == currentCatalogData.catalog[catalogIndex].library[i]) {
							createLibraryObject(currentCatalogData,catalogIndex,top);
							createDisplayPanel(currentCatalogData,catalogIndex);
							top += 110;
						}
					}
					catalogIndex++;
				}
			}
			
			function createLibraryObject(catalogData,catalogIndex,top) {
				var thisObjectID = "obj" + objectID;
				var width = 100;
				var height = 100;
				var right = getScrollerWidth() + 10;
				var insertHTML = "<div style='top:" + top + "px;right:" + right + "px;width:" + width +"px;height:" + height + "px' id='" + thisObjectID + "' onmouseout='hideDisplayPanel(" + catalogIndex + ")' onmouseover='showDisplayPanel(" + catalogIndex + ")'>";
				insertHTML += "<img src='" + catalogData.catalog[catalogIndex].iconImageURL + "' width='" + width + "' height='" + height + "' title='" + catalogData.catalog[catalogIndex].name + ": " + catalogData.catalog[catalogIndex].description + "' />";
				insertHTML += "</div>";
				$('library').insert(insertHTML);
				new Draggable(thisObjectID,{
					revert:function() {
						var workspaceTop = parseInt($('workspace').getStyle('top'));
						var workspaceLeft = parseInt($('workspace').getStyle('left'));
						var thisLocation = $(thisObjectID).viewportOffset();
						var worldLocation = $('world').viewportOffset();
						var workspaceDimensions = $('workspace').getDimensions();
						var scrollbarWidth = getScrollerWidth();
						var top = (gridSize - 1) * Math.floor((thisLocation.top - worldLocation.top + ($(thisObjectID).getHeight()*0.5))/(gridSize - 1));
						var left = (gridSize - 1) * Math.floor((thisLocation.left - worldLocation.left + ($(thisObjectID).getWidth()*0.5))/(gridSize - 1));
						if (thisLocation.left >= workspaceLeft && thisLocation.left <= (workspaceLeft + (workspaceDimensions.width - scrollbarWidth)) && thisLocation.top >= workspaceTop && thisLocation.top <= (workspaceTop + (workspaceDimensions.height - scrollbarWidth))) {
							createPlaceable(catalogIndex,top,left);
						}
						$('library').setStyle({zIndex:'1'});
						$(thisObjectID).remove();
					},
					onStart:function() {
						var thisLocation = $(thisObjectID).positionedOffset();
						var width = (catalogData.catalog[catalogIndex].width*gridSize)-(catalogData.catalog[catalogIndex].width+1);
						var height = (catalogData.catalog[catalogIndex].height*gridSize)-(catalogData.catalog[catalogIndex].height+1);
						if (catalogData.catalog[catalogIndex].group) {
							var newHTML = "";
							for (i=0;i<catalogData.catalog[catalogIndex].members.length;i++) {
								var thisMember = catalogData.catalog[catalogIndex].members[i];
								var thisMemberData = thisMember.split(" ");
								var thisMemberWidth = (catalogData.catalog[thisMemberData[0]].width*gridSize)-(catalogData.catalog[thisMemberData[0]].width+1);
								var thisMemberHeight = (catalogData.catalog[thisMemberData[0]].height*gridSize)-(catalogData.catalog[thisMemberData[0]].height+1);
								newHTML += "<img style=\"border:none;position:absolute;top:" + thisMemberData[1]*(gridSize-1) + "px;left:" + thisMemberData[2]*(gridSize-1) + "px\" src=\"" + catalogData.catalog[thisMemberData[0]].tileImageURL + "\" width=\"" + thisMemberWidth + "\" height=\"" + thisMemberHeight + "\" />";
							}
							$(thisObjectID).setStyle({
								border:'3px solid yellow',
								width: width + 'px',
								height: height + 'px'
							});
							$(thisObjectID).update(newHTML);
						} else {
							var newHTML = "<img style=\"border: 3px solid yellow\" src='" + catalogData.catalog[catalogIndex].tileImageURL + "' width=" + width + " height=" + height + "/>"; 
							$(thisObjectID).update(newHTML);
						}
						createLibraryObject(catalogIndex,(thisLocation.top + 1));
						$('library').setStyle({zIndex:'3'});
					},
					onDrag:function() {
						hideDisplayPanel(catalogIndex);
					}
				});
				Event.observe(thisObjectID, 'mousemove', function(event){
					var thisObjectPanel = "panel_" + catalogIndex;
					var panelTop = (Event.pointerY(event)+10) + "px";
					var panelLeft = (Event.pointerX(event)+10) + "px";
					$(thisObjectPanel).setStyle({top:panelTop,left:panelLeft});
				});
				objectID++;
			}
			
			function createDisplayPanel(catalogData,catalogIndex) {
				//create panel for displaying data about objects upon mouseover
				insertHTML = "<div id='panel_" + catalogIndex + "'>";
				insertHTML += "<img src='" + catalogData.catalog[catalogIndex].displayImageURL + "' />";
				insertHTML += "<h1>" + catalogData.catalog[catalogIndex].name + "</h1>";
				insertHTML += "<p>" + catalogData.catalog[catalogIndex].description + "</p>";
				insertHTML += "</div>";
				$('display').insert(insertHTML);
			}
			
			function showDisplayPanel(catalogIndex) {
				var thisPanel = "panel_" + catalogIndex;
				$(thisPanel).setStyle({visibility:'visible'});
			}
			
			function hideDisplayPanel(catalogIndex) {
				var thisPanel = "panel_" + catalogIndex;
				$(thisPanel).setStyle({visibility:'hidden'});
			}
			
			function createPlaceable(catalogURI,catalogIndex,top,left,direction) {
				new Ajax.Request(catalogURI, {
					method:'get',
					onSuccess: function(data) {
						var catalogData = data.responseText.evalJSON();
						var thisObjectID = "obj" + objectID;
						var width = (catalogData.catalog[catalogIndex].width*gridSize)-(catalogData.catalog[catalogIndex].width+1);
						var height = (catalogData.catalog[catalogIndex].height*gridSize)-(catalogData.catalog[catalogIndex].height+1);
						if (catalogData.catalog[catalogIndex].group) {
							//create group object
							var insertHTML = "<div id=\"" + thisObjectID + "\" class=\"draggable\" style=\"width:" + width + "px;height:" + height + "px;top:" + top + "px;left:" + left + "px;z-index:1\" onmouseover=\"highlightObject('" + thisObjectID + "',true)\" onmouseout=\"highlightObject('" + thisObjectID + "',false)\" onclick=\"showRotationControls('" + thisObjectID + "')\">";
							for (i=0;i<catalogData.catalog[catalogIndex].members.length;i++) {
								var thisMember = catalogData.catalog[catalogIndex].members[i];
								var thisMemberData = thisMember.split(" ");
								var thisMemberWidth = (catalogData.catalog[thisMemberData[0]].width*gridSize)-(catalogData.catalog[thisMemberData[0]].width+1);
								var thisMemberHeight = (catalogData.catalog[thisMemberData[0]].height*gridSize)-(catalogData.catalog[thisMemberData[0]].height+1);
								insertHTML += "<img style=\"position:absolute;top:" + thisMemberData[1]*(gridSize-1) + "px;left:" + thisMemberData[2]*(gridSize-1) + "px\" src=\"" + catalogData.catalog[thisMemberData[0]].tileImageURL + "\" width=\"" + thisMemberWidth + "\" height=\"" + thisMemberHeight + "\" />";
							}
							insertHTML += "<var>" + catalogIndex + "</var>";
							insertHTML += "</div>";
						} else {
							//create non-group object
							var insertHTML = "<div id=\"" + thisObjectID + "\" class=\"draggable\" style=\"top:" + top + "px;left:" + left + "px;z-index:" +catalogData.catalog[catalogIndex].zIndex + "\" onmouseover=\"highlightObject('" + thisObjectID + "',true)\" onmouseout=\"highlightObject('" + thisObjectID + "',false)\" onclick=\"showRotationControls('" + thisObjectID + "')\">";
							insertHTML += "<var>" + catalogIndex + " " + catalogURI + " " + catalogData.catalog[catalogIndex].cellType + " " + catalogData.catalog[catalogIndex].modelURL + " " + (catalogData.catalog[catalogIndex].height*unitFactor) + " " + (catalogData.catalog[catalogIndex].width*unitFactor) + "</var>";
							insertHTML += "<img src=\"" + catalogData.catalog[catalogIndex].tileImageURL + "\" width=\"" + width + "\" height=\"" + height + "\" alt=\"" + catalogData.catalog[catalogIndex].name + "\" title=\"" + catalogData.catalog[catalogIndex].name + ": " + catalogData.catalog[catalogIndex].description + "\" />";
							insertHTML += "</div>";
						}
						$('world').insert(insertHTML);
						new Draggable(thisObjectID,{
							snap:[(gridSize-1),(gridSize-1)],
							revert:function() {
								var workspaceTop = parseInt($('workspace').getStyle('top'));
								var workspaceLeft = parseInt($('workspace').getStyle('left'));
								var thisLocation = ($(thisObjectID).viewportOffset());
								var workspaceLocation = $('workspace').viewportOffset();
								var workspaceDimensions = $('workspace').getDimensions();
								var scrollbarWidth = getScrollerWidth();
								if ((thisLocation.left - 2) <= (workspaceLeft - gridSize) || (thisLocation.left + 2) >= (workspaceLeft + (workspaceDimensions.width - scrollbarWidth)) || (thisLocation.top - 2) <= (workspaceTop - gridSize) || thisLocation.top >= (workspaceTop + (workspaceDimensions.height - scrollbarWidth))) {
									return true; //move object back if dropped outside workspace
								}
								if ($(thisObjectID).getStyle('display') == 'none') {
									$(thisObjectID).remove();
								}
								$('workspace').setStyle({zIndex:'2'});
							},
							onStart:function() {
								$('workspace').setStyle({zIndex:'3'});
								$('rotation_controls').setStyle({visibility:'hidden'});
							},
							onDrag:function() {
								highlightObject(thisObjectID,true);
							}
						});
						objectID++;
						if (direction) {
							setRotation(thisObjectID,direction);
						}
						showRotationControls(thisObjectID);
					},
					onFailure: function() {
						alert("Failed to load catalog from " + url);
					}
				});
			}
			
			function highlightObject(thisObjectID,state) {
				var thisDimensions = $(thisObjectID).getDimensions();
				var thisLocation = $(thisObjectID).positionedOffset();
				if (state) {
					$('highlight_n').setStyle({
						top: (thisLocation.top+2) + 'px',
						left: (thisLocation.left+2) + 'px',
						width: thisDimensions.width + 'px',
						height: '3px',
						visibility: 'visible'
					});
					$('highlight_s').setStyle({
						top: (thisLocation.top+thisDimensions.height-1) + 'px',
						left: (thisLocation.left+2) + 'px',
						width: thisDimensions.width + 'px',
						height: '3px',
						visibility: 'visible'
					});
					$('highlight_e').setStyle({
						top: (thisLocation.top+2) + 'px',
						left: (thisLocation.left+thisDimensions.width-1) + 'px',
						width: '3px',
						height: thisDimensions.height + 'px',
						visibility: 'visible'
					});
					$('highlight_w').setStyle({
						top: (thisLocation.top+2) + 'px',
						left: (thisLocation.left+2) + 'px',
						width: '3px',
						height: thisDimensions.height + 'px',
						visibility: 'visible'
					});
				} else {
					$('highlight_n').setStyle({
						visibility: 'hidden'
					});
					$('highlight_s').setStyle({
						visibility: 'hidden'
					});
					$('highlight_e').setStyle({
						visibility: 'hidden'
					});
					$('highlight_w').setStyle({
						visibility: 'hidden'
					});
				}
			}
			
			function showRotationControls(thisObjectID) {
				/*var thisObjectData = $(thisObjectID).select('var');
				var catalogIndex = thisObjectData[0].innerHTML;
				if (catalogData.catalog[catalogIndex].rotatable){
					var thisLocation = $(thisObjectID).viewportOffset();
					var thisObjectSize = $(thisObjectID).getDimensions();
					var arrowLength = $('arrow_north').getHeight();
					var arrowWidth = $('arrow_north').getWidth();
					var controlboxTop = (thisLocation.top + 2 - arrowLength) + "px";
					var controlboxLeft = (thisLocation.left + 2 - arrowLength) + "px";
					var controlboxWidth = (thisObjectSize.width + (arrowLength * 2)) + "px";
					var controlboxHeight = (thisObjectSize.height + (arrowLength * 2)) + "px";
					var arrowLeft = Math.floor(arrowLength + (thisObjectSize.width * 0.5) - (arrowWidth * 0.5)) + "px";
					var arrowTop = Math.floor(arrowLength + (thisObjectSize.height * 0.5) - (arrowWidth * 0.5)) + "px";
					$('rotation_controls').setStyle({
						width: controlboxWidth,
						height: controlboxHeight,
						top: controlboxTop,
						left: controlboxLeft,
						visibility: 'visible'
					});
					$('arrow_north').setStyle({
						left: arrowLeft
					});
					$('arrow_south').setStyle({
						left: arrowLeft,
						bottom: '0px'
					});
					$('arrow_west').setStyle({
						top: arrowTop
					});
					$('arrow_east').setStyle({
						top: arrowTop,
						right: '0px'
					});
					$('rotation_object').innerHTML = thisObjectID;
				}
			*/}
			
			function setRotation(thisObjectID,direction) {
				var thisImage = $(thisObjectID).firstDescendant(); 
				var newImage = thisImage.readAttribute('src').sub(/\_[nsew]\./,'_' + direction + '.');
				thisImage.writeAttribute('src',newImage);
			}
			
			function getWorld() {
				new Ajax.Request('http://localhost:8080/WonderlandWorldBuilder/resources/tree',{
					requestHeaders: {Accept:'application/json'},
					method: 'get',
					onSuccess: function(data) {
						buildWorld(data);
					},
					onFailure: function() {
						alert("Failed to load world from server");
					}
				});
			}
			
			function buildWorld(data) {
				worldData = data.responseText.evalJSON();
				var cellIndex = 0;
				if (worldData.cell.children.cell.length > -1) {
					while (worldData.cell.children.cell[cellIndex]) {
						var top = (worldData.cell.children.cell[cellIndex].location.y.$/unitFactor)*(gridSize-1);
						var left = (worldData.cell.children.cell[cellIndex].location.x.$/unitFactor)*(gridSize-1);
						var direction = 0;
						if (worldData.cell.children.cell[cellIndex].rotation) {
							direction = worldData.cell.children.cell[cellIndex].rotation;
							switch (direction) {
								case 180:
									direction = "n";
									break;
								case 90:
									direction = "e";
									break;
								case 360:
									direction = "s";
									break;
								case 270:
									direction = "w";
									break;
								default:
									direction = 0;
							}
						}
						var catalogURI = worldData.cell.children.cell[cellIndex].catalogURI.$;
						var catalogIndex = worldData.cell.children.cell[cellIndex].catalogID.$;
						createPlaceable(catalogURI,catalogIndex,top,left,direction);
						cellIndex++;
					}
				} else {
					var top = (worldData.cell.children.cell.location.y.$/unitFactor)*(gridSize-1);
					var left = (worldData.cell.children.cell.location.x.$/unitFactor)*(gridSize-1);
					var direction = 0;
					if (worldData.cell.children.cell.rotation) {
						direction = worldData.cell.children.cell.rotation;
						switch (direction) {
							case 180:
								direction = "n";
								break;
							case 90:
								direction = "e";
								break;
							case 360:
								direction = "s";
								break;
							case 270:
								direction = "w";
								break;
							default:
								direction = 0;
						}
					}
					var catalogURI = worldData.cell.children.cell.catalogURI.$;
					var catalogIndex = worldData.cell.children.cell.catalogID.$;
					createPlaceable(catalogURI,catalogIndex,top,left,direction);
				}
				$('rotation_controls').setStyle({visibility:'hidden'});
			}
			
			function createJSON() {
				var outputJSON = "{\"cell\":{\"@uri\":\"http://localhost:8080/WonderlandWorldBuilder/resources/tree/root\",\"cellID\":{\"$\":\"root\"},\"cellType\":{\"$\":\"org.jdesktop.lg3d.wonderland.darkstar.server.cell.SimpleTerrainCellGLO\"},\"children\":{\"cell\":";
				var objectArray = $('world').childElements();
				if (objectArray.length > 1) {
					outputJSON += "["
				}
				var objectArrayIndex = 0;
				var cellID = 1;
				while (objectArray[objectArrayIndex]) {
					var thisData = $w(objectArray[objectArrayIndex].firstDescendant().innerHTML);
					var thisLocation = objectArray[objectArrayIndex].positionedOffset();
					var thisX = ((thisLocation[0]/(gridSize-1)))*unitFactor;
					var thisY = ((thisLocation[1]/(gridSize-1)))*unitFactor;
					outputJSON += "{\"@xmlns\":{\"xsi\":\"http:\/\/www.w3.org\/2001\/XMLSchema-instance\"},\"@xsi:type\":\"treeCellWrapper\",";
					outputJSON += "\"@uri\":\"http:\/\/localhost:8080\/WonderlandWorldBuilder\/resources\/tree\/" + cellID + "\",";
					outputJSON += "\"catalogID\":{\"$\":\"" + thisData[0] + "\"},";
					outputJSON += "\"catalogURI\":{\"$\":\"" + thisData[1] + "\"},";
					outputJSON += "\"cellID\":{\"$\":\"" + cellID + "\"},";
					outputJSON += "\"cellType\":{\"$\":\"" + thisData[2] + "\"},";
					outputJSON += "\"children\":{},\"location\":{";
					outputJSON += "\"x\":{\"$\":\"" + thisX + "\"},";
					outputJSON += "\"y\":{\"$\":\"" + thisY + "\"}";
					outputJSON += "},\"properties\":{\"property\":{\"key\":{\"$\":\"model\"},";
					outputJSON += "\"value\":{\"$\":\"" + thisData[3] + "\"}";
					outputJSON += "}},\"size\":{";
					outputJSON += "\"height\":{\"$\":\"" + thisData[4] + "\"},";
					outputJSON += "\"width\":{\"$\":\"" + thisData[5] + "\"}";
					outputJSON += "},\"version\":{\"$\":\"0\"}}},\"location\":{\"x\":{\"$\":\"0\"},\"y\":{\"$\":\"0\"}},\"properties\":{},\"size\":{\"height\":{\"$\":\"1024\"},\"width\":{\"$\":\"1024\"}},\"version\":{\"$\":\"0\"}}}";
					/*var rotation = objectArray[objectArrayIndex].firstDescendant().readAttribute('src');
					if (rotation = rotation.charAt(rotation.search(/_[nsew]./) + 1)) {
						switch (rotation) {
							case "n":
								rotation = 180;
								break;
							case "e":
								rotation = 90;
								break;
							case "s":
								rotation = 360;
								break;
							case "w":
								rotation = 270;
								break;
							default:
								rotation = 0;
						}
						var radians = rotation * (Math.PI/180);
						outputJSON += "\"rotation\":" + rotation + ",";
						outputJSON += "\"radians\":" + radians;
					}
					outputJSON += "},";*/
					objectArrayIndex++;
					cellID++;
				}
				if (objectArray.length > 1) {
					outputJSON += "]"
				}
				outputJSON += "}";
				outputJSON = outputJSON.replace(/\//g,"\\/");
				return outputJSON;
			}
			
			function putWorld() {
				new Ajax.Request("http://localhost:8080/WonderlandWorldBuilder/resources/tree", {
					method:'post',
					postBody: createJSON(),
                                        contentType:'application/json',
					onSuccess: function() {
						alert("World saved");
					},
					onFailure: function() {
						alert("Failed to save");
					}
				});
			}
			
			function createTrash() {
				var workspacePosition = $('workspace').viewportOffset();
				var workspaceDimensions = $('workspace').getDimensions();
				var top = (workspacePosition.top + workspaceDimensions.height) - (51 + getScrollerWidth());
				var left = workspacePosition.left + 1;
				document.write("<div id=\"trash\" style=\"top:" + top + "px;left:" + left + "px\"><img src=\"emblem-trash.png\" title=\"Drop object here to remove from world\" /></div>");
				Droppables.add('trash', {
					accept: 'draggable',
					hoverclass: 'highlight',
					onDrop: function(element) {
						element.setStyle({display:'none'});//set the element to not display, then remove it later instead of removing it here (to avoid conflicts between the "revert" event of the object and the "onDrop" event of the trash)
						$('highlight_n').setStyle({visibility: 'hidden'});
						$('highlight_e').setStyle({visibility: 'hidden'});
						$('highlight_s').setStyle({visibility: 'hidden'});
						$('highlight_w').setStyle({visibility: 'hidden'});
					}
				});
			}
			
			function zoomIn() {
				var data = createJSON();
				$('world').update('');
				gridSize *= 2;
				buildGrid();
				buildWorld(data);
			}
			
			function zoomOut() {
				var data = createJSON();
				$('world').update('');
				gridSize *= 0.5;
				buildGrid();
				buildWorld(data);
			}
			
			//declare globals
			
			var currentCatalogData = new Object();
			var worldData = new Object();
			var objectID = 0;
			var gridHTML = new String();
			
			//configuration variables
			
			var unitFactor = 2;//how much to multiply the grid coordinates by to convert to wonderland units
			//a note on unitFactor: conveniently it looks like this is equivalent to Blender units
			var gridSize = 64;
			var gridTop = 0;
			var gridLeft = 0;
			var gridRows = 30;
			var gridColumns = 40;
			var gridWidth = ((gridSize*gridColumns)-(gridColumns-1)) + "px";
			var gridHeight = ((gridSize*gridRows)-(gridRows-1)) + "px";
			var gridSquareTop = gridTop;
			var gridSquareLeft = gridLeft;
		</script>
	    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	</head>
	<body>
		<div id="main">
			<div id="library"></div>
			<div id="display"></div>
			<div id="workspace" title="Click and drag to pan workspace">
				<div id="grid"></div>
				<div id="world"></div>
				<div id="highlight_n"></div>
				<div id="highlight_s"></div>
				<div id="highlight_e"></div>
				<div id="highlight_w"></div>
			</div>
			<div id="rotation_controls" title="Click arrows to indicate object facing direction" onmouseout="$('rotation_controls').setStyle({visibility:'hidden'})" onmouseover="$('rotation_controls').setStyle({visibility:'visible'})">
				<var id="rotation_data"></var>
				<var id="rotation_object"></var>
				<img id="arrow_north" src="arrow_north.png" title="Click to face object north" onmouseover="setRotation($('rotation_object').innerHTML,'n')" onclick="$('rotation_controls').setStyle({visibility:'hidden'})" />
				<img id="arrow_south" src="arrow_south.png" title="Click to face object south" onmouseover="setRotation($('rotation_object').innerHTML,'s')" onclick="$('rotation_controls').setStyle({visibility:'hidden'})" />
				<img id="arrow_east" src="arrow_east.png" title="Click to face object east" onmouseover="setRotation($('rotation_object').innerHTML,'e')" onclick="$('rotation_controls').setStyle({visibility:'hidden'})" />
				<img id="arrow_west" src="arrow_west.png" title="Click to face object west" onmouseover="setRotation($('rotation_object').innerHTML,'w')" onclick="$('rotation_controls').setStyle({visibility:'hidden'})" />
			</div>
			<!--<div id="debug" style="position: absolute; top: 0px; right: 0px; width: 200px; height: 200px; overflow: scroll; background-color: white; z-index: 9999"></div>-->
			<div class="button" onclick="zoomIn()">Zoom In</div>
			<div class="button" onclick="zoomOut()">Zoom Out</div>
			<div class="button" onclick="putWorld()">Save World</div>
			<div id="selector">
				Load Catalog:<input type="text" value="http://localhost:8080/WonderlandWorldBuilder/catalog.json" id="catalogtextfield" onclick="this.value = 'http://localhost:8080/WonderlandWorldBuilder/catalog.json'" />
				Select Library:<select id="libraryswitch" onchange="getLibrary($('libraryswitch').getValue())"><option>(No catalog loaded)</option></select>
			</div>
			<script type="text/javascript" language="javascript">
				//initialize
				new DragScrollable('workspace');
				buildGrid();
				getWorld();
				createTrash();
				$('catalogtextfield').observe('keypress',submitCatalogURL);
			</script>
		</div>
	</body>
</html>
