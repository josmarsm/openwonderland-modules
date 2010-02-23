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
package org.jdesktop.wonderland.modules.cmu.common.events.responses;

import java.io.Serializable;

/**
 * Object to represent a CMU response to a Wonderland event, e.g. an appropriate
 * function to call when that event occurs.  This is an abstract response, containing
 * only a function name; base classes should correspond to specific function
 * types.
 * @author kevin
 */
public abstract class CMUResponseFunction implements Serializable {

    private String functionName = "";

    public CMUResponseFunction() {
        
    }

    public CMUResponseFunction(String functionName) {
        this.setFunctionName(functionName);
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public abstract Object[] getArgumentValues();

    public abstract Class[] getArgumentClasses();

    //TODO: Use factory paradigm
    public abstract CMUResponseFunction createResponse(String functionName);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof CMUResponseFunction && this.getClass().equals(other.getClass())) {
            CMUResponseFunction otherResponse = (CMUResponseFunction) other;
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
