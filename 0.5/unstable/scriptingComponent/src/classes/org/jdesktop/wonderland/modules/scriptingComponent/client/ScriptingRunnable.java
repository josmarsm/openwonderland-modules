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
    public String string1;
    public String string2;
    public String string3;

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

    public void set3Strings(String one, String two, String three)
        {
        string1 = one;
        string2 = two;
        string3 = three;
        }
    
    public void setSingleInt(int A)
        {
        a = A;
        }
    public void run()
        {

        }

    }
