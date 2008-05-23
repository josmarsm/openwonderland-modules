//This is the start of the SVG version -- do not develop further until Firefox fixes the image-rendering attribute bug
function createLibraryObjectSVG(catalogIndex,top,width,height) {
	var thisObjectID = "obj" + objectID;
	var right = getScrollerWidth() + 10;
	var insertHTML = "<div style='top:" + top + "px;right:" + right + "px;width:" + width +"px;height:" + height + "px' id='" + thisObjectID + "' ><object type='image/svg+xml' data='draw.svg?image=" + catalogData.catalog[catalogIndex].tileImageURL + "&width=" + width + "&height=" + height + "' width='" + width + "' height='" + height + "'></object><img alt='" + catalogIndex + "' src='blank.gif' width='" + width + "' height='" + height + "' title='" + catalogData.catalog[catalogIndex].name + ": " + catalogData.catalog[catalogIndex].description + "' /></div>\n";
	$('library').insert(insertHTML);
	new Draggable(thisObjectID,{
		revert:function() {
			var workspaceTop = parseInt($('workspace').getStyle('top'));
			var workspaceLeft = parseInt($('workspace').getStyle('left'));
			var thisLocation = $(thisObjectID).viewportOffset();
			var worldLocation = $('world').viewportOffset();
			var workspaceDimensions = $('workspace').getDimensions();
			var scrollbarWidth = getScrollerWidth();
			var top = (gridSize - 1) * Math.floor((thisLocation.top - worldLocation.top)/(gridSize - 1));
			var left = (gridSize - 1) * Math.floor((thisLocation.left - worldLocation.left)/(gridSize - 1));
			var thisImage = $(thisObjectID).childElements();
			thisImage = thisImage[1];
			if (thisLocation.left >= workspaceLeft && thisLocation.left <= (workspaceLeft + (workspaceDimensions.width - scrollbarWidth)) && thisLocation.top >= workspaceTop && thisLocation.top <= (workspaceTop + (workspaceDimensions.height - scrollbarWidth))) {
				createPlaceable(thisImage.readAttribute('alt'),width,height,top,left);
			}
			$('library').setStyle({zIndex:'1'});
			$(thisObjectID).remove();
		},
		onStart:function() {
			var thisLocation = $(thisObjectID).positionedOffset();
			var thisDimensions = $(thisObjectID).getDimensions();
			var thisImage = $(thisObjectID).childElements();
			thisImage = thisImage[1];
			createLibraryObject(thisImage.readAttribute('alt'),(thisLocation.top + 1),thisDimensions.width,thisDimensions.height);
			$('library').setStyle({zIndex:'3'});
		}
	});
	objectID++;
}

function createPlaceableSVG(catalogIndex,width,height,top,left) {
	var thisObjectID = "obj" + objectID;
	var insertHTML = "<div id='" + thisObjectID + "' class='draggable' style='top:" + top + "px;left:" + left + "px;width:" + width + "px;height:" + height + "px'><object type='image/svg+xml' data='draw.svg?image=" + catalogData.catalog[catalogIndex].imageURL + "&width=" + width + "&height=" + height + "' width='" + width + "' height='" + height + "'></object><img alt='" + catalogData.catalog[catalogIndex].modelURL + "' src='blank.gif' width='" + width + "' height='" + height + "' title='" + catalogData.catalog[catalogIndex].name + ": " + catalogData.catalog[catalogIndex].description + "' /></div>\n";
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
		}
	});
	objectID++;
}
