/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.tokensheet.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;

/**
 * Stores all the information of one student such as Name, tokens, passes and 
 * strikes.
 * 
 * @author Kaustubh
 */
@ISocialModel
@XmlRootElement(name = "Student")
public class Student implements Serializable {

    private String name;
    private int tokenValue, passValue, strikesValues;

    public Student() {
    }

    public Student(String username) {
        this.name = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPassesValue() {
        return passValue;
    }

    public void setPassesValue(int passes) {
        this.passValue = passes;
    }

    public int getStrikesValue() {
        return strikesValues;
    }

    public void setStrikesValue(int strikes) {
        this.strikesValues = strikes;
    }

    public int getTokensValue() {
        return tokenValue;
    }

    public void setTokensValue(int tokens) {
        this.tokenValue = tokens;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Tokens: " + tokenValue + ", Passes: " + passValue + ", Strikes: " + strikesValues;
    }
}
