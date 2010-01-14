/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.cmu.common.events;

import java.io.Serializable;

/**
 * Object to represent a CMU response to a Wonderland event, e.g. an appropriate
 * function to call when that event occurs.
 * @author kevin
 */
public class WonderlandResponse implements Serializable {

    private String functionName = "";

    public WonderlandResponse() {
        
    }

    public WonderlandResponse(String functionName) {
        this.setFunctionName(functionName);
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof WonderlandResponse && this.getClass().equals(other.getClass())) {
            WonderlandResponse otherResponse = (WonderlandResponse) other;
            if (this.getFunctionName().equals(otherResponse.getFunctionName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.functionName != null ? this.functionName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Response: " + getFunctionName();
    }
}
