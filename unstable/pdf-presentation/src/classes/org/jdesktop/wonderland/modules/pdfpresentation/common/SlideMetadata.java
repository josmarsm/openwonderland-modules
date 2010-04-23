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

package org.jdesktop.wonderland.modules.pdfpresentation.common;

import com.jme.math.Vector3f;
import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Stores the data the describes where a particular slide is located.
 *
 * @author Drew Harry <dharry@media.mit.edu>
 */
public class SlideMetadata implements Serializable {

    private CellTransform transform;

    // I kind of didn't want to store this at first, and just keep a URI for
    // the texture, but right now we get BufferedImages from the PDFContentImporter
    // by giving it a slide index, so we'll have to stick with this for now.
    private int pageIndex;

    // We'll probably want per-slide width and height here
    // later, but at the moment it's not exposed in the PDFContentImporter API.
    // We can safely just work off the maxes most of the time anyway.

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pdfPageIndex) {
        this.pageIndex = pdfPageIndex;
    }

    public CellTransform getTransform() {
        return transform;
    }

    public void setTransform(CellTransform transform) {
        this.transform = transform;
    }

    public String toString() {
        return "[" + pageIndex + "]: " + transform.getTranslation(null) + "; " + transform.getRotation(null);
    }

}
