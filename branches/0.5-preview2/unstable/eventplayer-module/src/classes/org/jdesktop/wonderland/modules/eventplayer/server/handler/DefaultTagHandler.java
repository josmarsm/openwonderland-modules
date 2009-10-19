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

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangeReplayer;
import org.xml.sax.Attributes;

/**
 * A default implementation of TagHandler that is used when no other implementation
 * is available
 * @author Bernard Horan
 */
public class DefaultTagHandler implements TagHandler {
    protected static final Logger logger = Logger.getLogger(DefaultTagHandler.class.getName());
    //keeps the string contents of the XML element
    protected StringBuffer buffer;
    protected ChangeReplayer changeReplayer;
    
    /**
     * Create an instance of this class with the argument
     * @param changeReplayer the object responsible for replaying changes
     */
    public DefaultTagHandler(ChangeReplayer changeReplayer) {
        this.changeReplayer = changeReplayer;
    }

    public void startTag(Attributes atts, Semaphore semaphore) {
        //Create a new buffer to hold the string contents of the XML element
        buffer = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length, Semaphore semaphore) {
        //Append the string content of the XML element into the buffer
        buffer.append(ch, start, length);
    }

    public void endTag(Semaphore semaphore) {
        //No op
    }

}
