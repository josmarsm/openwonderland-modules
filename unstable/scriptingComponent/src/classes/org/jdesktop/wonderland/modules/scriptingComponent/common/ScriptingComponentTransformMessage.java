package org.jdesktop.wonderland.modules.scriptingComponent.common;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

public class ScriptingComponentTransformMessage extends CellMessage
    {
    private int transformCode = 0;
    private Quaternion transform;
    private Vector3f vector;
    public static final int TRANSLATE_TRANSFORM = 0;
    public static final int ROTATE_TRANSFORM = 1;
    public static final int SCALE_TRANSFORM = 2;
    
    public ScriptingComponentTransformMessage(CellID cellID, int TransformCode, Quaternion Transform)
        {
        super(cellID);
        this.transformCode = TransformCode;
        this.transform = Transform;
        }
    
    public ScriptingComponentTransformMessage(CellID cellID, int TransformCode, Vector3f Vector)
        {
        super(cellID);
        this.transformCode = TransformCode;
        this.vector = Vector;
        }
    
    public int getTransformCode()
        {
        return this.transformCode;
        }
    
    public void setTransformCode(int TransformCode)
        {
        this.transformCode = TransformCode;
        }
    
    public Quaternion getTransform()
        {
        return this.transform;
        }
    
    public void setTransform(Quaternion Transform)
        {
        this.transform = Transform;
        }
    
    public Vector3f getVector()
        {
        return this.vector;
        }
    
    public void setVector(Vector3f Vector)
        {
        this.vector = Vector;
        }
    }
