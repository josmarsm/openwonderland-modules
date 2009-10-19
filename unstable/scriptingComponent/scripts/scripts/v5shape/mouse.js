var v = new java.lang.Runnable()
{
run: function()
  {
  print('Hello javascript world - from script   **** \r\n') ;
  MyClass.getInitialPosition();
  var0 = stateFloat[0];
  var1 = stateFloat[1];
  var2 = stateFloat[2];
  var3 = var0 + Math.cos(0) * 5;
  var4 = var2 + Math.sin(0) * 5;
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
    incr = (now.getSeconds() + (now.getMilliseconds() / 1000)) / 10;
    var3 = var0 + Math.cos(incr * 2 * Math.PI) * 5;
    var4 = var2 + Math.sin(incr * 2 * Math.PI) * 5;
    MyClass.setTranslation(var3, var1, var4);
    MyClass.setRotation(1, 1, 1, incr * Math.PI * 2);
    }
  
  }
}

t = new java.lang.Thread(v);
t.start();
print('after t.start\r\n');
println(MyClass.testName);
MyClass.testName = "graps";
println(MyClass.testName);
println(MyClass.getName());
MyClass.putName("Rubbit");
MyClass.testInt = 106;
println(testInt);
println(MyClass.testInt);
stateString[0] = "Inside equals";
println(stateString[0]);
//MyClass.sendSocketMessage('AbCdEfGhIj');

