/**
 * Project Looking Glass
 *
 * $RCSfile: JPEGImageProducer.java,v $
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.3 $
 * $Date: 2008/03/14 18:14:27 $
 * $State: Exp $
 * $Id: JPEGImageProducer.java,v 1.3 2008/03/14 18:14:27 bernard_horan Exp $
 */

package org.jdesktop.wonderland.modules.movierecorder.client.utils;


/**
 * This interface represents a producer of JPEG images data.
 *
 * @author Mikael Nordenberg, <a href="http://www.ikanos.se">www.ikanos.se</a>
 */
public interface JPEGImageProducer {
    /**
     * Returns the next JPEG image to be processed.
     * @return the next JPEG image as bytes or null if no additional images are available
     */
    public byte[] getNextImage();
}

