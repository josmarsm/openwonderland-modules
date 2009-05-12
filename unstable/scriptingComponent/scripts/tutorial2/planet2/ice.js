print('SCRIPT - ICE.JS - PLANET2 Hello from javascript - ice script \r\n') ;
print('SCRIPT - ICE.JS - PLANET2 Message received ' + stateString[0] + '\r\n');

var v = new java.lang.Runnable()
{
run: function()
  {
  print('SCRIPT - ICE.JS - PLANET2 Hello javascript world - from run   **** \r\n') ;
  MyClass.getInitialPosition();
  var0 = stateFloat[0];
  var1 = stateFloat[1];
  var2 = stateFloat[2];
  var3 = var0 + Math.cos(0) * 10;
  var4 = var2 + Math.sin(0) * 10;
  MyClass.setTranslation(var3, var1, var4, 0);
  stateInt[0] = 1;
  while(stateInt[0] == 1)
    {
    myAni();
    MyClass.mySleep(50);
    }

  function myAni()
    {
    now = new Date();
    incr = (now.getSeconds() + (now.getMilliseconds() / 1000)) / 60;
    var3 = var0 + Math.cos(incr * 2 * Math.PI) * 10;
    var4 = var2 + Math.sin(incr * 2 * Math.PI) * 10;
    MyClass.setTranslation(var3, var1, var4, 0);
    }
  
  }
}

var split_message = stateString[19].split(",");
if(split_message[0] == "401")
    {
    if(split_message[1] == 'start')
 	{
        t = new java.lang.Thread(v);
        t.start();
        print('SCRIPT - ICE.JS - PLANET2 START message for cell \r\n');
	}
    else
	{
	if(split_message[1] == 'stop')
	    {
	    stateInt[0] = 0;
	    MyClass.setTranslation(stateFloat[0], stateFloat[1], stateFloat[2], 0);
            print('SCRIPT - ICE.JS - PLANET2 STOP message for cell PLANET2\r\n');
	    }
	else
	    {
    	    print('SCRIPT - ICE.JS - PLANET2 some other 401 message - ' + stateString[19] + '\r\n');
	    }
	}
    }
else
    {
    print('SCRIPT - ICE.JS - PLANET2 some other message - ' + stateString[19] + '\r\n');
    }
//v.run();

