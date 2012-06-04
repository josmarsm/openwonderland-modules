<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />

        <script src="/wonderland-web-front/javascript/json2.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery-ui.min.js" type="text/javascript"></script>      
    
        <style type="text/css">
            #container {
                max-width: 600px;
            }
            
            #status {
                line-height: 48px;
            }
            
            #description {
                margin-top: 10px;
                margin-bottom: 15px;
                display: block;
            }
            
            #optimize {
                float: right;
            }
        </style>
        
        <script type="text/javascript">   
            var interval;
            
            $(function() {
                $("#optimize").button().click(function() {
                    $.post("resources/start").success(function() {
                        $("#progressbar").progressbar("value", 0);
                        $("#status").val("Preparing...");
                        $("#optimize").button("disable");
                    });
                    
                    return false;
                });
                
                $("#progressbar").progressbar({disabled: true});
                
                update();
                interval = setInterval(function() {
                    update();
                }, 1000);
            });
            
            function update() {
                $.getJSON("resources").success(function(data) {
                    var working = (data.status == "PREPARING" ||
                                   data.status == "WORKING");
                    $("#progressbar").progressbar("option", "disabled", !working);
                    $("#optimize").button("option", "disabled", working);
                    
                    switch (data.status) {
                        case "PREPARING":
                            $("#status").html("Preparing...");
                            break;
                        case "WORKING":
                           var progress = parseFloat(data.progress);
                           $("#progressbar").progressbar("value", progress * 100);
                           
                           var status = $("#status").html();
                           if (status == "Working") {
                               status = "Working...";
                           } else {
                               status = "Working";
                           }
                           
                           $("#status").html(status);
                           break;
                        case "COMPLETE":
                        case "ERROR":
                           $("#status").html("Optimization complete");
                           break;
                    }
                });
            }
        </script>
    </head>
<body>

    <h3>Model Optimization</h3>
    <div id="container">
        <span id="description">The model optimizer examines all models loaded by the system and 
        attempts to speed up their loading time in Wonderland. Model
        optimization may take several minutes to complete.</span>
        
        <div id="progressbar"></div>
        <span id="status">Press "Optimize" to begin</span>
        <button id="optimize">Optimize</button>
    </div>
</body>
