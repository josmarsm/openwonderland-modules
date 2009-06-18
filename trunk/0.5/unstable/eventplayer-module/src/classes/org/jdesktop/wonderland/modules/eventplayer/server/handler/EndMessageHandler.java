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
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import org.jdesktop.wonderland.modules.eventplayer.server.MessageReplayer;
import org.xml.sax.Attributes;

/**
 * A TagHandler responsible for handling XML elements named "EndMessage"
 * @author Bernard Horan
 */
public class EndMessageHandler extends DefaultTagHandler {
    private long timestamp;

    public EndMessageHandler(MessageReplayer messageReplayer) {
        super(messageReplayer);
    }

    @Override
    public void startTag(Attributes atts) {
        super.startTag(atts);
        //Get the timestamp from the attributes of the XML element
        String timestampString = atts.getValue("timestamp");
        timestamp = Long.parseLong(timestampString);
    }

    @Override
    public void endTag() {
        super.endTag();
        //Inform the message replayer that we've hit the end of the changes
        messageReplayer.endChanges(timestamp);
    }
}
