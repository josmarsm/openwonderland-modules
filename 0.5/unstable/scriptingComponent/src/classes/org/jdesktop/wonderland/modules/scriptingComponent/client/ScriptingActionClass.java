/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author morrisford
 */
public class ScriptingActionClass
    {
    private String name = null;
//    Runnable[] cmdArray;
    Map<String, ScriptingRunnable> cmdMap = new HashMap<String, ScriptingRunnable>();
//    Map<String, ScriptingAction> classMap = new HashMap<String, ScriptingAction>();

    public void setName(String Name)
        {
        name = Name;
        }
    public String getName()
        {
        return name;
        }
/*
    public void setActionArray(Runnable[] actionArray)
        {
        cmdArray = actionArray;
        }
    public Runnable getActionArray(int which)
        {
        return cmdArray[which];
        }
 */
    public void insertCmdMap(String Name, ScriptingRunnable runnable)
        {
        cmdMap.put(Name, runnable);
        }
    public ScriptingRunnable getCmdMap(String Name)
        {
        return cmdMap.get(Name);
        }
/*
    public void insertClassMap(String Name, ScriptingAction theClass)
        {
        classMap.put(Name, theClass);
        }
    public ScriptingAction getClassMap(String Name)
        {
        return classMap.get(Name);
        }
 */
    }
