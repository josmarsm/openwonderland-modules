/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */


package org.jdesktop.wonderland.modules.eventplayer.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a tape to the event player
 * @author Bernard Horan
 */
@XmlAccessorType(XmlAccessType.FIELD)
//disambiguate from any other XML type of the same name
@XmlType( namespace="eventplayer" )
public class Tape implements Serializable, Comparable<Tape> {

    private String tapeName;

    /**
     * Default constructor
     */
    public Tape() {
    }
    
    /**
     * Create a new instance of this class with tapename as the name of this tape
     * @param filename
     */
    public Tape(String filename) {
        this();
        this.tapeName = filename;
    }


    /**
     * Provide the string name of this object. I.e. the tapename
     * @return the name of the tape
     */
    @Override
    public String toString() {
        return tapeName;
    }

    /**
     * Compare this object with the argument<br>
     * Used to order tapes in a sorted collection
     * @param t the object against which to compare this object
     * @return an int indicating if this object sorts before, after or same as the argument
     */
    public int compareTo(Tape t) {
        //Tape t = (Tape) o;
        return tapeName.compareToIgnoreCase(t.tapeName);
    }
    
    /**
     * Return the name of the tape
     * @return the tape name
     */
    public String getTapeName() {
        return tapeName;
    }
}
