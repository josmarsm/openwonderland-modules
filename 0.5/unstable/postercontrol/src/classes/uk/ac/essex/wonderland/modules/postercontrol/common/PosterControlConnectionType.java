/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.postercontrol.common;

import org.jdesktop.wonderland.common.comms.ConnectionType;

/**
 * This class defines the ConnectionType used to send messages between the
 * PosterControlConnection and the PosterControlConnectionHandler. Use
 * PosterControlConnectionType.TYPE for a constant version of this type.
 *
 * @author Bernard Horan
 */
public class PosterControlConnectionType extends ConnectionType {
    /** A unique name for this connection type */
    private static final String NAME = "__PosterControlConnection";

    /** The type constant for this connection type */
    public static final PosterControlConnectionType TYPE =
            new PosterControlConnectionType();

    /**
     * Default constructor
     */
    public PosterControlConnectionType() {
        super (NAME);
    }
}
