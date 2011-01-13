if(stateInt[0] == 0)
{
print("In state = 0\n\r");
MyClass.setAnimationStartKeyframe(1);
MyClass.setAnimationEndKeyframe(10);
MyClass.setAnimationStartTranslate(0, 0, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(5);
MyClass.executeAction("animateAllNodes");
stateInt[0] = 1;
}
else if(stateInt[0] == 1)
{
print("In state = 1\n\r");
MyClass.setAnimationStartKeyframe(11);
MyClass.setAnimationEndKeyframe(20);
MyClass.setAnimationStartTranslate(0, 0, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(5);
MyClass.executeAction("animateAllNodes");
stateInt[0] = 2;
}
else if(stateInt[0] == 2)
{
print("In state = 2\n\r");
MyClass.setAnimationStartKeyframe(21);
MyClass.setAnimationEndKeyframe(120);
MyClass.setAnimationStartTranslate(0, 0, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(1);
MyClass.executeAction("animateAllNodes");
stateInt[0] = 3;
}
else if(stateInt[0] == 3)
{
print("In state = 3\n\r");
MyClass.setAnimationStartKeyframe(121);
MyClass.setAnimationEndKeyframe(130);
MyClass.setAnimationStartTranslate(0, 0, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(5);
MyClass.executeAction("animateAllNodes");
stateInt[0] = 4;
}
else if(stateInt[0] == 4)
{
print("In state = 4\n\r");
MyClass.setAnimationStartKeyframe(131);
MyClass.setAnimationEndKeyframe(140);
MyClass.setAnimationStartTranslate(0, 0, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(5);
MyClass.executeAction("animateAllNodes");
stateInt[0] = 5;
}
else if(stateInt[0] == 5)
{
print("In state = 5\n\r");
MyClass.setAnimationStartKeyframe(141);
MyClass.setAnimationEndKeyframe(240);
MyClass.setAnimationStartTranslate(0, 0, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(1);
MyClass.executeAction("animateAllNodes");
stateInt[0] = 0;
}

