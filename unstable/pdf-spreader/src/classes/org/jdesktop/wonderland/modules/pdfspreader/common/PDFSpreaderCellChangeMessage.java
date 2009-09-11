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

package org.jdesktop.wonderland.modules.pdfspreader.common;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

public class PDFSpreaderCellChangeMessage extends CellMessage {


    public enum MessageType {
        LAYOUT,
        DOCUMENT
    }

    public enum LayoutType {
        LINEAR,
        SEMICIRCLE,
        CIRCLE
    }

    private MessageType type;

    private float scale;
    private float spacing;
    private LayoutType layout;

    private int numPages;
    private float slideWidth;

    public PDFSpreaderCellChangeMessage(MessageType type) {
        this.type = type;
    }

    public LayoutType getLayout() {
        return layout;
    }

    public void setLayout(LayoutType layout) {
        this.layout = layout;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public MessageType getType() {
        return type;
    }

    public float getSlideWidth() {
        return slideWidth;
    }

    public void setSlideWidth(float slideWidth) {
        this.slideWidth = slideWidth;
    }


}