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

import com.jme.math.Vector3f;

/**
 * Response denoting a CMU function which takes three parameters representing an
 * avatar position.
 * @author kevin
 */
public class AvatarPositionFunction extends CMUResponseFunction {

    private Vector3f avatarPosition = null;

    public AvatarPositionFunction() {
        super();
    }

    public AvatarPositionFunction(String functionName) {
        super(functionName);
    }

    public Vector3f getAvatarPosition() {
        return avatarPosition;
    }

    public void setAvatarPosition(Vector3f avatarPosition) {
        this.avatarPosition = avatarPosition;
    }

    @Override
    public Object[] getArgumentValues() {
        Double[] args = new Double[3];
        for (int i = 0; i < 3; i++) {
            args[i] = new Double(this.getAvatarPosition().get(i));
        }
        return args;
    }

    @Override
    public Class[] getArgumentClasses() {
        return new Class[]{Double.class, Double.class, Double.class};
    }

    @Override
    public AvatarPositionFunction createResponse(String functionName) {
        return new AvatarPositionFunction(functionName);
    }
}
