<?php $MyClass->testMethod("test test");
$MyClass->testMethod("Hello from mouse.php script");
$MyClass->testMethod($stateString[0]);
$MyClass->setStateString("scroob", 0);
$locEv = $Event;

if($locEv == 0)
    {
    $MyClass->testMethod("Left mouse - no modifiers"); 
    $MyClass->testMethod('World coor');
    $MyClass->getWorldCoor();
    $MyClass->testMethod('X = ' + stateFloat[0]);
    $MyClass->testMethod('Y = ' + stateFloat[1]);
    $MyClass->testMethod('Z = ' + stateFloat[2]);

    //stateString[0] = MyClass.ClientContext.getInstance().toString();
    //println('ClientContext = ' + MyClass.testName);
    $MyClass->buildAnimation("animation2");
    $MyClass->startTimer(50);
    }
else if($locEv == 1)
    $MyClass->testMethod("Middle mouse - no modifiers"); 
else if($Event == 2)
    $MyClass->testMethod("Right mouse - no modifiers"); 
else if($Event == 3)
    $MyClass->testMethod("Left mouse - shift"); 
else if($Event == 4)
    $MyClass->testMethod("Middle mouse - shift"); 
else if($Event == 5)
    $MyClass->testMethod("Right mouse - shift"); 
else if($Event == 6)
    $MyClass->testMethod("Left mouse - control"); 
else if($Event == 7)
    $MyClass->testMethod("Middle mouse - control"); 
else if($locEv == 8)
    $MyClass->testMethod("Right mouse - control"); 
?>

