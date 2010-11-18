/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.admintools.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * Message to display a broadcast message to all connected users
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class BroadcastMessage extends Message {
    private final String text;

    public BroadcastMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
