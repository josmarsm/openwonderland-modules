print('PLANET6 Hello from javascript - message script \r\n') ;
print('PLANET6 Message received ' + stateString[0] + '\r\n');

var v = new java.lang.Runnable()
{
run: function()
  {
  print('PLANET6 - Hello javascript world - from script   **** \r\n') ;
  MyClass.getInitialPosition();
  var0 = stateFloat[0];
  var1 = stateFloat[1];
  var2 = stateFloat[2];
  var3 = var0 + Math.cos(0) * 9;
  var4 = var2 + Math.sin(0) * 9;
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
    var3 = var0 + Math.cos(incr * 2 * Math.PI) * 9;
    var4 = var2 + Math.sin(incr * 2 * Math.PI) * 9;
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
        print('PLANET6 START message for cell \r\n');
        }
    else
        {
        if(split_message[1] == 'stop')
            {
            stateInt[0] = 0;
            MyClass.setTranslation(stateFloat[0], stateFloat[1], stateFloat[2]);
            print('PLANET6 STOP message for cell \r\n');
            }
        else
            {
            print('PLANET6 some other 101 message - ' + stateString[0] + '\r\n');
            }
        }
    }
else
    {
    print('PLANET6 some other message - ' + stateString[0] + '\r\n');
    }

