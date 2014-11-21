/*
 * Fix an array. If the given object is null return an empty array. If the
 * given object is a regular object, create an array with just that object.
 * If the given object is an array, return it.
 *
 * This is useful to workaround issues with JSON parsing sometimes returning
 * an array and sometimes returning a single item.
 */
function ensureArray(obj) {
    if (obj == null) {
        return new Array();
    } else if (!(obj instanceof Array)) {
        return [ obj ];
    } else {
        return obj;
    }
}

/*
 * Convert an object into an array by pushing each property into a new
 * array.
 */
function toArray(obj) {
    var out = new Array();

    for (var i in obj) {
        out.push(obj[i]);
    }

    return out;
}

/**
 * Rebuild a tree-table by reapplying the treee-table properties to it, and
 * rexampdning any nodes that should be expanded.
 *
 * Parameters:
 *  table - the name of thetable to rebuild
 *  expanded - the ids of the rows that should be expanded
 */
function rebuildTable(table, expanded) {
    // make sure the table updates properly by removing treeTable
    // classes. Also save the list of expanded rows
    $( "#" + table + " tr").each(function(row) {
        $(this).removeClass("initialized");
    });

    $( "#" + table ).treeTable({
        initialState: "collapsed"
    });

    $.each(expanded, function(i, id) {
        $( "#" + id ).expand();
    });
}

/**
 * Append to a subsection of a tree-table
 *
 * Parameters:
 *  id: the id of the parent
 *  html: the html to append
 */
function appendTo(id, html) {
    // see if there are already children to append to
    var appendPoint = $( ".child-of-" + id + ":last");
    if (appendPoint.length == 0) {
        appendPoint = $( "#" + id );
    }

    appendPoint.after(html);
}

/**
 * Prepend to a subsection of a tree-table
 *
 * Parameters:
 *  id: the id of the parent
 *  html: the html to append
 */
function prependTo(id, html) {
    // see if there are already children to append to
    var prependPoint = $( ".child-of-" + id + ":first");
    if (prependPoint.length == 0) {
        // if there are no children, just add ourselves as the first child
        $( "#" + id ).after(html);
    }

    prependPoint.before(html);
}

/**
 * Turn a set of actions into a select menu
 *
 * Parameters:
 *  id: the id to give the select
 *  actions: a set of actions to include in the table
 */
function generateActions(id, actions) {
    var out = "<form action=\"#\"><fieldset> " +
    "<select name=\"" + id + "\" id=\"" + id + "\">" +
    "<option class=\"invisible\">Select action</option>";
    
    $.each(actions, function(index, action) {
        out += "<option value=\"" + action[0] + "\">" + action[1] +
        "</option>";
    });

    out += "</select></fieldset></select></form>";
    return out;
}

/**
 * Show the results dialog for a particular unit, lesson and sheet
 * 
 * Parameters:
 *  unit: optional unit to show results for
 *  lesson: optional lesson to show results for
 *  sheet: options sheet to show results for
 */
function getResults(unit, lesson, sheet) {
    var prefix = "?";
    var query = "";

    if (unit) {
        query += prefix + "unitId=" + unit.id;
        prefix = "&";
    }

    if (lesson) {
        query += prefix + "lessonId=" + lesson.id;
        prefix = "&";
    }

    if (sheet) {
        query += prefix + "sheetId=" + sheet.id;
        prefix = "&";
    }

    window.location.href="results.jsp" + query;
}

/**
 * method to enable / disable buttons in a dialog
 */
$.fn.dialogButtons = function(name, state)  {
    var buttons = $(this).next('div').find('button');
    if(!name) {
        return buttons;
    }

    return buttons.each(function(){
        var text = $(this).text();
        if(text==name && state=='disabled') {
            $(this).attr('disabled',true).addClass('ui-state-disabled');
            return this;
        }
        if(text==name && state=='enabled') {
            $(this).attr('disabled',false).removeClass('ui-state-disabled');
            return this;
        }
        if(text==name){
            return this;
        }
        if(name=='disabled'){
            $(this).attr('disabled',true).addClass('ui-state-disabled');
            return buttons;
        }
        if(name=='enabled'){
            $(this).attr('disabled',false).removeClass('ui-state-disabled');
            return buttons;
        }
    });
};

