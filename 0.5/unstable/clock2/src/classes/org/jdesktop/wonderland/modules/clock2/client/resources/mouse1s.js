print("This is a test of mouse1s.js - clock2\r\n");
now = new Date();
print("Time retrieve - " + now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds() + " - GMT diff = " + now.getTimezoneOffset()/60 + "\r\n");
MyClass.executeAction("setTime", now.getHours() + now.getTimezoneOffset()/60, now.getMinutes(), now.getSeconds());

