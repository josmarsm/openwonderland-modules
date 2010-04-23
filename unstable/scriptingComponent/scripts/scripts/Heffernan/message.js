print('PLANET1 Hello from javascript - message script \r\n') ;
print('PLANET1 Message received ' + stateString[0] + '\r\n');

var v = new java.lang.Runnable()
{
run: function()
  {
  print('PLANET1 Hello javascript world - from run   **** \r\n') ;
  MyClass.getInitialPosition();
  var0 = stateFloat[0];
  var1 = stateFloat[1];
  var2 = stateFloat[2];
  var3 = var0 + Math.cos(0) * 1;
  var4 = var2 + Math.sin(0) * 1;
  MyClass.setTranslation(var3, var1, var4);
  stateInt[0] = 1;
  while(stateInt[0] == 1)
    {
    myAni();
    MyClass.mySleep(50);
    }

  function myAni()
    {
    now = new Date();
    incr = (now.getSeconds() + (now.getMilliseconds() / 1000)) / 5;
    var3 = var0 + Math.cos(incr * 2 * Math.PI) * 1;
    var4 = var2 + Math.sin(incr * 2 * Math.PI) * 1;
    MyClass.setTranslation(var3, var1, var4);
    }
  
  }
}

var split_message = stateString[0].split(",");
if(split_message[0] == "101")
    {
    if(split_message[1] == 'start')
 	{
        t = new java.lang.Thread(v);
        t.start();
        print('PLANET1 START message for cell \r\n');
	}
    else
	{
	if(split_message[1] == 'stop')
	    {
	    stateInt[0] = 0;
	    MyClass.setTranslation(stateFloat[0], stateFloat[1], stateFloat[2]);
            print('PLANET1 STOP message for cell PLANET1\r\n');
	    }
	else
	    {
    	    print('PLANET1 some other 101 message - ' + stateString[0] + '\r\n');
	    }
	}
    }
else
    {
    print('PLANET1 some other message - ' + stateString[0] + '\r\n');
    }

