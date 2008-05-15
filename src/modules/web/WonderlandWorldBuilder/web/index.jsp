<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" onmousedown="deselectAll()">
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
							catalogData.catalog[entry].catalogURI = url;
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
				var thisObjectID = "lib" + libraryObjectID;
				libraryObjectID++;
				var width = 100;
				var height = 100;
				var right = getScrollerWidth() + 10;
				var insertHTML = "<div style='top:" + top + "px;right:" + right + "px;width:" + width +"px;height:" + height + "px' id='" + thisObjectID + "' onmouseout='hideDisplayPanel(" + catalogIndex + ")' onmouseover='showDisplayPanel(" + catalogIndex + ")' onmousedown='deselectAll()'>";
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
						top -= ((catalogData.catalog[catalogIndex].height-1)*gridSize);
						top += (catalogData.catalog[catalogIndex].height-1);
						left -= ((catalogData.catalog[catalogIndex].width-1)*gridSize);
						left += (catalogData.catalog[catalogIndex].width-1);
						if (catalogData.catalog[catalogIndex].rotatable) {
							var direction = "n";
						} else {
							var direction = null;
						}
						if (thisLocation.left >= workspaceLeft && thisLocation.left <= (workspaceLeft + (workspaceDimensions.width - scrollbarWidth)) && thisLocation.top >= workspaceTop && thisLocation.top <= (workspaceTop + (workspaceDimensions.height - scrollbarWidth))) {
							createPlaceable(catalogData.catalog[catalogIndex].catalogURI,catalogIndex,top,left,direction,null,true);
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
						createLibraryObject(currentCatalogData,catalogIndex,(thisLocation.top + 1));
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
			
			function createPlaceable(catalogURI,catalogIndex,top,left,direction,cellID,newComponent) {
				new Ajax.Request(catalogURI, {
					method:'get',
					onSuccess: function(data) {
						var catalogData = data.responseText.evalJSON();
						if (cellID) {
							var thisObjectID = cellID;
							if (parseInt(cellID.substring(3)) > objectID) {
								objectID = parseInt(cellID.substring(3));
							}
						} else {
							var thisObjectID = "obj" + objectID;
						}
						objectID++;
						var width = (catalogData.catalog[catalogIndex].width*gridSize)-(catalogData.catalog[catalogIndex].width+1);
						var height = (catalogData.catalog[catalogIndex].height*gridSize)-(catalogData.catalog[catalogIndex].height+1);
						if (catalogData.catalog[catalogIndex].group) {
							//create group object
							var insertHTML = "<div id=\"" + thisObjectID + "\" class=\"draggable\" style=\"width:" + width + "px;height:" + height + "px;top:" + top + "px;left:" + left + "px;z-index:1\" onmouseover=\"highlightObject('" + thisObjectID + "',true)\" onmouseout=\"highlightObject('" + thisObjectID + "',false)\" onmousedown=\"selectObject('" + thisObjectID + "')\" onclick=\"showRotationControls('" + thisObjectID + "')\">";
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
							var insertHTML = "<div id=\"" + thisObjectID + "\" class=\"draggable\" style=\"top:" + top + "px;left:" + left + "px;z-index:" +catalogData.catalog[catalogIndex].zIndex + "\" onmouseover=\"highlightObject('" + thisObjectID + "',true)\" onmouseout=\"highlightObject('" + thisObjectID + "',false)\" onmousedown=\"selectObject('" + thisObjectID + "')\" onclick=\"showRotationControls('" + thisObjectID + "')\">";
							insertHTML += "<var>" + catalogIndex + " " + catalogURI + " " + catalogData.catalog[catalogIndex].cellSetupType + " " + catalogData.catalog[catalogIndex].cellType + " " + catalogData.catalog[catalogIndex].modelURL + " " + catalogData.catalog[catalogIndex].height + " " + catalogData.catalog[catalogIndex].width + " " + catalogData.catalog[catalogIndex].rotatable + "</var>";
							insertHTML += "<img src=\"" + catalogData.catalog[catalogIndex].tileImageURL + "\" width=\"" + width + "\" height=\"" + height + "\" alt=\"" + catalogData.catalog[catalogIndex].name + "\" title=\"" + catalogData.catalog[catalogIndex].name + ": " + catalogData.catalog[catalogIndex].description + "\" />";
							insertHTML += "</div>";
						}
						$('world').insert(insertHTML);
						//update worldData with new component data
						if (newComponent) {
							if (worldData.cell.children.cell) {
								var newCellIndex = worldData.cell.children.cell.length;
							} else {
								var newCellIndex = 0;
								worldData.cell.children.cell = new Array();
							}
							var thisData = $w($(thisObjectID).firstDescendant().innerHTML);
							var thisLocation = $(thisObjectID).positionedOffset();
							var thisX = ((thisLocation[0]/(gridSize-1))+(thisData[6]*0.5))*unitFactor;
							var thisY = ((thisLocation[1]/(gridSize-1))+(thisData[5]*0.5))*unitFactor;
							worldData.cell.children.cell[newCellIndex] = {
								catalogURI: {$:""},
								catalogID: {$:""},
								location: {x:{$:""},y:{$:""}},
								rotation: {$:""},
								size: {height:{$:""},width:{$:""}},
								cellID: {$:""},
								cellSetupType: {$:""},
								cellType: {$:""},
								properties: {property:{key:{$:""},value:{$:""}}}
							};
							worldData.cell.children.cell[newCellIndex].catalogURI.$ = catalogURI;
							worldData.cell.children.cell[newCellIndex].catalogID.$ = catalogIndex + "";
							worldData.cell.children.cell[newCellIndex].location.x.$ = thisX + "";
							worldData.cell.children.cell[newCellIndex].location.y.$ = thisY + "";
							worldData.cell.children.cell[newCellIndex].size.height.$ = (thisData[5]*unitFactor) + "";
							worldData.cell.children.cell[newCellIndex].size.width.$ = (thisData[6]*unitFactor) + "";
							worldData.cell.children.cell[newCellIndex].cellID.$ = thisObjectID;
							worldData.cell.children.cell[newCellIndex].cellSetupType.$ = thisData[2];
							worldData.cell.children.cell[newCellIndex].cellType.$ = thisData[3];
							worldData.cell.children.cell[newCellIndex].properties.property.key.$ = "modelFile";
							worldData.cell.children.cell[newCellIndex].properties.property.value.$ = thisData[4];
						}
						new Draggable(thisObjectID,{
							snap:[((gridSize*0.5)-0.5),((gridSize*0.5)-0.5)],
							revert:function() {
								var workspaceTop = parseInt($('workspace').getStyle('top'));
								var workspaceLeft = parseInt($('workspace').getStyle('left'));
								var thisLocation = ($(thisObjectID).viewportOffset());
								var workspaceLocation = $('workspace').viewportOffset();
								var workspaceDimensions = $('workspace').getDimensions();
								var scrollbarWidth = getScrollerWidth();
								var thisCellIndex = getCellIndex(thisObjectID);
								if ($(thisObjectID).getStyle('visibility') == 'hidden') {
									worldData.cell.children.cell[thisCellIndex] = null;
									worldData.cell.children.cell = worldData.cell.children.cell.compact();
									selectedObject = null;
									$(thisObjectID).remove();
								} else {
									if ((thisLocation.left - 2) <= (workspaceLeft - gridSize) || (thisLocation.left + 2) >= (workspaceLeft + (workspaceDimensions.width - scrollbarWidth)) || (thisLocation.top - 2) <= (workspaceTop - gridSize) || thisLocation.top >= (workspaceTop + (workspaceDimensions.height - scrollbarWidth))) {
										return true; //move object back if dropped outside workspace
									} else {
										//update worldData with new component location
										var thisData = $w($(thisObjectID).firstDescendant().innerHTML);
										var thisLocation = $(thisObjectID).positionedOffset();
										var thisX = ((thisLocation[0]/(gridSize-1))+(thisData[6]*0.5))*unitFactor;
										var thisY = ((thisLocation[1]/(gridSize-1))+(thisData[5]*0.5))*unitFactor;
										worldData.cell.children.cell[thisCellIndex].location.x.$ = thisX + "";
										worldData.cell.children.cell[thisCellIndex].location.y.$ = thisY + "";
									}
									showRotationControls(thisObjectID);
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
						if (direction) {
							setRotation(thisObjectID,direction);
						}
						if (newComponent) {
							showRotationControls(thisObjectID);
						}
					},
					onFailure: function() {
						alert("Failed to load catalog data for this object from " + url);
					}
				});
			}
			
			function getCellIndex(thisObjectID) {
				var cellIndex = 0;
				while (worldData.cell.children.cell[cellIndex]) {
					if (worldData.cell.children.cell[cellIndex].cellID.$ == thisObjectID) {
						return cellIndex;
					}
					cellIndex++;
				}
				return false;
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
				selectedObjectMousePointer = true; //mouse pointer is over this object
				var thisData = $w($(thisObjectID).firstDescendant().innerHTML);
				var rotatable = thisData[7];
				if (rotatable == "true") {
					var thisLocation = $(thisObjectID).positionedOffset();
					var thisObjectSize = $(thisObjectID).getDimensions();
					var arrowLength = $('arrow_north').getHeight();
					var arrowWidth = $('arrow_north').getWidth();
					var controlboxTop = (thisLocation.top + 2 - arrowLength) + "px";
					var controlboxLeft = (thisLocation.left + 2 - arrowLength) + "px";
					var controlboxWidth = (thisObjectSize.width + (arrowLength * 2)) + "px";
					var controlboxHeight = (thisObjectSize.height + (arrowLength * 2)) + "px";
					var arrowLeft = Math.floor(arrowLength + (thisObjectSize.width * 0.5) - (arrowWidth * 0.5)) + "px";
					var arrowTop = Math.floor(arrowLength + (thisObjectSize.height * 0.5) - (arrowWidth * 0.5)) + "px";
					selectedObject = thisObjectID;
					selectedObjectZ = $(thisObjectID).getStyle('zIndex');
					$(thisObjectID).setStyle({zIndex:9999});
					$('rotation_controls').setStyle({
						width: controlboxWidth,
						height: controlboxHeight,
						top: controlboxTop,
						left: controlboxLeft,
						visibility: 'visible',
						zIndex: 999
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
			}
			
			function setRotation(thisObjectID,direction) {
				var thisImage = $(thisObjectID).firstDescendant().next();
				var newImage = thisImage.readAttribute('src').sub(/\_[nsew]\./,'_' + direction + '.');
				thisImage.writeAttribute('src',newImage);
				switch (direction) {
					case "n":
						var rotation = "180";
						break;
					case "e":
						var rotation = "90";
						break;
					case "s":
						var rotation = "360";
						break;
					case "w":
						var rotation = "270";
						break;
					default:
						var rotation = "0";
				}
				worldData.cell.children.cell[getCellIndex(thisObjectID)].rotation.$ = rotation;
			}
			
			function getWorld() {
				new Ajax.Request('resources/tree',{
					requestHeaders: {Accept:'application/json'},
					method: 'get',
					onSuccess: function(data) {
						worldData = data.responseText.evalJSON();
						buildWorld();
					},
					onFailure: function() {
						alert("Failed to load world from server");
					}
				});
			}
			
			function buildWorld() {
				var cellIndex = 0;
				if (worldData.cell.children.cell.length > -1) {
					while (worldData.cell.children.cell[cellIndex]) {
						var top = ((worldData.cell.children.cell[cellIndex].location.y.$-(worldData.cell.children.cell[cellIndex].size.height.$*0.5))/unitFactor)*(gridSize-1);
						var left = ((worldData.cell.children.cell[cellIndex].location.x.$-(worldData.cell.children.cell[cellIndex].size.width.$*0.5))/unitFactor)*(gridSize-1);
						var direction = "0";
						if (worldData.cell.children.cell[cellIndex].rotation) {
							direction = worldData.cell.children.cell[cellIndex].rotation.$;
							switch (direction) {
								case "180":
									direction = "n";
									break;
								case "90":
									direction = "e";
									break;
								case "360":
									direction = "s";
									break;
								case "270":
									direction = "w";
									break;
								default:
									direction = "0";
							}
						}
						var catalogURI = worldData.cell.children.cell[cellIndex].catalogURI.$;
						var catalogIndex = worldData.cell.children.cell[cellIndex].catalogID.$;
						createPlaceable(catalogURI,catalogIndex,top,left,direction,worldData.cell.children.cell[cellIndex].cellID.$);
						cellIndex++;
					}
				} else {
					var top = ((worldData.cell.children.cell.location.y.$-(worldData.cell.children.cell[cellIndex].size.height.$*0.5))/unitFactor)*(gridSize-1);
					var left = ((worldData.cell.children.cell.location.x.$-(worldData.cell.children.cell[cellIndex].size.width.$*0.5))/unitFactor)*(gridSize-1);
					var direction = "0";
					if (worldData.cell.children.cell.rotation) {
						direction = worldData.cell.children.cell.rotation.$;
						switch (direction) {
							case "180":
								direction = "n";
								break;
							case "90":
								direction = "e";
								break;
							case "360":
								direction = "s";
								break;
							case "270":
								direction = "w";
								break;
							default:
								direction = "0";
						}
					}
					var catalogURI = worldData.cell.children.cell.catalogURI.$;
					var catalogIndex = worldData.cell.children.cell.catalogID.$;
					createPlaceable(catalogURI,catalogIndex,top,left,direction,worldData.cell.children.cell.cellID.$);
				}
			}
			
			function putWorld() {
				new Ajax.Request("resources/tree", {
					method:'post',
					postBody: Object.toJSON(worldData),
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
						element.setStyle({visibility:'hidden'});//set the element to not display, then remove it later instead of removing it here (to avoid conflicts between the "revert" event of the object and the "onDrop" event of the trash)
						$('highlight_n').setStyle({visibility: 'hidden'});
						$('highlight_e').setStyle({visibility: 'hidden'});
						$('highlight_s').setStyle({visibility: 'hidden'});
						$('highlight_w').setStyle({visibility: 'hidden'});
					}
				});
			}
			
			function zoomIn() {
				$('world').update('');
				gridSize *= 2;
				gridSize -= 1;
				buildGrid();
				buildWorld();
			}
			
			function zoomOut() {
				$('world').update('');
				gridSize *= 0.5;
				gridSize += 0.5;
				buildGrid();
				buildWorld();
			}
			
			function selectObject(thisObjectID) {
				selectedObject = thisObjectID;
				selectedObjectZ = $(thisObjectID).getStyle('zIndex');
			}
			
			function deselectAll() {
				if (selectedObjectMousePointer == false) {
					$('rotation_controls').setStyle({visibility:'hidden'})
				}
				if (selectedObject) {
					$(selectedObject).setStyle({
						zIndex: selectedObjectZ
					});
				}
			}
			
			//declare globals
			
			var currentCatalogData = new Object();
			var worldData = new Object();
			var objectID = 1;
			var libraryObjectID = 1;
			var gridHTML = new String();
			var selectedObjectMousePointer = false;
			var selectedObject;
			var selectedObjectZ;
			
			//configuration variables
			
			var unitFactor = 2;//how much to multiply the grid coordinates by to convert to wonderland units
			//a note on unitFactor: conveniently it looks like this is equivalent to Blender units
			var gridSize = 65;//IMPORTANT: must be an odd number!
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
	<body onmousedown="deselectAll()">
		<div id="main">
			<div id="library" onscroll="deselectAll()"></div>
			<div id="display"></div>
			<div id="workspace" title="Click and drag to pan workspace" onscroll="deselectAll()" onmousedown="deselectAll()">
				<div id="grid"></div>
				<div id="world"></div>
				<div id="highlight_n"></div>
				<div id="highlight_s"></div>
				<div id="highlight_e"></div>
				<div id="highlight_w"></div>
				<div id="rotation_controls" title="Click arrows to indicate object facing direction" onclick="$('rotation_controls').setStyle({visibility:'hidden'});deselectAll()" onmouseout="selectedObjectMousePointer=false" onmouseover="selectedObjectMousePointer=true">
					<var id="rotation_data"></var>
					<var id="rotation_object"></var>
					<img id="arrow_north" src="arrow_north.png" title="Click to face object north" onclick="setRotation($('rotation_object').innerHTML,'n');$('rotation_controls').setStyle({visibility:'hidden'});deselectAll()" />
					<img id="arrow_south" src="arrow_south.png" title="Click to face object south" onclick="setRotation($('rotation_object').innerHTML,'s');$('rotation_controls').setStyle({visibility:'hidden'});deselectAll()" />
					<img id="arrow_east" src="arrow_east.png" title="Click to face object east" onclick="setRotation($('rotation_object').innerHTML,'e');$('rotation_controls').setStyle({visibility:'hidden'});deselectAll()" />
					<img id="arrow_west" src="arrow_west.png" title="Click to face object west" onclick="setRotation($('rotation_object').innerHTML,'w');$('rotation_controls').setStyle({visibility:'hidden'});deselectAll()" />
				</div>
			</div>
			<!--<div id="debug" style="position: absolute; bottom: 0px; right: 0px; width: 200px; height: 200px; overflow: scroll; background-color: yellow; z-index: 9999"></div>-->
			<div class="button" onclick="zoomIn()" onmousedown="deselectAll()">Zoom In</div>
			<div class="button" onclick="zoomOut()" onmousedown="deselectAll()">Zoom Out</div>
			<div class="button" onclick="putWorld()" onmousedown="deselectAll()">Save World</div>
			<div id="selector" onmousedown="deselectAll()">
				Load Catalog:<input type="text" value="catalog.json" id="catalogtextfield" onmousedown="deselectAll()" />
				Select Library:<select id="libraryswitch" onchange="getLibrary($('libraryswitch').getValue())" onmousedown="deselectAll()"><option>(No catalog loaded)</option></select>
			</div>
			<script type="text/javascript" language="javascript">
				//initialize
				new DragScrollable('workspace');
				buildGrid();
				getWorld();
				createTrash();
				$('catalogtextfield').observe('keypress',submitCatalogURL);
				getCatalog($('catalogtextfield').getValue());
			</script>
		</div>
	</body>
</html>
