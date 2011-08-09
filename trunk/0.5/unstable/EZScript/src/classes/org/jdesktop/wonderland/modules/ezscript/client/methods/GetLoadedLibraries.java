
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class GetLoadedLibraries implements ReturnableScriptMethodSPI {

    private static java.lang.reflect.Field LIBRARIES = null;
    
    static {
        try {
            LIBRARIES = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            LIBRARIES.setAccessible(true);            
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(GetLoadedLibraries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(GetLoadedLibraries.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private List<String> libs;
    public String getDescription() {
        return "Returns a list of all the native libraries loaded.\n"
                + "Great for use with the Show() command.";
    }

    public String getFunctionName() {
        return "GetLoadedLibraries";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        libs = new ArrayList<String>();
        //throw new UnsupportedOperationException("Not supported yet.");
        
    }

    public Object returns() {
        //throw new UnsupportedOperationException("Not supported yet.");
       return libs;
       
        
    }

    public void run() {
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();
        try {
            final Vector<String> libraries = (Vector<String>)LIBRARIES.get(loader);
            
            for(String s: libraries) {
                libs.add(process(s));
            }
            
            //libs.addAll(libraries);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GetLoadedLibraries.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GetLoadedLibraries.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String process(String in) {
        if(in.contains(".dll")) {
            
            return in.substring(in.lastIndexOf("\\")+1, in.length());
        }
        return in.substring(in.lastIndexOf("/")+1, in.length());
    }
    
}
