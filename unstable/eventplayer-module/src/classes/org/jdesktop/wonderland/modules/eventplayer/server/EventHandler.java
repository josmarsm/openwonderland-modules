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

package org.jdesktop.wonderland.modules.eventplayer.server;

import org.jdesktop.wonderland.modules.eventplayer.server.handler.TagHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.NoTagHandler;
import org.xml.sax.Attributes;

/**
 * An XML SAX handler for handing changes/events that have been recorded by the
 * event recorder.
 * @author Bernard Horan
 */
public class EventHandler  {
    private ChangeReplayer messageReplayer;
    private Stack<TagHandler> tagHandlerStack = new Stack<TagHandler>();
    
    
    /**
     * Create a new instance of this class
     * @param messageReplayer the object responsible for replaying the messages
     */
    public EventHandler(ChangeReplayer messageReplayer) {
        this.messageReplayer = messageReplayer;
    }
    
    /**
     * Notification of the start of an XML element
     * @param uri the uri of the element
     * @param name the name of the element
     * @param qName the qualified name of the element
     * @param atts the attributes in the element
     * @param semaphore the semaphore to be signalled
     */
    public void startElement(String uri, String name,
			      String qName, Attributes atts, Semaphore semaphore) {
        TagHandler tagHandler= newTagHandler(qName);
        tagHandlerStack.push(tagHandler);
        tagHandler.startTag(atts, semaphore);
    }
    
    /**
     * Notification of characters in an XML document
     * @param ch the array of characters
     * @param start the starting index of the characters in the array
     * @param length the length of characters in the array
     * @param semaphore the semaphore to be signalled
     */
    public void characters(char ch[], int start, int length, Semaphore semaphore) {
        tagHandlerStack.peek().characters(ch, start, length, semaphore);
    }
    
    /**
     * Notification of the end of an XML element
     * @param uri the uri of the element
     * @param name the name of the element
     * @param qName the qualified name of the element
     * @param semaphore the semaphore to be signalled
     */
    public void endElement(String uri, String name,String qName, Semaphore semaphore) {
        tagHandlerStack.pop().endTag(semaphore);
    }
    
    
    
    /**
     * Create a new instance of a TagHandler for an XML element named tagName
     * @param tagName the name of the XML element for which a TagHandler is to be created
     * @return an instance of a implementation of TagHandler
     */
    protected TagHandler newTagHandler(String tagName) {
        Class<? extends TagHandler> tagHandlerClass = getTagHandlerClass(tagName);
        TagHandler tagHandler;
        if (tagHandlerClass == null) {
            tagHandler = new NoTagHandler(messageReplayer);
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
    protected Class<? extends TagHandler> getTagHandlerClass(String elementName) {
        return messageReplayer.getTagHandlerClass(elementName);
    }
    
    /**
     * Create a new instance of an implementation of TagHandler
     * @param handlerClass an implementation of TagHandler
     * @return an instance of a TagHandler class
     */
    protected TagHandler newTagHandler(Class<? extends TagHandler> handlerClass) {
        try {
            Constructor<? extends TagHandler> con = handlerClass.getConstructor(new Class[]{ChangeReplayer.class});
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
