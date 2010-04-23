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

/**
 * A function response with no arguments.
 * @author kevin
 */
public class NoArgumentFunction extends CMUResponseFunction {

    public NoArgumentFunction() {
        super();
    }

    public NoArgumentFunction(String functionName) {
        super(functionName);
    }

    @Override
    public Object[] getArgumentValues() {
        return new Object[0];
    }

    @Override
    public Class[] getArgumentClasses() {
        return new Class[0];
    }

    @Override
    public NoArgumentFunction createResponse(String functionName) {
        return new NoArgumentFunction(functionName);
    }

}
