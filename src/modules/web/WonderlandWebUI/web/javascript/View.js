var checkImg = new Image();
var avatarIcon = "resources/images/person.png";
var positionX = 0;
var positionZ = 0;
var userName;
var initPos = true;

/*
 * Function called on loading of view.jsp
 */
function viewInit() {
  dsClient.init();
  Servlet.setWebContext();

  extractUserName();

  // wire up the mouse listeners to do dragging
  var outerDiv = document.getElementById("outerDiv");
  outerDiv.onmousedown = startMove;
  outerDiv.onmousemove = processMove;
  outerDiv.onmouseup = stopMove;

  //necessary to enable dragging on IE
  outerDiv.ondragstart = function() { return false; }
  
  initTileMap();
  
  if( browser.isIE ){
      setTimeout('checkTiles()',1000);
      setTimeout('initLocation()',2000);
  } else {
      window.setTimeout('checkTiles()',1000);
      window.setTimeout('initLocation()',2000);
  }
}

/*
 *Location for the avatar is created, and set to their position in world.
 */
function cbCreateUserLocation(nametag, posX, posY) {
  if( userName == nametag && initPos ) {
      return;
  }
  
  var innerDiv = document.getElementById("innerDiv");
  var avatars;
  if( document.getElementById("avatars") == null ) {
    avatars = document.createElement("div");  
    avatars.setAttribute("id", "avatars");
  } else {
    avatars = document.getElementById("avatars");
  }

  var user = document.createElement("div");
  user.setAttribute("id", nametag);

  var user_avatar = document.createElement("img");
  user_avatar.style.position = "absolute";
  
  //adjust the position based on the mapping conversion 
  user_avatar.style.left = getVworldToWebX(posX) + "px";
  user_avatar.style.top = getVworldToWebY(posY) + "px";  
  
  user_avatar.style.zIndex = 1;
  user_avatar.setAttribute("id", nametag+"_avatar");
  user_avatar.setAttribute("src",avatarIcon);
  
  user.appendChild(user_avatar);
 
 //a box that floats above the avatars
  var avatar_info = document.createElement("div");
  avatar_info.setAttribute("id", nametag + "_avatar_info");
  avatar_info.setAttribute("class", "info");
  var box_width = 50;
  var box_height = 30;
  avatar_info.style.width = box_width + "px"; 
  avatar_info.style.height = box_height + "px";
  var winX = parseInt(user_avatar.style.left);
  var winY = parseInt(user_avatar.style.top) + parseInt("-3") + parseInt("-"+box_height);
  avatar_info.style.left = winX + "px";
  avatar_info.style.top = winY +"px";
  avatar_info.style.zIndex = 1;
  
  //in the future more information will be added to the user tag
  var avatar_info_popup = document.createElement("ul");
  avatar_info_popup.setAttribute("id", nametag + "_avatar_info_popup");
  
  var avatar_info_popup_name = document.createElement("li");
  
  var popup_name = document.createElement("a");
  
  var popup_name_data = document.createTextNode(nametag);
  
  popup_name.appendChild(popup_name_data);
  
  avatar_info_popup_name.appendChild(popup_name);
  
  avatar_info_popup.appendChild(avatar_info_popup_name);
  
  avatar_info.appendChild(avatar_info_popup);

  user.appendChild(avatar_info);

  //this stores the current position of the avatar in the map so that
  //it can be reloaded on zoom in, and zoom out
  var avatar_position = document.createElement("div");
  avatar_position.setAttribute("id", nametag + "_position");
  
  var avatar_position_x = document.createElement("x");
  var avatar_position_x_data = document.createTextNode(posX);
  
  avatar_position_x.appendChild(avatar_position_x_data);
  avatar_position.appendChild(avatar_position_x);

  var avatar_position_y = document.createElement("z");
  var avatar_position_y_data = document.createTextNode(posY);

  avatar_position_y.appendChild(avatar_position_y_data);
  avatar_position.appendChild(avatar_position_y);

  user.appendChild(avatar_position);

  //here is where i add the user information
  avatars.appendChild(user);
  innerDiv.appendChild(avatars);
}

/* 
 * This function is used to track the user's movements, as seen by the wonderland server,
 * and forwarded to the web client
 */
function cbTrackUser(nametag, posX, posY) {
  if( userName == nametag ) { initPos = false; }
    
  //first thing is to remove the user pin from the map
  var innerDiv = document.getElementById("innerDiv");
  var avatars = document.getElementById("avatars");
  var olduser = document.getElementById(nametag);

  //delete myself from the avatar list
  avatars.removeChild(olduser);

  var user = document.createElement("div");
  user.setAttribute("id", nametag);

  // now add the image to the new location
  var user_avatar = document.createElement("img");
  user_avatar.style.position = "absolute";
  
  user_avatar.style.zIndex = 1;
  user_avatar.setAttribute("id", nametag+"_avatar");
  user_avatar.setAttribute("src",avatarIcon);

  checkImg.src = avatarIcon;
  
  //adjust the position based on the mapping conversion
  user_avatar.style.left = getVworldToWebX(posX) + "px";
  user_avatar.style.top = getVworldToWebY(posY) + "px";  
  
  user.appendChild(user_avatar);
 
 //a box that floats above the avatars
  var avatar_info = document.createElement("div");
  avatar_info.setAttribute("id", nametag + "_avatar_info");
  avatar_info.setAttribute("class", "info");
  var box_width = 50;
  var box_height = 30;
  avatar_info.style.width = box_width + "px"; 
  avatar_info.style.height = box_height + "px";
  var winX = parseInt(user_avatar.style.left);
  var winY = parseInt(user_avatar.style.top) + parseInt("-10") + parseInt("-"+box_height);
  avatar_info.style.left = winX + "px";
  avatar_info.style.top = winY + "px";
  avatar_info.style.zIndex = 1;
  
  //in the future more information will be added to the user tag
  var avatar_info_popup = document.createElement("ul");
  avatar_info_popup.setAttribute("id", nametag + "_avatar_info_popup");
  
  var avatar_info_popup_name = document.createElement("li");

  var popup_name = document.createElement("a");
  
  var popup_name_data = document.createTextNode(nametag);
  
  popup_name.appendChild(popup_name_data);
  
  avatar_info_popup_name.appendChild(popup_name);

  avatar_info_popup.appendChild(avatar_info_popup_name);
  
  avatar_info.appendChild(avatar_info_popup);
  
  user.appendChild(avatar_info);

  //this stores the current position of the avatar in the map so that
  //it can be reloaded on zoom in, and zoom out
  var avatar_position = document.createElement("div");
  avatar_position.setAttribute("id", nametag + "_position");
  
  var avatar_position_x = document.createElement("x");
  var avatar_position_x_data = document.createTextNode(posX);
  
  avatar_position_x.appendChild(avatar_position_x_data);
  avatar_position.appendChild(avatar_position_x);

  var avatar_position_y = document.createElement("z");
  var avatar_position_y_data = document.createTextNode(posY);

  avatar_position_y.appendChild(avatar_position_y_data);
  avatar_position.appendChild(avatar_position_y);

  user.appendChild(avatar_position);
  
  avatars.appendChild(user);
  
  innerDiv.appendChild(avatars);
}

/**
 * This function will convert the Vworld X position to the equivalent web X position
 * in pixels.
 */
function getVworldToWebX(posX) {
  var tmp = (posX * offsetX) + (checkImg.width*3);
  
  return tmp;
}

/**
 * This function will convert the Vworld Z position to the equivalent web Y position
 * in pixels.
 */
function getVworldToWebY(posY) {
  var tmp = (posY * offsetZ) + (checkImg.height*1.5);
  
  return tmp;
}

function goToLocation() {  
  Servlet.goToLocation(dwr.util.getValue("posX"),dwr.util.getValue("posY"),dwr.util.getValue("posZ"));
}

function goToLocation(posX, posY, posZ) {  
  Servlet.goToLocation(posX, posY, posZ);
}

/*
 *Callback function that displays data message from user 'name'.
 */
function cbChatUpdate(name, data) {
  var chat = document.getElementById('chat_messages');
  var temp;
  temp = '[' + name + ']: ' + data + '<br />';
  temp += chat.innerHTML;
  chat.innerHTML = temp;
}

function cbConnectionFailure() {
  alert("Connection to server was Lost!!!")
}  
  
/*
 * This sends the chat message to the servlet.
 */
function sendMessage() {
  var message = dwr.util.getValue('new_chat_message');
  if( message.length > 0) {
    Servlet.sendChat(message); 
  }
  dwr.util.setValue("new_chat_message","");
}

//web code
function toggleBox(divId, iState) { // 1 visible, 0 hidden
  if(document.layers) { //NN4+
     document.layers[divId].visibility = iState ? "show" : "hide";
  }
  else if(document.getElementById) { //gecko(NN6) + IE 5+
      var obj = document.getElementById(divId);
      obj.style.visibility = iState ? "visible" : "hidden";
  }
  else if(document.all)	{ // IE 4
      document.all[divId].style.visibility = iState ? "visible" : "hidden";
  }
}

function getUsersOnline() {
  Servlet.usersOnline(cbUsersOnline);
}

function cbUsersOnline(data) {
  dwr.util.removeAllRows("users_online");
  dwr.util.addRows("users_online",data,usersOnlineTable, { escapeHtml:false });
} 

var usersOnlineTable = [
                        function(username) { return username; },
                        function(username) { return "<a onclick=\"Servlet.goToUser('"+username+"');\"> Goto </a>"; }
                      ];
                      
                      
function getServerStats() {
  Servlet.serverStats(cbServerStats);
}
  
function cbServerStats(data) { 
  var server_stats = document.getElementById('server_stats');
  server_stats.innerHTML = "<tr><td> Server Ver. </td><td>" + data[0] + "</td></tr>"; 
  server_stats.innerHTML += "<tr><td> Proto Ver. </td><td>" + data[1] + "</td></tr>"; 
  server_stats.innerHTML += "<tr><td> Uptime </td><td>" + data[2] + "</td></tr>"; 
}

/* 
 * Code to work on the right-click goTo this location option
 */
function mapClick() {
  if(browser.isIE) {
    document.onmousedown=IEClick;
  } else {
    document.captureEvents(Event.MOUSEDOWN);
    document.addEventListener('mousedown', nonIEClick, true);    
  }
  return false;
}

function nonIEClick(e){
  if(e.which == 3) {
    alert('right');
  }
  return false;
}

function IEClick(e){
  if(event.button == 2) {
    alert('right');
  }
  return false;
} 

/*
 * Function to request a call to be placed to the web user, from wonderland, and the extension/number
 * entered.
 */
function sendCallMe() {

  var message = dwr.util.getValue('phoneExt');
    
  if( message.length > 0) {
    Servlet.sendCallMe(message); 
    
  } else {
  
    message = dwr.util.getValue('phoneNum');

    if( message.length > 0) {
      Servlet.sendCallMe(message); 
    }
  }
}
