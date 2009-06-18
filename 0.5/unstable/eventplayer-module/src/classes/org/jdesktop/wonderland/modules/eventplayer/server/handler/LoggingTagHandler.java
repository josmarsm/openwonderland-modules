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

import org.jdesktop.wonderland.modules.eventplayer.server.*;
import java.util.logging.Logger;
import org.xml.sax.Attributes;

/**
 * A tag handler that just logs method calls. For testing purposes.
 * @author Bernard Horan
 */
public class LoggingTagHandler implements TagHandler {
    private static final Logger logger = Logger.getLogger(LoggingTagHandler.class.getName());

    public LoggingTagHandler(EventPlayer eventPlayer) {
        
    }

    public void startTag(Attributes atts) {
        logger.info(atts.toString());
    }

    public void characters(char[] ch, int start, int length) {
        logger.info(ch.toString());
    }

    public void endTag() {
        logger.info("endTag");
    }

}
