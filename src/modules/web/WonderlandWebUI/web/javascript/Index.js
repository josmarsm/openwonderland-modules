
var fillInUsed = false;

function indexInit() {
  document.getElementById('username').focus();
  dsClient.init();
}
/*
 * This function is used to call the login process of the servlet.
 * TODO: Handle failure of login, or non-unique login names
 */
function indexLogin() {
  var username = $("username").value;
  var password = $("password").value;
  var servername = $("servername").value;
  var portnum = $("portnum").value;
                          
  dwr.util.setValue("msg3","Please wait ...");
  
  //check to see if i need to add the login information to my cookie
  if( !fillInUsed ) {
    set_cookie("wonderland.serverlist", get_cookie("wonderland.serverlist") + username + ":" + servername + ":" + portnum + ",");
  }
 
  set_cookie("wonderland.username", username);
  
  dsClient.login(username, password, servername, portnum);        
  dsClient.connect();
}

/*
 * This function is when the user clicks on a list item of the history window.
 */
function fillInLogin(username, host, port) {
  fillInUsed = true;
  //do not worry about adding the information, since I have
  //already extracted it from the cookie
  dwr.util.setValue("username",username);
  dwr.util.setValue("servername",host);
  dwr.util.setValue("portnum",port);
}
