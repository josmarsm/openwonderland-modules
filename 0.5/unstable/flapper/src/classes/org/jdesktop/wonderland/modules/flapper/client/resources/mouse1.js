$id = 70 + cellID;
MyClass.setAnimationIceCode($id);
MyClass.watchMessage($id);
MyClass.setAnimationStartKeyframe(0);
MyClass.setAnimationEndKeyframe(150);
MyClass.setAnimationStartTranslate(0, 0, 0);
MyClass.setAnimationStartRotation(0, 0, 0);
MyClass.setAnimationTimeMultiplier(1);
MyClass.executeAction("animate3Nodes", "fuselage", "leftFlapper", "rightFlapper");
