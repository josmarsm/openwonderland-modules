println('Hello from javascript - timer script  \r\n') ;
more = MyClass.playAnimationFrame();
MyClass.getFrameRate();
MyClass.getAniFrame();
println('Animation step ' + stateInt[0] + 1 + ' Framerate = ' + stateFloat[3]);
if(more == 1)
    MyClass.startTimer(20);    
