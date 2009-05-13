import	javax.script.ScriptContext;
import  org.jdesktop.wonderland.client.cell.ScriptingComponent;

class mouse3 
    {
    private static ScriptContext myContext;

    public static void main(String[] args) 
        {
        System.out.println("Hello World! - args = " + args); 
	Object myName = myContext.getAttribute("name");
	System.out.println("myName = " + myName);
	Object myClassObject = myContext.getAttribute("MyClass");
	Class<org.jdesktop.wonderland.client.cell.ScriptingComponent> sc = myClassObject.getClass();
	System.out.println("testMethod = " + sc.testMethod("Testtest"));
	System.out.println("Class = " + myClassObject);
        }

    public static void setScriptContext(ScriptContext ctx)
	{
	myContext = ctx;
	System.out.println("Inside setScriptContext");
	}
    }
