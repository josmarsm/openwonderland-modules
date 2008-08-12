<%@ page contentType="text/html; charset=ISO-8859-5" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>Window To Wonderland</title>
    
    <% if( ((String)session.getAttribute("browserType")).equals("desktop") ) { %>
      <link rel="stylesheet" type="text/css" href="css/view.css">
      <link rel="stylesheet" type="text/css" href="css/sdmenu.css">
    <% } else { %>
      <link rel="stylesheet" type="text/css" href="css/view.mobi.css">
      <link rel="stylesheet" type="text/css" href="css/sdmenu.mobi.css">
    <% } %>
    
    <!-- 
      External scripts included after this page script, to prevent the first js from executing functions that are defined here 
      and any others that I create should also be declared before the dwr scripts
    -->
    
    <% if( ((String)session.getAttribute("browserType")).equals("desktop") ) { %>
      <script type="text/javascript" src="javascript/View.js"></script>
      <script type="text/javascript" src="javascript/Window.js"></script>
    <% } else { %>
      <script type="text/javascript" src="javascript/View.mobi.js"></script>
      <script type="text/javascript" src="javascript/Window.mobi.js"></script>
    <% } %>
    
    <script type="text/javascript" src="javascript/DSClient.js"></script>
    
    <script type="text/javascript" src="javascript/browserdetect_lite.js"></script>
    <script type="text/javascript" src="javascript/opacity.js"></script>

    <script type="text/javascript" src="javascript/prototype.js"></script>
    <script type="text/javascript" src="javascript/scriptaculous.js"></script>
 
    <!-- this next section will have to include all class names that are found in dwr.xml -->
    <script type="text/javascript" src="/WonderlandWebUI/dwr/interface/ClientConnect.js"></script>
    <script type="text/javascript" src="/WonderlandWebUI/dwr/interface/Servlet.js"></script>
      
    <script type="text/javascript" src="/WonderlandWebUI/dwr/engine.js"></script>
    <script type="text/javascript" src="/WonderlandWebUI/dwr/util.js"></script>

  </head>
  <body onload="javascript:viewInit()">
    <% if( ((String)session.getAttribute("browserType")).equals("desktop") ) { %>
      <div id="menu">
        <ul class="menu">
          <li><strong>Zoom</strong>
            <ul>
              <li><a onclick="zoomIn()">Zoom In</a></li>
              <li><a onclick="zoomOut()">Zoom Out</a></li>
            </ul>
          </li>
          <li><strong>Placemarks</strong>
            <ul>
              <li><a onclick="javascript:goToLocation(50, 0, 50)">Starting Location</a></li>
              <li><a onclick="javascript:goToLocation(53.827713, 0, 88.33005)">MPK20 Conference Room</a></li>
              <li><a onclick="javascript:goToLocation(51.64006, 0, 13.052788)">MPK20 Team Room</a></li>
              <li><a onclick="javascript:toggleBox('divGoTo',1)">Go To Location</a></li>
            </ul>
          </li>
          <li><strong>Call World</strong>
            <ul>
              <li><a onclick="javascript:toggleBox('divSoftphone',1)">Softphone</a></li>
            </ul>
          </li>
          <li><strong>Users Online</strong>
            <ul>
              <li><a onclick="javascript:toggleBox('divUsersOnline',1)">Display</a></li>
            </ul>
          </li>
        </ul>
      </div>
      <div id="outerDiv" >
        <!-- Arrow icons from http://www.segd.nl/signage-symbols/free-vector-arrows.htm -->
        <div class="arrow" onclick="javascript:moveUp()" style="position: absolute; bottom: 41px; right: 25px; z-index: 2">
          <img src="resources/images/move_forward.png" />
        </div>
        <div class="arrow" onclick="javascript:moveDown()" style="position: absolute; bottom: 3px; right: 25px; z-index: 2">
          <img src="resources/images/move_back.png"/>
        </div>
        <div class="arrow" onclick="javascript:moveLeft()" style="position: absolute; bottom: 22px; right: 45px; z-index: 2" >
          <img src="resources/images/move_left.png" />
        </div>
        <div class="arrow" onclick="javascript:moveRight()" style="position: absolute; bottom: 22px; right: 5px; z-index: 2">
          <img src="resources/images/move_right.png" />
        </div>
      <div id="innerDiv" style="z-index: 0" onClick="mapClick"> </div>
      </div>
      <div id="chat">
        <form id="chatbox" action="javascript:sendMessage()">
          <div id="chat_messages"></div>
          <div id="new_message">
            <input id="new_chat_message" type="text" name="new_message" size="80"/>
          </div>
          <div id="actions">
            <input type="reset" value="Clear" name="Clear" />
            <input type="button" value="Send" onClick="javascript:sendMessage()" />
          </div>
        </form>
      </div>
      <!-- hidden divs that are used as popups in the web page -->
      <div id="dialogs">
        <div id="divUsersOnline" class="popup" >
          <div class="close">
            <a onclick="javascript:toggleBox('divUsersOnline',0)">Close</a>
          </div>
          <table>
            <thead>
              <tr><th>User</th><th>Goto</th></tr>
            </thead>
            <tbody id="users_online"></tbody>
          </table>
        </div>
        <div id="divServerStats" class="popup" >
          <div class="close">
            <a onclick="javascript:toggleBox('divServerStats',0)">Close</a>
          </div>
          <table>
            <thead></thead>
            <tbody id='server_stats'></tbody> 
          </table>
        </div>
        <div id="divGoTo" class="popup">
          <div class="close">
            <a onclick="javascript:toggleBox('divGoTo',0)">Close</a>
          </div>
          <form id="goto">
            <div id="x">
              X: <input type="text" name="posX" ></input>
            </div>
            <div id="y"> 
              Y: <input type="text" name="posY" ></input>
            </div>
            <div id="z"> 
              Z: <input type="text" name="posZ" ></input>
            </div>             
            <input type="button" value="Go" onclick="javascript:goToLocation()"></input>        
          </form>
        </div>
        <div id="divSoftphone" class="popup">
          <div class="close">
            <a onclick="javascript:toggleBox('divSoftphone',0)">Close</a>
          </div>
          <form id="softphone">
            Enter number to dial into
            <div id="phone_Ext">
              phone ext: <input type="text"  name="phoneExt" ></input>
            </div>
            <div id="phone_Num"> 
              phone number: <input type="text"  name="phoneNum" ></input>
            </div>
            <input type="button" value="Go" onclick="javascript:sendCallMe()"></input>        
          </form>
        </div>
      </div>
    <% } else { %>
      <div id="outerDiv" >
        <div id="mobilemenu">
          <ul class="menu">
            <li><strong>Zoom</strong>
              <ul>
                <li><a onclick="javascript:zoomIn()">Zoom In</a></li>
                <li><a onclick="javascript:zoomOut()">Zoom Out</a></li>
              </ul>
            </li>
            <li><strong>Placemarks</strong>
              <ul>
                <li><a onclick="javascript:goToLocation(50, 0, 50)">Starting Location</a></li>
                <li><a onclick="javascript:goToLocation(53.827713, 0, 88.33005)">MPK20 Conference Room</a></li>
                <li><a onclick="javascript:goToLocation(51.64006, 0, 13.052788)">MPK20 Team Room</a></li>
                <li><a onclick="javascript:toggleBox('divGoTo',1)">Go To Location</a></li>
              </ul>
            </li>
            <li><strong>Call World</strong>
            <ul>
              <li><a onclick="javascript:toggleBox('divSoftphone',1)">Softphone</a></li>
            </ul>
            </li>
            <li><strong>Users Online</strong>
              <ul>
                <li><a onclick="javascript:toggleBox('divUsersOnline',1)">Display</a></li>
              </ul>
            </li>
            <li><strong>Chat Messages</strong>
              <ul>
                <li><a onclick="javascript:toggleBox('divChat',1)">Chatbox</a></li>
              </ul>
            </li>
          </ul>
        </div>
        <!-- Arrow icons from http://www.segd.nl/signage-symbols/free-vector-arrows.htm -->
        <div class="arrow" onclick="javascript:moveUp()" style="position: absolute; bottom: 41px; right: 25px; z-index: 2">
          <img src="resources/images/move_forward.png" />
        </div>
        <div class="arrow" onclick="javascript:moveDown()" style="position: absolute; bottom: 3px; right: 25px; z-index: 2">
          <img src="resources/images/move_back.png"/>
        </div>
        <div class="arrow" onclick="javascript:moveLeft()" style="position: absolute; bottom: 22px; right: 45px; z-index: 2" >
          <img src="resources/images/move_left.png" />
        </div>
        <div class="arrow" onclick="javascript:moveRight()" style="position: absolute; bottom: 22px; right: 5px; z-index: 2">
          <img src="resources/images/move_right.png" />
        </div>
        <div id="innerDiv" style="z-index: 0"> </div>
      </div>
      <!-- hidden divs that are used as popups in the web page -->
      <div id="dialogs">
        <div id="divUsersOnline" class="popup" >
          <div class="close">
            <a onclick="javascript:toggleBox('divUsersOnline',0)">Close</a>
          </div>
          <table>
            <thead>
              <tr><th>User</th><th>Goto</th></tr>
            </thead>
            <tbody id="users_online"></tbody>
          </table>
        </div>
        <div id="divServerStats" class="popup" >
          <div class="close">
            <a onclick="javascript:toggleBox('divServerStats',0)">Close</a>
          </div>
          <table>
            <thead></thead>
            <tbody id='server_stats'></tbody> 
          </table>
        </div>
        <div id="divGoTo" class="popup" >
          <div class="close">
            <a onclick="javascript:toggleBox('divGoTo',0)">Close</a>
          </div>
          <form id="goto">
            <div id="x">
              X: <input type="text" name="posX" ></input>
            </div>
            <div id="y"> 
              Y: <input type="text" name="posY" ></input>
            </div>
            <div id="z"> 
              Z: <input type="text" name="posZ" ></input>
            </div>            
            <input type="button" value="Go" onclick="javascript:goToLocation()"></input>        
          </form>
        </div>
        <div id="divChat" class="popup">
          <div class="close">
            <a onclick="javascript:toggleBox('divChat',0)">Close</a>
          </div>
          <form id="chatbox" action="javascript:sendMessage()">
            <div id="chat_messages"></div>
            <div id="new_message">
              <input type="text" name="new_message" size="50"/>
            </div>
            <div id="actions">
              <input type="reset" value="Clear" name="Clear" />
              <input type="button" value="Send" onClick="javascript:sendMessage()" />
            </div>
          </form>
        </div>
        <div id="divSoftphone" class="popup">
          <div class="close">
            <a onclick="javascript:toggleBox('divSoftphone',0)">Close</a>
          </div>
          <form id="softphone">
            Enter number to dial into
            <div id="phone_Ext">
              phone ext: <input type="text"  name="phoneExt" ></input>
            </div>
            <div id="phone_Num"> 
              phone number: <input type="text"  name="phoneNum" ></input>
            </div>
            <input type="button" value="Go" onclick="javascript:sendCallMe()"></input>        
          </form>
        </div>
      </div>
    <% } %>  
  </body>
</html>
