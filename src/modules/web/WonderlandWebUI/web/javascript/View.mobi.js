var checkImg = new Image();
var avatarIcon = "resources/images/person.png";
var positionX = 0;
var positionZ = 0;
var userName;
var initPos = true;

function viewInit() {
  dsClient.init();
  Servlet.setWebContext();
  
  extractUserName();
  
  initTileMap();
  
  //this is used to allow for updating of information, and positioning of the
  //avatars
  if( browser.isIE ) {
      setTimeout('checkTiles()',1000);
      setTimeout('initLocation()',2000);      
  }
  else {
      window.setTimeout('checkTiles()',1000);
      window.setTimeout('initLocation()',2000);      
  }
}
/* This will differ from the desktop version, since the handhelds, mobiles,
 * are assumed to not have click, and drag. The window will move as the 
 * avatar changes position, keeping the avatar centered, and moving everything 
 * else around it.
 */
//pass in the position of the map i should be at, and then re-center the map and avatar to this position
function cbCreateUserLocation(nametag, posX, posY) {
  if( userName == nametag && initPos) {
      return;
  }
  
  var innerDiv = document.getElementById("innerDiv");
  var avatars;
  var viewOffsetX; //value to adjust left value of view window
  var viewoffsetZ; //value to adjust top value of view window
  var viewWidth; //value in px
  var viewHeight; //value in px
  
  // This is done only for the users avatar, no need to make everyone's centered
  if( userName == nametag ) {
    //centers the avatar based on the size of the window
    viewWidth = parseInt(outerDiv.getWidth()) / 2; //value in px
    viewHeight = parseInt(outerDiv.getHeight()) / 2; //value in px
  }

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
  
  if( userName == nametag ) {
    user_avatar.style.left = viewWidth + "px";
    user_avatar.style.top = viewHeight + "px";

    //adjust the position based on the mapping conversion
    //this will apply to the position of the upper left hand corner
    //so i will need to subtract 1/2 width, and 1/2 height to get 
    //the correct positioning
    viewOffsetX = -1*(parseInt(getVworldToWebX(posX)) - (parseInt(viewWidth) - parseInt(30)));
    viewoffsetZ = -1*(parseInt(getVworldToWebY(posY)) - (parseInt(viewHeight) - parseInt(30)));

    //adjust the view window
    innerDiv.style.left = viewOffsetX + "px";
    innerDiv.style.top = viewoffsetZ + "px";  
  } else {
    //adjust the position based on the mapping conversion
    user_avatar.style.left = getVworldToWebX(posX) + "px";
    user_avatar.style.top = getVworldToWebY(posY) + "px";  
  }
  
  user_avatar.style.zIndex = 1;
  user_avatar.setAttribute("id", nametag+"_avatar");
  user_avatar.setAttribute("src",avatarIcon);
    
  user.appendChild(user_avatar);
 
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
  
  //add a table of various stats about user
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
  
  if( userName == nametag ) {
      Servlet.goToLocation(posX,0,posY);
  }
}

/* This will differ from the desktop version, since the handhelds, mobiles,
 * are assumed to not have click, and drag. The window will move as the 
 * avatar changes position, keeping the avatar centered, and moving everything 
 * else around it.
 * This will be called on a per-user basis 
 */
function cbTrackUser(nametag, posX, posY) {
  var viewOffsetX; //value to adjust left value of view window
  var viewoffsetZ; //value to adjust top value of view window
  var viewWidth; //value in px
  var viewHeight; //value in px
  
  //first thing is to remove the user pin from the map
  var innerDiv = document.getElementById("innerDiv");
  var avatars = document.getElementById("avatars");
  var olduser = document.getElementById(nametag);

  if( userName == nametag ) {
    //centers the avatar based on the size of the window
    viewWidth = parseInt(outerDiv.getWidth()) / 2; //value in px
    viewHeight = parseInt(outerDiv.getHeight()) / 2; //value in px
  }
  
  avatars.removeChild(olduser);
  
  var user = document.createElement("div");
  user.setAttribute("id", nametag);

  // now add the image to the new location
  var user_avatar = document.createElement("img");
  user_avatar.style.position = "absolute";

  if( userName == nametag ) {
    user_avatar.style.left = viewWidth + "px";
    user_avatar.style.top = viewHeight + "px";

    //adjust the position based on the mapping conversion
    //this will apply to the position of the upper left hand corner
    //so i will need to subtract 1/2 width, and 1/2 height to get 
    //the correct positioning
    viewOffsetX = -1*(parseInt(getVworldToWebX(posX)) - (parseInt(viewWidth) - parseInt(30)));
    viewoffsetZ = -1*(parseInt(getVworldToWebY(posY)) - (parseInt(viewHeight) - parseInt(30)));

    //adjust the view window
    innerDiv.style.left = viewOffsetX + "px";
    innerDiv.style.top = viewoffsetZ + "px";  
  } else {
    //adjust the position based on the mapping conversion
    user_avatar.style.left = getVworldToWebX(posX) + "px";
    user_avatar.style.top = getVworldToWebY(posY) + "px";  
  }
  
  user_avatar.style.zIndex = 1;
  user_avatar.setAttribute("id", nametag+"_avatar");
  user_avatar.setAttribute("src",avatarIcon);
  
  checkImg.src = avatarIcon;
  
  //adjust the position based on the mapping conversion
  user_avatar.style.left = getVworldToWebX(posX) + "px";
  user_avatar.style.top = getVworldToWebY(posY) + "px";  
  
  user.appendChild(user_avatar);
 
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
  var message = dwr.util.getValue('new_message_txt');
  if( message.length > 0) {
    Servlet.sendChat(message); 
  }
  dwr.util.setValue("new_message_txt","");
}

//web code to display and hide menus
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

//web code
function toggleMenu(divId) { // 1 visible, 0 hidden
  if(document.layers) { //NN4+
    var check = document.layers[divId].style.display;
    if( check == "none") {
      document.layers[divId].style.display = "block";
    } else {
      document.layers[divId].style.display = "none";    
    }
  } else if(document.getElementById) { //gecko(NN6) + IE 5+
    var obj = document.getElementById(divId);
    var check = obj.style.display;
    if( check == "none"){
        obj.style.display = "block";
    } else {
        obj.style.display = "none";
    }
  }
  else if(document.all)	{ // IE 4
    var check = document.all[divId].display;
    if( check == "none") {
      document.all[divId].style.display = "block";
    } else {
      document.all[divId].style.display = "none";    
    }  
  }
}

