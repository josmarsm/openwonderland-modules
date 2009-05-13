/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.eventplayer.server;

import org.xml.sax.Attributes;

/**
 *
 * @author bh37721
 */
public interface TagHandler {

    public void startTag(Attributes atts);

    public void characters(char ch[], int start, int length);

    public void endTag();
}
