/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.isocial.generic.web.resources;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ryan
 */
@XmlRootElement(name="testing")
public class TestingJSON {

    private int pin;
    private String name;
    private List<String> strings;

    protected TestingJSON() {
        
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> ss) {
        strings = ss;
    }

    public String toString() {
        String s = "-TestingJSON!-\n"
                +"\nName: " + name
                +"\nPIN: " +pin
                + strings.toString();

        return s;
    }
}
