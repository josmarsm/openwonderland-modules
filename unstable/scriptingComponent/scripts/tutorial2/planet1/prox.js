//prox.js - planet1
print("SCRIPT - PROX.JS - Hello from proximity script ***************************************\r\n");
if(stateBoolean[18])
    print("SCRIPT - PROX.JS - Entered index = " + stateInt[18] + "\r\n");
    if(stateInt[18] == 0)
        MyClass.postMessageEvent("400,start", 300);
    if(stateInt[18] == 1)
        MyClass.postMessageEvent("400,start", 300);
    if(stateInt[18] == 2)
        MyClass.postMessageEvent("400,start", 300);
else
    print("SCRIPT - PROX.JS - Exited index = " + stateInt[18] + "\r\n");
    if(stateInt[18] == 0)
        MyClass.postMessageEvent("400,stop", 300);
    if(stateInt[18] == 1)
        MyClass.postMessageEvent("400,stop", 300);
    if(stateInt[18] == 2)
        MyClass.postMessageEvent("400,stop", 300);

