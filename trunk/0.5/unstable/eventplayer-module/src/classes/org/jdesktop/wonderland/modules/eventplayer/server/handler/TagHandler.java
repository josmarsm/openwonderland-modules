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

import org.xml.sax.Attributes;

/**
 * An interface that provides methods to intercept SAX parsing
 * @author Bernard Horan
 */
public interface TagHandler {

    /**
     * Called by the SAX handler when it has its startElement() method called
     * @param atts the attributes of an XML element
     */
    public void startTag(Attributes atts);

    /**
     * Called by the SAX handler when it has its characters() method called
     * @param ch the characters enclosed by the XML element
     * @param start the start of the array of chars
     * @param length the length of the chars
     */
    public void characters(char ch[], int start, int length);

    /**
     * Called by the SAX handler when it has its endElement() method called
     */
    public void endTag();
}
