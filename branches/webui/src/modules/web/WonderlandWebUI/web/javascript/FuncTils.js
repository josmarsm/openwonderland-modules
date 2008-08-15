
/**
 * Function used to set a cookie
 * credit: http://www.elated.com/articles/javascript-and-cookies/
 */
function set_cookie(cookieName, value)
{
  var cookie_string = cookieName + "=" + escape ( value );
  
  var tmp = new Date( );
  var expires = new Date( parseInt(tmp.getFullYear()) + parseInt("1") , tmp.getMonth(), tmp.getDay());
  cookie_string += "; expires=" + expires.toGMTString();

  document.cookie = cookie_string;
}

/**
 * Function used to get a cookie
 * credit: http://www.elated.com/articles/javascript-and-cookies/
 */
function get_cookie( cookie_name )
{
  var results = document.cookie.match ( '(^|;) ?' + cookie_name + '=([^;]*)(;|$)' );

  if ( results ) {
    return ( unescape (results[2]) );
  }
  else
    return null;
}

/*
 * Get the username that was entered on the login page, and stored as a cookie.
 */
function extractUserName() {
  var cookie = get_cookie("wonderland.username");
  userName = cookie;
}

function initLocation() {
  var innerDiv = document.getElementById("innerDiv");
  var avatars;
  var viewOffsetX; //value to adjust left value of view window
  var viewoffsetZ; //value to adjust top value of view window

  var outerDiv = document.getElementById("outerDiv");
  var viewWidth = parseInt(outerDiv.getWidth()) / 2; //value in px
  var viewHeight = parseInt(outerDiv.getHeight()) / 2; //value in px

  if( document.getElementById("avatars") == null ) {
    avatars = document.createElement("div");  
    avatars.setAttribute("id", "avatars");
  } else {
    avatars = document.getElementById("avatars");
  }
  
  var user = document.createElement("div");
  user.setAttribute("id", userName);
    
  var user_avatar = document.createElement("img");
  user_avatar.style.position = "absolute";
  
  user_avatar.style.left = viewWidth + "px";
  user_avatar.style.top = viewHeight + "px";

  //adjust the position based on the mapping conversion
  //this will apply to the position of the upper left hand corner
  //so i will need to subtract 1/2 width, and 1/2 height to get 
  //the correct positioning
  viewOffsetX = -1*(parseInt(getVworldToWebX(positionX)) - (parseInt(viewWidth) - parseInt(30)));
  viewoffsetZ = -1*(parseInt(getVworldToWebY(positionZ)) - (parseInt(viewHeight) - parseInt(30)));

  //adjust the view window
  innerDiv.style.left = viewOffsetX + "px";
  innerDiv.style.top = viewoffsetZ + "px";  
  
  user_avatar.style.zIndex = 1;
  user_avatar.setAttribute("id", userName+"_avatar");
  user_avatar.setAttribute("src",avatarIcon);
    
  user.appendChild(user_avatar);
 
  var avatar_info = document.createElement("div");
  avatar_info.setAttribute("id", userName + "_avatar_info");
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
  avatar_info_popup.setAttribute("id", userName + "_avatar_info_popup");
  
  var avatar_info_popup_name = document.createElement("li");
  
  var popup_name = document.createElement("a");
  
  var popup_name_data = document.createTextNode(userName);
  
  popup_name.appendChild(popup_name_data);
  
  avatar_info_popup_name.appendChild(popup_name);
  
  avatar_info_popup.appendChild(avatar_info_popup_name);
  
  avatar_info.appendChild(avatar_info_popup);

  user.appendChild(avatar_info);
  
  var avatar_position = document.createElement("div");
  avatar_position.setAttribute("id", userName + "_position");
  
  var avatar_position_x = document.createElement("x");
  var avatar_position_x_data = document.createTextNode(positionX);
  
  avatar_position_x.appendChild(avatar_position_x_data);
  avatar_position.appendChild(avatar_position_x);

  var avatar_position_y = document.createElement("z");
  var avatar_position_y_data = document.createTextNode(positionZ);

  avatar_position_y.appendChild(avatar_position_y_data);
  avatar_position.appendChild(avatar_position_y);

  user.appendChild(avatar_position);

  //here is where i add the user information
  avatars.appendChild(user);
  innerDiv.appendChild(avatars);
}

