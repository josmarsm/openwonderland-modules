
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

        <script src="../isocial-sheets/scripts/underscore.js" type="text/javascript"></script>
        <script src="../isocial-sheets/scripts/handlebars.js" type="text/javascript"></script>
        <script type="text/javascript" src="<% application.getContextPath(); %>html/js/swfobject.js"></script>
        <script type="text/javascript" src="<% application.getContextPath(); %>html/js/recorder.js"></script>

        
        <%--
        <script type="text/javascript"
        src="https://ajax.googleapis.com/ajax/libs/swfobject/2.2/swfobject.js"></script>

        <!-- Setup the recorder interface -->
        <script type="text/javascript" src="recorder/recorder.js"></script>

        <!-- GUI code... take it or leave it -->
        <script type="text/javascript" src="recorder/gui.js"></script>--%>
        
        <script>
        function setupRecorder() {
        var appWidth = 24;
                            
                        Wami.setup({
                                id : "wami",
                                onReady : setupGUI
                        });
                }

                function setupGUI() {
                        var gui = new Wami.GUI({
                                id : "wami",
                                recordUrl : "saveaudiofile?fname="+fname,
                                playUrl : "https://wami-recorder.appspot.com/audio"
                        });

                        gui.setPlayEnabled(false);
                }
        </script>
        <style>
            textarea {
                resize: none;
            }
            
            div#mainForm h1 {
                text-align: center;
            }
            
            div#mainForm input { 
                padding-bottom: 10px; 
            }
            
            div#sheet-area {
                border: 1px solid black;
                min-height: 480px;
                padding-bottom: 10px;
            }
            
            div#top-questions {
                padding: 10px 15px 10px 10px;
                background-color: lightgray;
            }
            
            div#sheet-area label, div#sheet-area input { 
                display: inline; 
            }
            
            div#sheet-checkboxes {
                padding-top: 10px;
            }
            
            div#sheet-checkboxes label {
                padding-right: 25px;
            }
            
            div#bottom-buttons {
                margin: 10px 0px 10px 0px;
                float: right;
            }
            
            div.question-controls-holder {
                height: 32px;
                padding-right: 5px;
            }
            
            div.question-controls {
                float: right;
                padding-top: 10px;
            }
            
            div.question-controls button {
                padding: 0px;
                width: 44px;
                height: 16px;
                font-size: 10px;
            }
            
            div.question-controls .edit-button span,
            div.question-controls .delete-button span {
                padding: 0px;
                font-size: 10px;
                position: absolute;
                top: 50%;
                margin-top: -8px;
                text-align: center;
                width: 42px;
            }
            
            div.question-content {
                margin: 0px 5px 0px 5px;
                padding: 2px 5px 2px 5px;
                border: 1px dashed black; 
            }
            
            div.question-content .question-field {
                width: 100%;
                color: gray;
            }
            
            div.question-content .question-audio {
                width: 100%;
                color: gray;
            }
            
            div.multiple-question-content ul {
                list-style: none;
            }
            
            div.multiple-question-content ul li.other-li input,
            div.multiple-question-content ul li.other-li label
            {
                position: relative;
                top: -9px;
            }
            
            div#edit-dialog label {
                font-weight: bold;
                margin-top: 10px;
                margin-bottom: 4px;
            }
            
            
            
            #edit-question-text {
                width: 100%;
            }
            
            #edit-question-type {
                width: 180px;
            }

            #edit-question-details {
                margin-top: 20px;
            }
            
            #edit-field-lines-label {
                display: inline;
                position: relative;
                top: -8px;
            }
            
            #edit-field-lines {
                display: inline;
            }
            
            #edit-field-instructions {
                width: 100%;
            }
            
            #edit-audio-audioinstructions {
                width: 100%;
            }
            
            #edit-audio-allowedreplays-label {
                float : left;
                font-weight: bold;
                margin-top: 3px;
                margin-bottom: 4px;
            }
            
            #edit-audio-allowedreplays {
                display : inline;
            }
            
            #edit-audio-audiolines-label {
                float : left;
                font-weight: bold;
                margin-top: 3px;
                margin-bottom: 4px;
            }
            #edit-audio-audiolines {
                display : inline;
            }
            
            #edit-audio-audioinstructions-label {
                float : left;
                font-weight: bold;
                margin-top: 3px;
                margin-bottom: 4px;
            }
            #edit-audio-audioinstructions {
                display : inline;
            }
            
            #audio-play-button {
                width: 87px;
                height: 24px;
                vertical-align: middle;
            }
            
            #edit-recording-maxlength {
                display: inline;
                
            }
            
            #edit-audio-maxlength-label {
                display: inline;
                position: relative;
                top: -8px;
            }
            #edit-recording-maxlength-label {
                display: inline;
                position: relative;
                top: -8px;
            }
            
            #edit-recording-maxlength-label0 {
                display: inline;
                position: relative;
                top: -8px;
            }
            
            #edit-recording-maxlength-label1 {
                display: inline;
                position: relative;
                top: -8px;
                color: grey;
                font-weight: normal;
                margin-left: 17px;
                font-size: 11px;
            }
            
            div#edit-dialog .instruction-text {
                font-weight: normal;
                font-style: italic;
            }
            
            .audioinstructions-text {
                font-weight: normal;
                font-style: italic;
            }
            
            div#edit-multiple-div textarea {
                width: 100%;
            }
            
            div#edit-multiple-other-div, div#edit-multiple-allow-multiple-div {
                margin-top: 15px;
            }
            
            div#edit-multiple-other-div input,
            div#edit-multiple-other-div label,
            div#edit-multiple-allow-multiple-div input,
            div#edit-multiple-allow-multiple-div label
            {
                display: inline;
                font-weight: normal;
            }
        </style> 
        <style>
        #control_panel { white-space: nowrap; }
        #control_panel a { outline: none; display: inline-block; width: 24px; height: 24px; }
        #control_panel a img { border: 0; }
        #save_button { position: absolute; padding: 0; margin: 0; }
        #play_button { display: inline-block; }
        </style>
        
        <script type="text/javascript">
            //Play Question
            var soundEmbed = null;
            function PlaySound(filename,id)
            {
                $.ajax({
                    type: 'POST',
                    url: "convertaufile",
                    data: {filepath : "/content/groups/users/audiosheet/"+filename+".au"},
                    success: function(data) {
                        var form = document.getElementById("form-"+id);
                        form.style.display = "none";
                        soundEmbed = "<embed height='70px' src='http://"+location.hostname+":8080/webdav/content/groups/users/audiosheet/"+filename+".wav' autostart='true'></embed>"
                        var div = document.getElementById("div-"+id);
                        div.innerHTML = soundEmbed;
                    }
                });
            }
            var questions;
            var file_saved=0;
            var recording=0;
            $(function() {
                Handlebars.registerHelper('string-if', function(str, options) {
                    if(/true/i.test(str)) {
                        return options.fn(this);
                    } else {
                        return options.inverse(this);
                    }
                });
                
                $('.edit-button').button();
                $('#save').focus();
            
                $('#add-question').click(function() {
                    
                    editQuestionDialog();
                });
                
                $.getJSON('edit/${it.unitId}/${it.lessonId}/${it.sheetId}', function(data) {
                   
                    loadData(data);
                })
                
                $('#save').click(function() {
                    $.ajax({
                        type: 'POST',
                        url: 'edit/${it.unitId}/${it.lessonId}/${it.sheetId}',
                        data: formToJSON(),
                        dataType: 'text',
                        contentType: 'application/json',
                        success: function(data) {
                            window.location.href=data;
                        }
                    });
                });
                
                $('#duplicate').click(function() {
                    // changes12 copy audio files
                    $.ajax({
                        type: 'POST',
                        url: 'edit/${it.unitId}/${it.lessonId}/${it.sheetId}/duplicate',
                        data: formToJSON(),
                        dataType: 'text',
                        contentType: 'application/json',
                        success: function(data) {
                            window.location.href=data;
                        }
                    });
                });
                
                $('#cancel').click(function() {
                    // changes12 delete audio files
                    $.ajax({
                        type: 'GET',
                        url: 'edit/${it.unitId}/${it.lessonId}/${it.sheetId}/cancel',
                        dataType: 'text',
                        success: function(data) {
                            window.location.href=data;
                        }
                    });
                });
            });
            
            function loadData(data) {
                $('#name-field').val(data.name);
                $('#autoOpen').prop("checked", /true/i.test(data.autoOpen));
                $('#visible').prop("checked", /true/i.test(data.dockable));
                
                questions = fixJSON(data.questions);
                renderQuestions();
            }
            
            var templates = {};
            function getTemplate(name) {
                if (!templates[name]) {
                    templates[name] = Handlebars.compile($("#" + name).html());
                }
                
                return templates[name];
            }
            
            function fixJSON(json) {
                _.each(json, function(question) {
                    if (question.properties && question.properties.item) {
                        if(question.properties.item instanceof Array) {
                            _.each(question.properties.item, function(item) {
                                question[item.key] = item.value;
                            });
                        } else {
                            var a = question.properties.item;
                           question[a.key] = a.value; 
                        }
                    }
                    delete question.properties;
                    questionType(question.type).parseJSON(question);
                });
                
                return json;
            }
            
            function questionsToJSON(questions) {
                var NO_CONVERT = ['id', 'text', 'type'];
                
                _.each(questions, function(question) {
                    var properties = null;
                    
                    questionType(question.type).toJSON(question);
                    
                    _.each(question, function(val, key) {                        
                        if (_.indexOf(NO_CONVERT, key) == -1) {
                            if (!properties) {
                                properties = {
                                    item: []
                                }
                            }
                                                        
                            properties.item.push({
                                "key": key,
                                "value": val
                            });
                            
                            delete question[key];
                        }
                    });
                    
                    if (properties) {
                        question.properties = properties;
                    }
                });
                
                return questions;
            }
            
            function formToJSON() {
                var out = {
                    name: $("#name-field").val(),
                    autoOpen: $("#autoOpen").is(":checked"),
                    dockable: $("#visible").is(":checked"),
                    questions: questionsToJSON(questions)
                };
                
                return JSON.stringify(out);
            }
            
            function renderQuestions() {
                // sort questions
                questions = _.sortBy(questions, function(question) {
                    return question.id;
                });
                
                var template = getTemplate('tmpl-question');
                $('#sheet-questions').empty();
                
                _.each(questions, function(question) {
                    var type = questionType(question.type);
                    
                    var questionTemplate = type.renderTemplate();
                    var questionJSON = type.prerender(question);
                    
                    Handlebars.registerPartial("question", questionTemplate);
                    var rendered = template(questionJSON);  
                    var appended = $(rendered).appendTo('#sheet-questions');
                    
                    appended.find('.edit-button').button().click(function() {
                        
                        // changes12 bind audio sile
                        
                        editQuestionDialog(question);
                    });
                    appended.find('.delete-button').button().click(function() {
                        
                        // changes12 delete audio files
                        
                        removeQuestion(question.id);
                    });
                    appended.find('.up-button').button({
                        icons: {
                            primary: "ui-icon-triangle-1-n"
                        },
                        text: false
                    }).click(function() {
                        moveDown(question.id);
                    });
                    appended.find('.down-button').button({
                        icons: {
                            primary: "ui-icon-triangle-1-s"
                        },
                        text: false
                    }).click(function() {
                        moveUp(question.id); 
                    });
                });
            }
            
            function editQuestionDialog(question) {
                var question = question || {};
                var template = getTemplate("tmpl-edit-dialog");
                var rendered = template(question);
                var appended = $(rendered).appendTo('body');
                
                appended.find('#edit-question-type option[value="' + 
                              question.type + '"]').attr("selected", "true");
                
                appended.find("#edit-question-type").selectmenu({
                    select: function(event, options) {
                        loadDetails({type: options.value}, $("#edit-question-details"));
                    }
                });
                
                if (question.type) {
                    loadDetails(question, $("#edit-question-details"));
                }
                
                var buttons = [{
                    text: "Cancel",
                    click: function() {
                        
                        // changes12 delete generated au file
                        
                        $("#edit-dialog").dialog("close");
                    }
                }];
                if (!question.type) {
                    buttons.push({
                        text: "Save & Add Question", 
                        click: function() {
                            
                            // changes12 save wave & au file
                            
                            addQuestion(dialogToQuestion(question));
                            $("#edit-dialog").dialog("close");
                            editQuestionDialog();
                        }
                    });
                }
                buttons.push({
                    text: "Save",
                    click: function() {
                        
                        // changes12 save wave & au file
                        
                        addQuestion(dialogToQuestion(question));
                        $("#edit-dialog").dialog("close");
                    }
                });
                
                appended.dialog({
                    title: question.type?"Edit Question":"Add Question",
                    width: 480,
                    height: 500,
                    buttons: buttons,
                    close: function() {
                        $("#edit-dialog").remove();
                    }
                });
            }
            
            function loadDetails(question, div) {
                div.empty();

                questionType(question.type).loadDetails(question, div);
            }
            
            function dialogToQuestion(question) {
                var result = {
                    id: question?question.id:null,
                    type: $("#edit-question-type").val(),
                    text: $("#edit-question-text").val()
                };
                
                return questionType(result.type).toQuestion(result);
            }
            
            function addQuestion(question) {
                if (question.id) {
                    questions[question.id] = question;
                } else {
                    // existing question
                    question.id = _.size(questions);
                    questions.push(question);
                }
                
                renderQuestions();
            }
            
            function removeQuestion(index) {
                questions.splice(index, 1);
                _.each(questions, function(question) {
                    if (question.id > index) {
                        question.id = question.id - 1;
                    }
                });
                
                renderQuestions();
            }
            
            function moveUp(index) {
                index = parseInt(index);
                if (index == _.size(questions) - 1) {
                    return;
                }
                
                questions[index].id = parseInt(questions[index].id) + 1;
                questions[index + 1].id = parseInt(questions[index + 1].id) - 1;
                renderQuestions();
            } 
            
            function moveDown(index) {
                index = parseInt(index);
                if (index == 0) {
                    return;
                }
                
                questions[index].id = parseInt(questions[index].id) - 1;
                questions[index - 1].id = parseInt(questions[index - 1].id) + 1;
                renderQuestions();
            } 
            
            var BaseQuestionType = {
                renderTemplate: function() {
                    return getTemplate("tmpl-" + this.type + "-question");
                },
                
                editTemplate: function() {
                    return getTemplate("tmpl-edit-details-" + this.type); 
                },
                
                parseJSON: function(json) {
                    return json;
                },
                
                toJSON: function(json) {
                    return json;
                },
                
                prerender: function(question) {
                    return question;
                },
                
                loadDetails: function(question, div) {
                    var template = this.editTemplate();
                    
                    var question = question || {};
                    div.html(template(question));
                },
                
                toQuestion: function(result) {
                    return result;
                },
                
                extend: function(obj) {
                    _.defaults(obj, BaseQuestionType);
                    _.bindAll(obj);
                    return obj;
                }
            };
            
            var TextQuestionType = BaseQuestionType.extend({
                type: "text",
                
                loadDetails: function(question, div) {
                    // do nothing
                }
            });
            
            //recording question type
            var RecordingQuestionType = BaseQuestionType.extend({
                type: "recording",
                
                loadDetails: function(question, div) {
                    var template = this.editTemplate();
                
                    var question = question || {maxlength: '1'};
                    div.html(template(question));
                },
                toQuestion: function(result) {
                    
                    var len = $("#edit-recording-maxlength").val();
                    
                    if(len=="" || len==null) {
                        len = 2;
                    }
                    var reInteger = /^\d+$/;
                    if(!reInteger.test(len)) {
                        alert("Invalid value for Maximum length");
                    } else {
                        if(parseInt(len)>4) {
                            alert("Maximum length must be less than 4 minutes");
                        } else {
                            result['maxlength'] = len;
                            return result;
                        }
                    }
                }
                
            });
            
            //audio question type
            var AudioQuestionType = BaseQuestionType.extend({
                type: "audio",
                
                loadDetails: function(question, div) {
                    file_saved=0;
                    var template = this.editTemplate();
                    recording = 0;
                    var question = question || {allowedreplays: '1'};
                    div.html(template(question));
                    
                   //creating file name       
                    var id = '${it.sheetId}';
                    var sheetName = document.getElementById("name-field").value;
                    fname = id+"/"+sheetName+"-audio-"+new Date().getTime();
                   //setupRecorder();
                   //initialization for recording
                    var appWidth = 24;
                    var appHeight = 24;
                    var flashvars = {'event_handler': 'microphone_recorder_events', 'upload_image': 'html/images/upload.png'};
                    var params = {};
                    var attributes = {'id': "recorderApp", 'name':  "recorderApp"};
                    swfobject.embedSWF("recorder.swf", "flashcontent", appWidth, appHeight, "10.1.0", "", flashvars, params, attributes);
                    
                },
                
                toQuestion: function(result) {
                    
                    var replays = $("#edit-audio-allowedreplays").val();
                    var save = 1;
                    var reInteger = /^\d+$/;
                    var msg="";
                    var lines=$("#edit-audio-audiolines").val();
                    //var lines = $("#edit-audio-audiolines").val();
                    if(file_saved==0) {
                        msg = "Please save the file before saving question.\n\n";
                        save = 0;
                    }   
                    if(replays=="" || replays==null) {
                        msg = msg+"Number of replays can not be empty.\n\n";
                        save = 0;
                    } else {
                        if(!reInteger.test(replays)) {
                            msg = msg+"Invalid value for Maximum Number of replays.";
                            save = 0;
                        } 
                    }
                    if(lines==null || lines=="") {
                        msg = msg+"Number of lines can not be empty.\n\n";
                        save = 0;
                    }
                    if(save==1) {
                        result['fname'] = fname;
                        result['host'] = location.hostname+":"+location.port;
                        result['allowedreplays'] = replays;
                        result['audiolines'] = lines;
                        result['audioinstructions'] = $("#edit-audio-audioinstructions").val();
                        return result;
                    } else {
                        alert(msg);
                    }

                }
            });
            
            var FieldQuestionType = BaseQuestionType.extend({
                type: "field",
                
                loadDetails: function(question, div) {
                    var template = this.editTemplate();
                
                    var question = question || {lines: '1'};
                    div.html(template(question));
                },
                
                toQuestion: function(result) {
                    result['lines'] = $("#edit-field-lines").val();
                    result['instructions'] = $("#edit-field-instructions").val();
                    return result;
                }
            });
            
            var MultipleQuestionType = BaseQuestionType.extend({
                type: "multiple",
                
                prerender: function(json) {
                    var choices = json['choices'];
                    
                    if (choices) {
                        json = _.clone(json);
                        json['choices'] = _.filter(choices.split('\n'), function(data) {
                            return data && data.length > 0;
                        });
                    }
                    
                    if (json['multiple']) {
                        json['multiple'] = /true/i.test(json['multiple']);
                    }
                    
                    if (json['other']) {
                        json['other'] = /true/i.test(json['other']);
                    }
                    
                    return json;
                },
                
                toQuestion: function(result) {
                    result['choices'] = $("#edit-multiple-choices").val();
                    result['other'] = $("#edit-multiple-other").is(":checked");
                    result['otherText'] = $("#edit-multiple-other-text").val();
                    result['multiple'] = $("#edit-multiple-allow-multiple").is(":checked");
                    return result;
                }
            });
            
            var QuestionType = {
                text: TextQuestionType,
                field: FieldQuestionType,
                multiple: MultipleQuestionType,
                audio : AudioQuestionType,
                recording : RecordingQuestionType
            };
            
            function questionType(type) {
                return QuestionType[type];
            }
            
            function record_start()
            {
                var status = $('#status').text();
                //alert("status : "+status);
                if(status=="Recording...")
                {
                    Recorder.record('audio', 'audio.wav');
                }
                else
                {
                    if(recording==1)
                    {
                        if(confirm("Replace recording?"))
                        {
                            Recorder.record('audio', 'audio.wav');
                        }
                    }
                    else
                    {
                        Recorder.record('audio', 'audio.wav');
                    }
                }
            }
            
        </script>
    </head>
    <body>
        
        <div id="mainForm" class="ui-widget-container ui-widget ui-corner-all">
            <h1>Design Sheet</h1>
            <div id="sheet-area">
                <div id="top-questions">
                    <label for="name-field">Sheet Name:</label>
                    <input id="name-field" class="ui-widget-content ui-corner-all" size="50" type="text" name="name" value=""/>
                    <input id="add-question" class="edit-button" value="+ Add Question" type="submit"/>
                
                    <div id="sheet-checkboxes">
                        <input class="ui-widget-content ui-corner-all" type="checkbox" id="autoOpen" value="true"/>
                        <label for="autoOpen">Visible on startup</label>
                    
                        <input class="ui-widget-content ui-corner-all" type="checkbox" id="visible" value="true"/>
                        <label for="visible">Visible in Windows menu</label>
                    </div>
                </div>
                
                <div id="sheet-questions">    
                </div>
            </div>    

            <div id="bottom-buttons">
                <input class="edit-button" id="cancel" name="action" value="Cancel" type="submit"/>
                <input class="edit-button" id="duplicate" name="action" value="Duplicate" type="submit"/>
                <input class="edit-button" id="save" name="action" value="Save" type="submit"/>
            </div>
            
        </div>
        
        <script id="tmpl-question" type="text/x-handlebars-tmpl">
            <div class="question">
                <div class="question-controls-holder">
                    <div class="question-controls">
                        <button class="edit-button">Edit</button>
                        <button class="delete-button">Delete</button>
                        <button class="up-button"></button>
                        <button class="down-button"></button>
                    </div>
                </div>
                {{> question}}
            </div>
        </script> 
    
        <script id="tmpl-text-question" type="text/x-handlebars-tmpl">
            <div class="question-content">
                {{ text }}
            </div>
        </script>
        
        <script id="tmpl-field-question" type="text/x-handlebars-tmpl">
            <div class="question-content">
                <label for="question-{{id}}-field">{{text}}</label>
                <textarea id="question-{{id}}-field" class="question-field ui-widget-content ui-corner-all" readonly="true" rows="{{lines}}">{{instructions}}</textarea>
            </div>
        </script>
        
        <script id="tmpl-audio-question" type="text/x-handlebars-tmpl">
            
            <div class="question-content">
                <label for="question-{{id}}-audio">{{text}}</label>
                <audio controls="controls">
                <%
                    java.net.InetAddress i = java.net.InetAddress.getLocalHost();
                    System.out.println(i.getHostName());
                    String ab = i.getHostName();
                %>
                    <source src="http://<%=ab%>:8080/webdav/content/groups/users/audiosheet/{{fname}}.wav" type="audio/wav" />
                Your browser does not support this audio format.
                </audio>
                <textarea id="question-{{id}}-audio" class="question-audio ui-widget-content ui-corner-all" readonly="true" rows="{{audiolines}}">{{audioinstructions}}</textarea>
                <%--<textarea id="question-{{id}}-audio" class="question-audio ui-widget-content ui-corner-all" readonly="true" rows="{{allowedreplays}}">{{allowedreplays}}</textarea>--%>
            </div>
        </script>
            
        <script id="tmpl-recording-question" type="text/x-handlebars-tmpl">
            <div class="question-content">
                <label for="question-{{id}}-recording">{{text}}</label>
                
            </div>
        </script>
        
        <script id="tmpl-multiple-question" type="text/x-handlebars-tmpl">
            <div class="question-content multiple-question-content">
                <label for="question-{{id}}-multiple">{{text}}</label>
                <form id="question-{{id}}-multiple">
                    <ul>
                    {{#choices}}
                        <li>
                        <input type={{#string-if ../multiple}}"checkbox"{{else}}"radio"{{/string-if}} name="question-{{../id}}-radio"/>
                        <label>{{this}}</label>
                    {{/choices}}
                    {{#if other}}
                        <li class="other-li">
                        <input type={{#string-if multiple}}"checkbox"{{else}}"radio"{{/string-if}}" name="question-{{id}}-radio"/>
                        <label>Other {{#if otherText}}({{otherText}}){{/if}}:</label>
                        <textarea rows="1" class="ui-widget ui-corner-all" readonly="true"/>
                    {{/if}}
                    </ul>
                </form>
            </div>
        </script>
        
        
        
        <script id="tmpl-edit-dialog" type="text/x-handlebars-tmpl">
            <div id="edit-dialog">
                <label for="edit-question-text">Question Text:</label>
                <textarea rows="3" id="edit-question-text" class="ui-widget-content ui-corner-all">{{text}}</textarea>
                
                <label for="edit-question-type">Question Type:</label>
                <select id="edit-question-type">
                    <option value="text">Text Only</option>
                    <option value="field">Short Answer</option>
                    <option value="multiple">Multiple Choice</option>
                    
                    <option value="recording">Recording</option>
                    <option value="audio">Audio</option>
                </select>
                
                <div id="edit-question-details">
                </div>
            </div>
        </script>
        
        <script id="tmpl-edit-details-field" type="text/x-handlebars-tmpl">
            <label id="edit-field-lines-label" for="edit-field-lines">Number of lines:</label>
            <textarea rows="1" cols="4" id="edit-field-lines" class="ui-widget-content ui-corner-all">{{lines}}</textarea>
            
            <label for="edit-field-instructions" class="instruction-text">If you want any instructions to appear in the text box, enter them below:</label>
            <textarea rows="1" id="edit-field-instructions" class="ui-widget-content ui-corner-all">{{instructions}}</textarea>
        </script>
        
        <script id="tmpl-edit-details-multiple" type="text/x-handlebars-tmpl">
            <div id="edit-multiple-div">
                <div id="edit-multiple-choices-div">
                    <label for="edit-multiple-choices" class="instruction-text">Put each choice on a separate line:</label>
                    <textarea rows="5" id="edit-multiple-choices" class="ui-widget-content ui-corner-all">{{#choices}}{{.}}&#10;{{/choices}}</textarea>
                </div>

                <div id="edit-multiple-other-div">
                    <input type="checkbox" id="edit-multiple-other" {{#string-if other}}checked="true"{{/string-if}}/>
                    <label for="edit-multiple-other">Add "Other" as a choice using this text:</label>
                    <textarea rows="1" id="edit-multiple-other-text" class="ui-widget-content ui-corner-all">{{otherText}}</textarea>
                </div>

                <div id="edit-multiple-allow-multiple-div">
                    <input type="checkbox" id="edit-multiple-allow-multiple" {{#string-if multiple}}checked="true"{{/string-if}}/>
                    <label for="edit-multiple-allow-multiple">Allow multiple answers</label>
                </div>
            </div>
        </script>
            
        <script id="tmpl-edit-details-audio" type="text/x-handlebars-tmpl">
            
            <%-- Recorder Starts --%>
            <div id="status">
            Recorder Status...
            </div>
            
            <div id="control_panel">
            <a id="record_button" onclick="record_start();" href="javascript:void(0);" title="Record"><img src="html/images/record.png" width="24" height="24" alt="Record"/></a>
            <span id="save_button">
                <span id="flashcontent">
                <p>Your browser must have JavaScript enabled and the Adobe Flash Player installed.</p>
                </span>
                
            </span>
            <a id="play_button" style="display:none;" onclick="Recorder.playBack('audio');" href="javascript:void(0);" title="Play"><img src="html/images/play.png" width="24" height="24" alt="Play"/></a>
            </div>
            <div id="save_instruction" style="display: none;font-style: italic;font-size: 11px">
                    Click the Save button (disk icon) when you are satisfied with your recording.
                </div>
            <div id="upload_status" style="display: none">
            </div>

            <div style="display: none">Activity Level: <span id="activity_level"></span></div>

            <form id="uploadForm" name="uploadForm" action="saveaudiofile">
                
                <input id="upload_file" name="upload_file[parent_id]"  type="hidden">
                
            </form>
            
            <%--
            <div id="wami" style="margin-left: -1px"></div>--%>
           <%-- Recorder Ends --%> 
           <label id="edit-audio-allowedreplays-label" for="edit-audio-allowedreplays"
                   style="float: left;fontt-weight: bold;margin-top: 3px;margin-bottom: 4px;">Number of replays allowed:</label>
            <textarea rows="1" cols="4" id="edit-audio-allowedreplays" class="ui-widget-content ui-corner-all">{{allowedreplays}}</textarea>
            <br />
            <label id="edit-audio-audiolines-label" for="edit-field-lines"
                   style="float: left;fontt-weight: bold;margin-top: 3px;margin-bottom: 4px;">Number of lines:</label>
            <textarea rows="1" cols="4" id="edit-audio-audiolines" class="ui-widget-content ui-corner-all">{{audiolines}}</textarea>
            <br />
            <label for="edit-audio-audioinstructions" class="audioinstructions-text">If you want any instructions to appear in the text box, enter them below:</label>
            <textarea rows="1" id="edit-audio-audioinstructions" class="ui-widget-content ui-corner-all">{{audioinstructions}}</textarea>
        </script>
           
        <script id="tmpl-edit-details-recording" type="text/x-handlebars-tmpl">
            <label id="edit-recording-maxlength-label" for="edit-recording-maxlength">Maximum Length : </label>
            <textarea rows="1" cols="2" id="edit-recording-maxlength" class="ui-widget-content ui-corner-all">{{maxlength}}</textarea>
            <label id="edit-recording-maxlength-label0">minutes</label><br /><br />
            <label id="edit-recording-maxlength-label1">An audio recorder will appear on the sheet.</label>
        </script>
            
    </body>
</html>
