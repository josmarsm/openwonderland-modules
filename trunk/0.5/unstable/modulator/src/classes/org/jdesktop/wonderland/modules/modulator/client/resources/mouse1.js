$id = 70 + cellID;
MyClass.setAnimationIceCode($id);
MyClass.watchMessage($id);
MyClass.setAnimationStartKeyframe(0);
MyClass.setAnimationEndKeyframe(0);
MyClass.setAnimationStartTranslate(0, 4, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(1);
MyClass.executeAction("animateAllNodes");
