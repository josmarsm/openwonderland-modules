print('Hello from javascript - mouse3 script  \r\n') ;
// set the step value to initialize
// calculate the position 
now = new Date();
incr = now.getSeconds() / 60;
stateFloat[3] = stateFloat[0] + Math.cos(incr * stateFloat[5]) * 2;
print('xx = ' + stateFloat[3] + '\r\n');
stateFloat[4] = stateFloat[2] + Math.sin(incr * stateFloat[5]) * 2;
print('zz = ' + stateFloat[4] + '\r\n');
MyClass.setTranslation(stateFloat[3], stateFloat[1], stateFloat[4]);
print('Seconds = ' + now.getSeconds() + ' incr = ' + incr + '\r\n');
if(stateInt[0] == 1)
    MyClass.startTimer(100, 0);
