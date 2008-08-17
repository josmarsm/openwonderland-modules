/**
 * Project Looking Glass
 *
 * $RCSfile$
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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.pdfviewer.client.cell;

public interface PDFCellMenuListener {
    public void open();
    public void first();
    public void previous();
    public void gotoPage();
    public void next();
    public void last();
    public void startSlideShow();
    public void pauseSlideShow();
    public void zoomIn();
    public void zoomOut();
    public void sync();
    public void unsync();
}
