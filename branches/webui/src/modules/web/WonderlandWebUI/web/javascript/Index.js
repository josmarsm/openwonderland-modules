
var fillInUsed = false;

function indexInit() {
  document.getElementById('username').focus();
  dsClient.init();
}

function indexLogin() {
  var username = $("username").value;
  var password = $("password").value;
  var servername = $("servername").value;
  var portnum = $("portnum").value;
                          
  dwr.util.setValue("msg3","Please wait ...");
  
  //check to see if i need to add the login information to my cookie
  if( !fillInUsed ) {
    var cookie = get_cookie("wonderland.serverlist");
    window.document.write(cookie);
    set_cookie(username + ":" + servername + ":" + portnum + ",");
  }
 
  dsClient.login(username, password, servername, portnum);        
  dsClient.connect();        
}

function cleanup() {
  dsClient.disconnect();
}

function fillInLogin(username, host, port) {
  fillInUsed = true;
  //do not worry about adding the information, since I have
  //already extracted it from the cookie
  dwr.util.setValue("username",username);
  dwr.util.setValue("servername",host);
  dwr.util.setValue("portnum",port);
}

/**
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

/**
 * credit: http://www.elated.com/articles/javascript-and-cookies/
 */
function set_cookie(value)
{
  var cookie_string = "wonderland.serverlist=" + escape ( value );
  
  var tmp = new Date( );
  var expires = new Date( parseInt(tmp.getFullYear()) + parseInt("1") , tmp.getMonth(), tmp.getDay());
  cookie_string += "; expires=" + expires.toGMTString();

  document.cookie = cookie_string;
}