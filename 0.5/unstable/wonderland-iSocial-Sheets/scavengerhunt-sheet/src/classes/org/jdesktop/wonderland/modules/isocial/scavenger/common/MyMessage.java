/* CREATE_FOR_ESL_AUDIO
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author nilang
 */

public class MyMessage  extends CellMessage{
    
    private String message =null;
    private String action = null;
    private String fileName = "DefaultFile";
    private String callId;
    private Vector3f vec;
    private String username;
    
    public MyMessage(String action,String fileName,String callId,Vector3f vec,String username) {
        super(null);
      
        this.action = action;   
        this.fileName = fileName;
        this.callId = callId;
        this.vec = vec;
        this.username = username;
    } 
    
    public String getAction()
    {
        return action;
    }
    
    public void setAction(String action)
    {
        this.action = action;
    }
    
    
    public void setMessage(String myMessage)
    {
        this.message = myMessage;
    }
    public String getMessage()
    {
        return message;
    }
    
    public String getFileName()
    {
        return fileName;
    }
    
    public void setFileName(String fileName)           
    {
        this.fileName = fileName;
    }
    
    public String getCallId()
    {
        return callId;
    }
    
    public void setCallId(String callId)           
    {
        this.callId = callId;
    }
    
    
    public Vector3f getVector()
    {
        return vec;
    }
    
    
    public void setVector(Vector3f vec)
    {
        this.vec = vec;
    }
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)           
    {
        this.username = username;
    }
    
}
