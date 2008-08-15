<%@ page contentType="text/html; charset=ISO-8859-5" %>
<%@ page import="java.util.*, java.util.regex.*, java.net.*" %>

<%
  
  String user_agent = request.getHeader("user-agent"); 
  
  String []mobile = new String[]{".*[Aa]cs-.*",
                                "alav",
                                "alca",
                                "amoi",
                                "audi",
                                "aste",
                                "avan",
                                "benq",
                                "bird",
                                ".*[Bb]lac.*",
                                "blaz",
                                "brew",
                                "cell",
                                "cldc",
                                "cmd-",
                                "dang",
                                "doco",
                                "eric",
                                "hipt",
                                "inno",
                                "ipaq",
                                ".*i[Pp]od.*",
                                ".*i[Pp]ho.*",
                                "java",
                                "jigs",
                                "kddi",
                                "keji",
                                "leno",
                                "lg-c",
                                "lg-d",
                                "lg-g",
                                "lge-",
                                "maui",
                                "maxo",
                                "midp",
                                "mits",
                                "mmef",
                                ".*[Mm]obi.*",
                                "mot-",
                                "moto",
                                "mwbp",
                                "nec-",
                                "newt",
                                "noki",
                                "opwv",
                                "palm",
                                "pana",
                                "pant",
                                "pdxg",
                                "phil",
                                "play",
                                "pluc",
                                "port",
                                "prox",
                                "qtek",
                                "qwap",
                                "sage",
                                "sams",
                                "sany",
                                "sch-",
                                "sec-",
                                "send",
                                "seri",
                                "sgh-",
                                "shar",
                                "sie-",
                                "siem",
                                "smal",
                                "smar",
                                "sony",
                                "sph-",
                                "symb",
                                "t-mo",
                                "teli",
                                "tim-",
                                "tosh",
                                "tsm-",
                                "upg1",
                                "upsi",
                                "vk-v",
                                "voda",
                                "wap-",
                                "wapa",
                                "wapi",
                                "wapp",
                                "wapr",
                                "webc",
                                "winw",
                                "winw",
                                "xda",
                                "xda-"
                                 };
                                 
  //now check to see what it contains to set the version of the application to
  //either desktop or mobile
  
  //out.println(user_agent);
  int i = 0;
  
  for( i = 0; i < mobile.length; i++ ) {
    if( user_agent.matches(mobile[i])) {
      //make the browser mobile
      session.setAttribute( "browserType", "mobile" );
      //out.println("browser is mobile: i = " + i);
      break;
    }
  }
  
  if( i >= mobile.length ) {
    //default is desktop
    session.setAttribute( "browserType", "desktop" );
    //out.println("browser is desktop: i = " + i);
  }
  
%>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Content-Script-Type" content="text/javascript">
    <title>Window to Wonderland</title>
    
    <% if( ((String)session.getAttribute("browserType")).equals("desktop") ) { %>
      <link rel="stylesheet" type="text/css" href="css/login.css">
    <% } else { %>
      <link rel="stylesheet" type="text/css" href="css/login.mobi.css">
    <% } %>
    
    <!-- ************************************************************************************************************************  -->
    <!--  External scripts included after this page script, to prevent the first js from executing functions that are defined here -->
    <!-- and any others that I create should also be declared before the dwr scripts -->
    <!-- ************************************************************************************************************************ -->
    <script type="text/javascript" src="javascript/FuncTils.js"></script>
    <script type="text/javascript" src="javascript/DSClient.js"></script>
    <script type="text/javascript" src="javascript/Index.js"></script>

    <!-- this next section will have to include all class names that are found in dwr.xml -->
    <script type="text/javascript" src="/WonderlandWebUI/dwr/interface/Servlet.js"></script>
    <script type="text/javascript" src="/WonderlandWebUI/dwr/interface/ClientConnect.js"></script>
    
    <script type="text/javascript" src="/WonderlandWebUI/dwr/engine.js"></script>
    <script type="text/javascript" src="/WonderlandWebUI/dwr/util.js"></script>
  </head>
  <body onload="javascript:indexInit()"> 
    <div id="logindiv">
      <div id="promptDiv" class="basePrompt">
        <div id="image">
          <img src="resources/images/logo.gif" alt="logo missing"/>
        </div>
        <div id="text">
          <span id="msg1">Project Wonderland Web Portal</span><br/>
          <span id="msg2">Client only supports 0.4 </span><br/>
          <span id="msg3">Please Login</span>
        </div>
      </div>
      <div id="left" >
        <div class="title">Username:</div>
        <div class="entry">
          <input id="username" name="username" type="text" value="" alt="" title="" />
        </div>
        <div class="title">Password:</div>
        <div class="entry">
          <input id="password" name="password" type="password" value="" alt="" title="" />
        </div>
        <div class="title">Wonderland Server:</div>
        <div class="entry">
          <input id="servername" name="servername" type="text" value="" alt="" title="" />
        </div>
        <div class="title">Port Number:</div>
        <div class="entry">
          <input id="portnum" name="portnum" type="text" value="" alt="" title="" />
        </div>
      </div>
      <div id="right" >
        <div class="title">History </div>
        <div id="selectlist">
          <%
            String cookieName = "wonderland.serverlist";
            Cookie cookies [] = request.getCookies();
            Cookie myCookie = null;

            if (cookies != null) {
              for ( i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                  myCookie = cookies[i];
                  //now I need to parse the values in the cookie
                  //the values will be username,host,port|username,host,port|....
                  //to be filled in and output using the following format
                  //<div id="server" onclick="javascript:fillInLogin()"> username:host:port </div>
                  String tmp = URLDecoder.decode(myCookie.getValue(),"UTF-8");
                  //out.print(tmp);
                  String[] servers = tmp.split(",");
                  //out.print("servers="+servers[0]);
                  //out.print("serversSize="+servers.length);
                  
                  //String[] data = servers[0].split(":");
                  //out.print("data="+data[0]);
                  //out.print("dataSize="+data.length);
                  
                  for( int j = 0; j < servers.length ; j++ ) {
                    String[] data = servers[j].split(":");
                    if( data.length != 3 ) {
                      //flush cookie
                      Cookie cookie = new Cookie("wonderland.serverlist","");
                      cookie.setMaxAge(0); //cookie expires immediately
                      response.addCookie(cookie);
                    } else {
                      out.println("<div id='server' onclick='javascript:fillInLogin(\"" + data[0] + "\",\"" + data[1] + "\",\"" + data[2] + "\")' >" + data[0] + ":" + data[1] + ":" + data[2] + "</div>");
                    }
                  }
                  break;
                }
              }
            } else { //create cookie
              Cookie cookie = new Cookie("wonderland.serverlist","");
              cookie.setMaxAge(365 * 24 * 60 * 60); //cookie expires in a year
              response.addCookie(cookie);
            }
          %>
        </div>
      </div>
      <div id="buttonDiv">
        <input type="button" value="Login" onclick="javascript:indexLogin()"/>
      </div>
    </div>
  </body>
</html>
