print('Hello from javascript - message script  \r\n') ;
print('Message received ' + stateString[0] + '\r\n');

var v = new java.lang.Runnable()
{
run: function()
  {
  print('Hello javascript world - from script   **** \r\n') ;
  MyClass.getInitialPosition();
  var0 = stateFloat[0];
  var1 = stateFloat[1];
  var2 = stateFloat[2];
  var3 = var0 + Math.cos(0) * 2;
  var4 = var2 + Math.sin(0) * 2;
  var5 = 2 * Math.PI;
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
    var3 = var0 + Math.cos(incr * var5) * 2;
    var4 = var2 + Math.sin(incr * var5) * 2;
    MyClass.setTranslation(var3, var1, var4);
    }
  
  }
}

var split_message = stateString[0].split(",");
if(split_message[0] == "100")
    {
    t = new java.lang.Thread(v);
    t.start();
    print('after t.start\r\n');
    }
else
    {
    print('some other message\r\n');
    }

