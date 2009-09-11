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
package org.jdesktop.wonderland.modules.connectionsample.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message sent to the ColorChangeConnectionHandler to request that a new
 * cell is created.  A CreateCellResponseMessage or an ErrorMessage will
 * be returned, depending if the creation failed or succeeded.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class CreateCellRequestMessage extends Message {
    // no state is needed in the message
}
