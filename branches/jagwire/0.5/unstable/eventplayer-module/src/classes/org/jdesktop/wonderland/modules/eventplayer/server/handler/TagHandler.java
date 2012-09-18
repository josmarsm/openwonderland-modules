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
package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import java.util.concurrent.Semaphore;
import org.xml.sax.Attributes;

/**
 * An interface that provides methods to intercept SAX parsing
 * @author Bernard Horan
 */
public interface TagHandler {

    /**
     * Called by the SAX handler when it has its startElement() method called
     * @param atts the attributes of an XML element
     * @param semaphore the semaphore to be signalled
     */
    public void startTag(Attributes atts, Semaphore semaphore);

    /**
     * Called by the SAX handler when it has its characters() method called
     * @param ch the characters enclosed by the XML element
     * @param start the start of the array of chars
     * @param length the length of the chars
     * @param semaphore the semaphore to be signalled
     */
    public void characters(char ch[], int start, int length, Semaphore semaphore);

    /**
     * Called by the SAX handler when it has its endElement() method called
     * @param semaphore the semaphore to be signalled
     */
    public void endTag(Semaphore semaphore);
}
