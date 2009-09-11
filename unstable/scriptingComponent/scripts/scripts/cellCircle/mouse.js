print('Hello from javascript - mouse script  \r\n') ;
MyClass.getInitialPosition();
//stateFloat[0] = MyClass.xInitial;
//stateFloat[1] = MyClass.yInitial;
//stateFloat[2] = MyClass.zInitial;
stateFloat[5] = 2 * Math.PI;
// calculate the position position
stateFloat[3] = stateFloat[0] + Math.cos(0) * 2;
print('Initial xx = ' + stateFloat[3] + '\r\n');
stateFloat[4] = stateFloat[2] + Math.sin(0) * 2;
print('Initial zz = ' + stateFloat[4] + '\r\n');
MyClass.setTranslation(stateFloat[3], stateFloat[1], stateFloat[4]);
stateInt[0] = 1;
MyClass.startTimer(100, 0);
