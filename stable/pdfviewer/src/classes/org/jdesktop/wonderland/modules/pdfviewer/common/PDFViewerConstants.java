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
package org.jdesktop.wonderland.modules.pdfviewer.common;

/**
 * PDF viewer shared state keys.
 *
 * @author nsimpson
 */
public class PDFViewerConstants {

    // the name of the map where we store status
    public static final String STATUS_MAP = "pdfviewer-status";
    // the key for the URI of the PDF document loaded by the PDF viewer
    public static final String DOCUMENT_URI = "document-uri";
    // the key for the currently displayed page
    public static final String PAGE_NUMBER = "page-number";
    // the key for the scroll position on the page
    public static final String PAGE_POSITION = "page-position";
    // the key for exiting slide show mode
    public static final String SLIDE_SHOW_MODE = "slide-show-mode";
}
