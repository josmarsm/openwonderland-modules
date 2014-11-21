<%-- 
    Document   : edit1.jsp
    Created on : Dec 8, 2010, 9:41:51 AM
    Author     : jkaplan
    Edited by  : mhaug
    Modified on: Mar 7, 2011
--%>

<%@page import="org.eclipse.persistence.internal.libraries.asm.tree.analysis.Value"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Editor</title>

        <link href="../isocial-sheets/css/isocial.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="../isocial-sheets/css/jquery.treeTable.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="../isocial-sheets/css/jquery.ui.selectmenu.css" rel="stylesheet" type="text/css" media="screen" />

        <script src="/wonderland-web-front/javascript/json2.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery-ui.min.js" type="text/javascript"></script>      
        <script src="../isocial-sheets/scripts/isocial.js" type="text/javascript"></script>
        <script src="../isocial-sheets/scripts/jquery.treeTable.js" type="text/javascript"></script>
        <script src="../isocial-sheets/scripts/jquery.ui.selectmenu.js" type="text/javascript"></script>

        <style>
            div#mainForm input { padding-bottom: 10px; }

            .divHeaderTable
            {
                        width: 100%;
                        padding-bottom:5px;
                        display:block;
            }
            .divHeaderRow
            {
                        width: 100%; /* add extra that you want to for header column */
                        display:block;
                        height:105px;
            }
            .divHeaderColumn
            {
                        float: left;
                        width: 33%;
                        display:block;
            }
            .divTable
            {
                        width: 80%;
                        display:block;
                        padding-top:10px;
                        padding-bottom:10px;
                        padding-right:10px;
                        padding-left:10px;
                        display:table;
            }
            .divRow
            {
                     width: 99%;
                     display:table-row;
                     padding-bottom:15px;


            }
            .divColumnSmall
            {
                     float: left;
                     width: 25px;
                     display:table-cell;
                     padding-bottom:15px;
            }
            .divColumnMedium
            {
                     float: left;
                     width: 125px;
                     display:table-cell;
                     padding-bottom:15px;
            }
            .divColumnLarge
            {
                     float: left;
                     width: 250px;
                     display:table-cell;
                     padding-bottom:15px;

            }
        </style>

        <script type="text/javascript">
            <c:set var="unitId" value="${param['unitId']}"/>
            <c:set var ="lessonId" value="${param['lessonId']}"/>
            <c:set var="sheetId" value="${param['sheetId']}"/>
   
            $(function() {
                //$( "#publish" ).button().focus();
                $( "#saveButton" ).button();
                $( "#cancelButton" ).button();
                $( ".testButton").button();
                $( ".testButton").click(function() {
                     var huh = new Object();
                     huh.label = "Ryan.";
                     huh.PI = 3.14;

                    $.ajax({
                        type: 'POST',
                        url: 'edit/testing',
                        data: JSON.stringify(huh),
                        success: function(data) {
                        },
                        dataType: 'json',
                        contentType: 'application/json'
                    });
                });

                $("#cancelButton").click(function() {
                    window.location.href="../isocial-sheets/lessons.jsp";
                });
                $("#saveButton").click(function() {
                    saveSheet();
                });

                $("#publishButton").button();
                $("#publishButton").click(function() {
                    publishSheet();
                });
                
                function testQuestions() {
                    var sheet = new Object();
                    sheet.lessonId = "efaf34";
                    sheet.unitId = "f7bf7c";
                    sheet.sheetId = "2d50ba";

                    sheet.questions = new Object();
                    sheet.questions.item = new Array();
                    var wrapper1 = new Object();
                    var wrapper2 = new Object();
                    var wrapper3 = new Object();
                    var MultipleChoiceQuestion1 = new Object();
                    var MultipleChoiceQuestion2 = new Object();
                    var YesNoQuestion = new Object();
                    
                    MultipleChoiceQuestion1.answers = new Array();
                    MultipleChoiceQuestion1.inclusive = true;
                    var answer1 = new Object;
                    var answer2 = new Object;
                    answer1.questionTitle = "What is your name?";
                    answer1.value = "amy";
                    answer2.questionTitle = "What is your name?";
                    answer2.value = "ryan";
                    MultipleChoiceQuestion1.answers.push(answer1);
                    MultipleChoiceQuestion1.answers.push(answer2);
                    MultipleChoiceQuestion1.answers.push(answer1);
                    MultipleChoiceQuestion1.answers.push(answer2);

                    MultipleChoiceQuestion2.answers = new Array();
                    MultipleChoiceQuestion2.inclusive = true;
                    var answer3 = new Object();
                    var answer4 = new Object();
                    answer3.questionTitle = "colors?";
                    answer3.value="green";
                    answer4.questionTitle = "colors?";
                    answer4.value="blue";
                    MultipleChoiceQuestion2.answers.push(answer3);
                    MultipleChoiceQuestion2.answers.push(answer4);
                    MultipleChoiceQuestion2.answers.push(answer3);
                    MultipleChoiceQuestion2.answers.push(answer4);

                    YesNoQuestion.title = "YesNoQuestion";
                    YesNoQuestion.value = "A yes or no question";
                    wrapper1.MultipleChoiceQuestion = MultipleChoiceQuestion1;
                    wrapper2.MultipleChoiceQuestion = MultipleChoiceQuestion2;
                    wrapper3.YesNoQuestion = YesNoQuestion;
                    sheet.questions.item.push(wrapper1);
                    sheet.questions.item.push(wrapper2);
                    sheet.questions.item.push(wrapper3);





                    $.ajax({
                        type: "POST",
                        url: "edit/SheetDetailsTest",
                        data: JSON.stringify(sheet),
                        success: function(data) { },
                        dataType: 'json',
                        contentType: 'application/json'

                    });
                }

                function buildSheet() {
                    var sheet = new Object();
                    sheet.sheetTitle = $('#sheetName').val();
                    sheet.publish = $('#toPublish').val();
                    sheet.unitId = $("#unitid").val();
                    sheet.lessonId = $("#lessonid").val()
                    sheet.sheetId = $("#sheetid").val();
                    sheet.questions = new Object();
                    sheet.questions.item = new Array();

                    if($("#autoOpen").attr("checked").toString == "true") {
                        sheet.autoOpen = true;
                    } else {
                        sheet.autoOpen = false;
                    }

                    if($("#dockable").attr("checked").toString() == "true") {
                        sheet.dockable = true;
                    } else {
                        sheet.dockable = false;
                    }

                    if($("#singleton").attr("checked").toString() == "true") {
                        sheet.singleton = true;
                    } else {
                        sheet.singleton = false;
                    }

                    sheet.directions = $("#directions").val();
                    
                    //for every question
                    $('.tree').each(function(index) {

                        //create the question wrapper object
                        var wrapper = new Object();

                        //create the question object
                        wrapper.MultipleChoiceQuestion = new Object();
                        wrapper.MultipleChoiceQuestion.inclusive = "true";
                        wrapper.MultipleChoiceQuestion.title ="MultipleChoiceQuestion";


                        
                        var value = $(this).find("#questionValue").val();
                        var inclusive = $(this).find("#isInclusive").attr("checked");
                        
                        wrapper.MultipleChoiceQuestion.inclusive = inclusive;
                        

                        //alert(inclusive);
                       
                        var badIndex = value.indexOf("Remove");
                        wrapper.MultipleChoiceQuestion.value = value;//.substr(0, badIndex);
                        //create the answers object
                        wrapper.MultipleChoiceQuestion.answers = new Array();

                        //add all answers                                                
                        $(this).find(".answerString").each(function(index){                            
                            
                            var answer = new Object();
                            answer.questionTitle = value;//value.substr(0, badIndex);
                            answer.value = $(this).val();
                            //populate the answer and add to array in current question
                            wrapper.MultipleChoiceQuestion.answers.push(answer);
                        })
                        
                        //add the question wrapper object to the sheet
                        sheet.questions.item.push(wrapper);
                    });
                    return sheet;
                }

<%--                function sendSheet() {
                    $.ajax({
                        type: 'POST',
                        url: 'edit/update',
                        data: JSON.stringify(buildSheet()),
                        success: function(data) { },
                        dataType: 'json',
                        contentType: 'application/json'

                    });
                }--%>

                function publishSheet() {
                    $.ajax({
                        type: 'POST',
                        url: 'edit/publish',
                        data: JSON.stringify(buildSheet()),
                        success: function(data) {
                            window.location.href="../isocial-sheets/lessons.jsp";
                        },
                        dataType: 'json',
                        contentType: 'application/json'
                    });
                }

                function saveSheet() {
                    $.ajax({
                        type: 'POST',
                        url: 'edit/save',
                        data: JSON.stringify(buildSheet()),
                        success: function(data) {
                            window.location.href="../isocial-sheets/lessons.jsp";
                        },
                        dataType: 'json',
                        contentType: 'application/json'
                    });
                }

                function sendSample() {
                    $.ajax({
                        type: 'GET',
                        url: 'edit/Sample'
                    });
                }

            });
        </script>

        <script type="text/javascript">
            var questionAmount = 1;
            var questionType = "mc";
            
            function update() {
                var urlString =  "edit/" + $("#unitid").val() +"/"+$("#lessonid").val() + "/" + $("#sheetid").val();

                $.getJSON(urlString, function(data) {
                    if(!(data.questions instanceof Array)) {
                        var q = data.questions;
                        data.questions = new Array();
                        data.questions.push(q);
                    }

                    $(".sheetName").val(data.name);
                    $("#directions").val(data.directions);

                    if(data.singleton == "true") {
                        $("#singleton").attr("value", "true");
                        $("#singleton").attr("checked", true);
                    } else {
                        $("#singleton").attr("value", "false");
                        $("#singleton").attr("checked", false);
                    }


                    $.each(data.questions, function(i, question) {
                        reconstructQuestion(question);
                    });

                    if(data.dockable == "true") {
                        $("#dockable").attr("value", "true");
                        $("#dockable").attr("checked", true);
                    } else {
                        $("#dockable").attr("value", "false");
                        $("#dockable").attr("checked", false);
                    }

                    if(data.autoOpen == "true") {
                        $("#autoOpen").attr("value", "true");
                        $("#autoOpen").attr("checked", true);

                    } else {
                        $("#autoOpen").attr("value", "false");
                        $("#autoOpen").attr("checked", false);
                    }

                    buildTable();
                    buildAnswerButton();
                    buildRemoveButtons();
                });
            }
            
            function buildTable() {
                $(".tree").treeTable({
                    initialState: "expanded"
                });
            }

            function buildAnswerButton() {
                    $('.addAnswer').button('destroy').button();
                    $('.addAnswer').click(function(event) {
                        //          button->  td -> tr     -> table -> inside -> header -> header id
                        var rowID = $(this).parent().parent().parent().children().first().attr('id');
                        var answerString = constructAnswerString("child-of-"+rowID);
                        //inject the answer string into the DOM before this button
                        //button->td->   tr->     answerString
                        $(this).parent().parent().before(answerString);
                        buildTable();
                        buildRemoveButtons();
                        event.stopImmediatePropagation();

                    });
                }

            function buildRemoveButtons() {
                    $('.removeTable').button('destroy').button();
                    $('.removeTable').click(function(event) {
                        $(this).closest('.tree').remove();
                    });

                    $('.removeAnswer').button('destroy').button();
                    $('.removeAnswer').click(function(event) {
                        $(this).closest('#answer').remove();
                    });
                }

            function reconstructQuestion(question) {
                var isOpenEnded = false;
                if(question.answers == null) { 
                    isOpenEnded = true;
                }
                else if(!(question.answers instanceof Array)) {
                    var a = question.answers;
                    question.answers = new Array();
                    question.answers.push(a);
                    alert("Not an array!");
                }
                
                var questionTable = "";
                if(isOpenEnded == true) {
                    /* construct open ended question instead */
                    questionTable = "<table class='tree'>";
                    questionTable += "<tr id='Q"+questionAmount+"' class='question'>";
                    questionTable +=    "<td><input id='questionValue' type='text' value=\""+question.value+"\"></input></td>";
                    questionTable +=    "<td><button class='removeTable'>Remove</button></td>";
                    questionTable += "</tr></table> <br/>";
                    
                } else {
                    /* construct multiple-choice question*/
                    questionTable = "<table class='tree'>";
                    questionTable += "<tr id='Q"+questionAmount+"' class='question'>";
                    questionTable += "<td><input id='questionValue' type='text' value=\""+question.value+"\"></input></td> ";
                    questionTable += "<td><button class='removeTable'>Remove</button></td>"
                    questionTable += "<td><label for='isInclusive'>Inclusive?</label>";
                    if(question.inclusive == "true") {
                        questionTable += "<input id='isInclusive' type='checkbox' checked="+true+"></input></td>";

                    } else {
                        questionTable += "<input id='isInclusive' type='checkbox'></input></td>";

                    }
                    questionTable += "</tr>";
                    $.each(question.answers, function(i, answer) {
                        questionTable += reconstructAnswer("Q"+questionAmount, answer);

                    })
                    questionTable += "<tr id='buttonRow' class='child-of-Q"+questionAmount+"'>";
                    questionTable += "<td><button class='addAnswer'>Add Answer</button></td></tr>";
                    questionTable += "</table><br/>";
                }



                $("#questions").append(questionTable);
                questionAmount += 1;
            }

            function reconstructAnswer(className, answer) {
                if(answer == null) {
                    return "";
                }
                var answerString = "<tr id='answer' class='child-of-"+className+"'>";
                answerString += "<td><input type='text' name='answer' class='answerString' value=\""+answer.value+"\"></input></td>";
                answerString += "<td><button class='removeAnswer'>Remove</button>";
                answerString += "</td>";
                answerString += "</tr>";
                return answerString;
            }

            $(document).ready(function() {
                

                //create the button to add a question
                $(".addQuestionButton").button();
                $(".addQuestionButton").click(function() {
                   $("#add-new-question").dialog('open');

                });

                //create the initial tree
                buildTable();

                //build the initial answer button
                buildAnswerButton();

                //build the initial remove buttons
                buildRemoveButtons();
            
                var testActions = [["mc", "Multiple Choice"],["open", "Open Ended"]];
                $("#add-new-question").append(generateActions("testing-select", testActions));
                $("select#testing-select").selectmenu({
                    select: function(event, options) {
                        switch(options.value) {
                            case "mc":
                                questionType = "mc";
                                break;
                            case "open":
                                questionType ="open";                               
                                break;
                        };
                    }
                });
                //create modal dialog
                $("#add-new-question").dialog({
                    autoOpen: false,
                    resizable: false,
                    height: 280,
                    modal: true,
                    buttons: {
                        "Add Question": function() {
                            questionAmount += 1;
                            var questionTable = "";
                            if(questionType == "mc") {
                               questionTable += buildMCString();
                            } else {
                                questionTable += buildOpenString();
                            }

                            //insert the table in the correct area, before the
                            //add question button
                            $('#questions').append(questionTable);
                            buildTable();

                            //destroy any lingering buttons and recreate them
                            buildAnswerButton();

                            //destroy any lingering Remove buttons and recreate
                            //them
                            buildRemoveButtons();

                            $(this).dialog('close');
                        },
                        Cancel: function() {
                            $(this).dialog('close');
                        }
                    }
                });
               
                function buildMCString() {
                    var questionTable = "<table class='tree mc'>";
                    questionTable += "<tr id='Q"+questionAmount+"' class='question'>";
                    questionTable += "<td><input id='questionValue' type='text' value=\""+$('#question-value').val()+"\"></input></td> ";
                    questionTable += "<td><button class='removeTable'>Remove</button></td>"
                    questionTable += "<td><label for='isInclusive'>Inclusive?</label>";
                    questionTable += "<input id='isInclusive' type='checkbox' checked='false'></input></td>";
                    questionTable += "</tr><tr id='buttonRow' class='child-of-Q"+questionAmount+"'>";
                    questionTable += "<td><button class='addAnswer'>Add Answer</button></td></tr>";
                    questionTable += "</table><br/>";
                    return questionTable;
                }
                
                function buildOpenString() {
                    var questionTable = "<table class='tree open'>";
                    questionTable += "<tr id='Q"+questionAmount+"' class='question'>";
                    questionTable += "<td><input id='questionValue' type='text' value=\""+$('#question-value').val()+"\"></input></td> ";
                    questionTable += "<td><button class='removeTable'>Remove</button></td>"

                    questionTable += "</tr>";

                    questionTable += "</table><br/>";
                    return questionTable;
                }
               
                function buildTable() {
                    $(".tree").treeTable({
                        initialState: "expanded"
                    });
                }

                function constructAnswerString(className) {
                    
                    var answerString = "<tr id='answer' class='"+className+"'>";
                    answerString += "<td><input type='text' name='answer' class='answerString'></input></td>";
                    answerString += "<td><button class='removeAnswer'>Remove</button>";
                    answerString += "</td>";
                    answerString += "</tr>";
                    return answerString;
                }

                function buildAnswerButton() {
                    $('.addAnswer').button('destroy').button();
                    $('.addAnswer').click(function(event) {
                        //          button->  td -> tr     -> table -> inside -> header -> header id
                        var rowID = $(this).parent().parent().parent().children().first().attr('id');                       
                        var answerString = constructAnswerString("child-of-"+rowID);
                        //inject the answer string into the DOM before this button
                        //button->td->   tr->     answerString
                        $(this).parent().parent().before(answerString);
                        buildTable();
                        buildRemoveButtons();
                        event.stopImmediatePropagation();

                    });
                }
                
                function buildRemoveButtons() {
                    $('.removeTable').button('destroy').button();
                    $('.removeTable').click(function(event) {
                        $(this).closest('.tree').remove();
                    });

                    $('.removeAnswer').button('destroy').button();
                    $('.removeAnswer').click(function(event) {
                        $(this).closest('#answer').remove();
                    });                                            
                }

                function addElement() {
                  var numi = document.getElementById('theValue');

                  var num = (document.getElementById('theValue').value -1)+ 2;

                  numi.value = num;



                  var divIdName = 'my'+num+'Div';
                  var htmlString = "";
                  htmlString += "<h3><a href='#'>Question "+num+"</a></h3>";
                  htmlString += "<div id='"+divIdName+"' class='divTable'><div class='divRow'>";
                  htmlString += "<select id='qType"+num+"' name='questionType' onchange='addQuestion("+num+");'>";
                  htmlString += "<option value='0' name ='spaceholder' disabled='disabled' selected='selected'>Please choose a question.</option>";
                  htmlString += "<option value='1' name ='openEnded'    >Open Ended Question</option>";
                  htmlString += "<option value='2' name ='multipleChoice'   >Multiple Choice Question</option>";
                  htmlString += "</select></div>";
                  htmlString += "<div id='myDiv"+num+"'></div>";
                  htmlString += "<button class='removeButton'>Remove</button></div>";
                  return htmlString;


                }

                function addQuestion(num){
                  var divName = "myDiv"+num;
                  var ni = document.getElementById(divName);
                  var dropDownName = "qType"+num;
                  var e = document.getElementById(dropDownName);
                  var qPick = e.options[e.selectedIndex].value;
                  if(qPick == 1)
                  {
                    ni.innerHTML = addOpenEnded();
                  }
                  else if(qPick ==2)
                  {
                    ni.innerHTML = addMultipleChoice();
                  }

                }
                function addOpenEnded(){
                    var htmlString = "";
                    htmlString = '<div class ="divRow"><div class="divColumnMedium"><label for="question">Question Text:</label></div><div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="question" value=""/></div>';
                    return htmlString;
                }
                function addMultipleChoice(){
                    var htmlString = ""
                    htmlString = '<div class ="divRow"><div class="divColumnMedium"><label for="question">Question Text:</label></div><div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="question" value=""/></div></div>';
                    htmlString += '<div class ="divRow"><div class="divColumnSmall"><input class="ui-widget-content ui-corner-all" type="checkbox" name="allowMultiple" value="true"/></div><div class="divColumnLarge"><label for="allowMultiple">Allow Multiple Selection?</label></div></div>';
                    htmlString += '<div class ="divRow"><div class="divColumnMedium"><label for="option1">Option 1</label></div><div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="option1" value=""/></div></div>';
                    htmlString += '<div class ="divRow"><div class="divColumnMedium"><label for="option2">Option 2</label></div><div class="divColumnLarge"><input class="ui-widget-content ui-corner-all" size="30" type="text" name="option2" value=""/></div></div>';
                    return htmlString;
                }
                update();
            });
        </script>
    </head>
    <body>
        <c:set var="details" value="${requestScope['sheet'].details}" />
        <div id="mainForm" class="ui-widget-container ui-widget ui-corner-all">
            <h1>Edit ${details.name}</h1>
<%--
            <form id="editForm" action="#">



                <div class="divTable">
                    <div class="divRow">
                        <div class="divColumnMedium">
                        <label for="name">Sheet Name:</label>
                        </div>
                        <div class="divColumnLarge">
                            <input class="ui-widget-content ui-corner-all" size="30" type="text" name="name" value="${details.name}"/>
                        </div>
                        <div class="divColumnSmall">
                           <input class="ui-widget-content ui-corner-all" type="checkbox" name="autoOpen" value="true"
                           <c:if test="${details.autoOpen}">checked="checked"</c:if>/>
                        </div>
                        <div class="divColumnMedium">
                           <label for="autoOpen">Visible on startup?</label>
                        </div>
                    </div>

                    <%--
                        <label for="question">Question:</label>
                        <input class="ui-widget-content ui-corner-all" size="30" type="text" name="question" value="${details.question}"/>
                     

                    <div class="divRow" id="newQuestion">

                        <div class="divColumnMedium">
                            <select id="qType" name="questionType">
                                <option value="1" name ="openEnded">Open Ended Question</option>
                                <option value="2" name ="multipleChoice">Multiple Choice Question</option>
                            </select>
                        </div>
                    </div>

                    <input type="hidden" value="0" id="theValue" />


                    <div id="myDiv"> </div>

                    <div class = "divRow">
                        <div class="divColumnMedium">
                            <input class="ui-widget-content ui-corner-all" type="button" name="questionType" value="+ Add another question"   onclick="addElement();"/>
                        </div>
                    </div>
                    <div class="divRow>
                        <div class="divColumnLarge">
                            <input id="publish" name="action" value="Publish" type="submit"/>
                            <input id="save" name="action" value="Save" type="submit"/>
                            <input id="cancel" name="action" value="Cancel" type="submit"/>
                        </div>
                    </div>
               
            </form>
            --%>
            <input type="hidden" value="0" id="theValue" />
        <div class="divTable">
            <div class="divRow">
                <div class="divColumnMedium">
                    <label for="name">Sheet Name:</label>
                </div>
                <div class="divColumnLarge">
                    <input id="sheetName" class="ui-widget-content ui-corner-all sheetName" size="30" type="text" name="name" value="${details.name}"/>
                </div>
                <div class="divColumnSmall">
                    <input id="autoOpen" class="ui-widget-content ui-corner-all" type="checkbox" name="autoOpen" value="true"
                    <c:if test="${details.autoOpen}">checked="true"</c:if>/>
                </div>
                <div class="divColumnMedium">
                    <label for="autoOpen">Visible on startup?</label>
                </div>
            </div>
            <div class="divRow">
                <div class="divColumnMedium">
                    <label for="directions">Directions: </label>
                </div>
                <div class="divColumLarge">
                    <input id="directions" class="ui-widget-content ui-corner-all directionsNAme" size="60" type="text" name="directions" value="none" />
                </div>
            </div>
            <div class="divRow">
                <div class="divColumnMedium">
                    <label for="dockable">Dockable?</label>
                </div>
                <div class="divColumnSmall">
                    <input id="dockable" class="ui-widget-content ui-corner-all" type="checkbox" name="dockable" value="true"/>
                </div>
            </div>
            <div class="divRow">
                <div class="divColumnMedium">
                    <label for="singleton">Single Unit Usage?</label>
                </div>
                <div class="divColumnSmall">
                    <input id="singleton" class="ui-widget-content ui-corner-all" type="checkbox" name="singleton" value="true"/>
                </div>
            </div>

        </div>

            <div id="questions">                                    
               <%-- <table class="tree">
                    <tr id="Q1" class="question">
                        <td class='questionValue'>What is your name?</td>
                        <td><button class='removeTable'>Remove</button></td>
                    </tr>
                    <tr id="node" class="child-of-Q1">
                        <td><button class="addAnswer">Add Answer</button></td>
                    </tr>
                </table>--%>
            </div>

            <div id="add-new-question" title="New Question">
                <p>Enter a new question:</p><br/><br/>                               
                <input type='text' name="question" id="question-value"/>

            </div>
            <button id="publishButton">Publish</button>
            <button id="saveButton">Save</button>
            <button id="cancelButton">Cancel</button>
            <button class="addQuestionButton">Add Question</button>
            <input type="hidden" name="unitid" id="unitid" value="${unitId}"/>
            <input type="hidden" name="lessonid" id="lessonid" value="${lessonId}"/>
            <input type="hidden" name="sheetid" id="sheetid" value="${sheetId}"/>

        </div>
    </body>
</html>
