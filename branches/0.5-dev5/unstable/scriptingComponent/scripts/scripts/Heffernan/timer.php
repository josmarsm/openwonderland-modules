<?php 
$MyClass->testMethod("Hello from php - timer script");
$more = $MyClass->playAnimationFrame();
$MyClass->getFrameRate();
$MyClass->getAniFrame();
$handle = fopen("fpsCount.txt", "a");
$logString = sprintf("Animation step %d Framerate = %2.3f \n", $stateInt[0], $stateFloat[3]);
fwrite($handle, $logString);
fclose($handle);
$MyClass->setStateString($logString, $stateInt[0]);
if($more == 1)
    {
    $MyClass->startTimer(20);    
    }
else
    {
    for($i = 0; $i <= $stateInt[0]; $i++)
	{
	$MyClass->testMethod($stateString[$i]);
	}
    }
?>
