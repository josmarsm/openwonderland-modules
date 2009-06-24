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

package org.jdesktop.wonderland.modules.eventplayer.server;

import org.jdesktop.wonderland.modules.eventplayer.server.handler.DefaultTagHandler;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.TagHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An XML SAX handler for handing changes/events that have been recorded by the
 * event recorder.
 * @author Bernard Horan
 */
public class EventHandler extends DefaultHandler {
    private ChangeReplayer messageReplayer;
    private Stack<TagHandler> tagHandlerStack = new Stack<TagHandler>();
    
    
    /**
     * Create a new instance of this class
     * @param messageReplayer the object responsible for replaying the messages
     */
    public EventHandler(ChangeReplayer messageReplayer) {
        this.messageReplayer = messageReplayer;
    }
    
    @Override
    public void startElement (String uri, String name,
			      String qName, Attributes atts) {
        TagHandler tagHandler= newTagHandler(qName);
        tagHandlerStack.push(tagHandler);
        tagHandler.startTag(atts);
    }
    
    @Override
    public void characters (char ch[], int start, int length) {
        tagHandlerStack.peek().characters(ch, start, length);
    }
    
    @Override
    public void endElement(String uri, String name,String qName) {
        tagHandlerStack.pop().endTag();
    }
    
    
    
    /**
     * Create a new instance of a TagHandler for an XML element named tagName
     * @param tagName the name of the XML element for which a TagHandler is to be created
     * @return an instance of a implementation of TagHandler
     */
    protected TagHandler newTagHandler(String tagName) {
        Class<TagHandler> tagHandlerClass = getTagHandlerClass(tagName);
        TagHandler tagHandler;
        if (tagHandlerClass == null) {
            tagHandler = new DefaultTagHandler(messageReplayer);
        } else {
            tagHandler = newTagHandler(tagHandlerClass);
        }
        return tagHandler;
    }
    
    /**
     * Return a class that should be used to handle XML elements named elementName
     * @param elementName the name of the XML element
     * @return a class that should be a implementation of TagHandler
     */
    protected Class<TagHandler> getTagHandlerClass(String elementName) {
        return messageReplayer.getTagHandlerClass(elementName);
    }
    
    /**
     * Create a new instance of an implementation of TagHandler
     * @param handlerClass an implementation of TagHandler
     * @return an instance of a TagHandler class
     */
    protected TagHandler newTagHandler(Class<TagHandler> handlerClass) {
        try {
            Constructor<TagHandler> con = handlerClass.getConstructor(new Class[]{ChangeReplayer.class});
            return con.newInstance(messageReplayer);
        } catch (InstantiationException ex) {
            Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    
    
}
