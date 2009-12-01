/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.common;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author morrisford
 */
public class ScriptingComponentCellCreateMessage extends CellMessage
    {
    private String ClassName;
    private float   x;
    private float   y;
    private float   z;
    private String  CellName;

    public ScriptingComponentCellCreateMessage(String className, float X, float Y, float Z, String cellName)
        {
        ClassName = className;
        x = X;
        y = Y;
        z = Z;
        CellName = cellName;
        }

    public void setClassName(String className)
        {
        ClassName = className;
        }

    public String getClassName()
        {
        return ClassName;
        }

    public void setX(float X)
        {
        x = X;
        }

    public float getX()
        {
        return x;
        }

    public void setY(float Y)
        {
        y = Y;
        }

    public float getY()
        {
        return y;
        }

    public void setZ(float Z)
        {
        z = Z;
        }

    public float getZ()
        {
        return z;
        }

    public void setCellName(String cellName)
        {
        CellName = cellName;
        }

    public String getCellName()
        {
        return CellName;
        }
    }
