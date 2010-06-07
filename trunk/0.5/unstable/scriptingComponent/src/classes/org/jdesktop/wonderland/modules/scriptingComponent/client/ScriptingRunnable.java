/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.client;

/**
 *
 * @author morrisford
 */
public class ScriptingRunnable implements Runnable
    {
    public float x;
    public float y;
    public float z;
    public int a;
    public String avatar;
    public String[] nameArray;
    public String animation;

    public void setNameArray(String[] NameArray)
        {
        nameArray = NameArray;
        }

    public String[] getNameArray()
        {
        return nameArray;
        }

    public void setAnimation(String Animation)
        {
        animation = Animation;
        }

    public void setAvatar(String Avatar)
        {
        avatar = Avatar;
        }
    
    public void setPoint(float X, float Y, float Z)
        {
        x = X;
        y = Y;
        z = Z;
        }

    public void setSingleInt(int A)
        {
        a = A;
        }
    public void run()
        {

        }

    }
