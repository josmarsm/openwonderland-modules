//only have the one instance of DSClient
var dsClient = new DSClient();

function DSClient () {
  var username;
  var servername;
  var portnum;
  
  var sessionId = null;
  
  /*
   * Initialize the app on load.
   */
  this.init = function() {
  // Kick off DWR reverse-ajax (Comet).
  dwr.engine.setActiveReverseAjax(true);
  dwr.engine.setErrorHandler(this.errorHandler); 
  } // End init().

  this.login = function(username, password, servername, portnum) {
    this.username = username;
    this.password = password;
    this.portnum = portnum;
    Servlet.startDSClient( username, password, servername, portnum);
  }// end login 
   
  this.connect = function() {
    Servlet.connect(  {
      callback : function(data) {
        if( data == 1 ) {
          document.location = "view.jsp";
        } else {
          document.location.reload();
          dwr.util.setValue("msg2","Login Failed. Please try again!");
        }
      }
    });
  }

  this.disconnect = function() {
    Servlet.disconnect(  {
      callback : function() {
        alert("You have been logged off");
      }
    });
  } 
  
  this.errorHandler = function(msg) {
    alert("ERROR: " + msg);
  }
  
  this.cbErrorMessage = function(msg) {
    alert("Error encountered: " + msg);
  }
} 
