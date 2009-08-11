/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author bh37721
 */
public class EventHandler extends DefaultHandler {
    private EventPlayerImpl eventPlayer;
    private Stack<TagHandler> tagHandlerStack = new Stack<TagHandler>();
    
    
    public EventHandler(EventPlayerImpl eventPlayer) {
        this.eventPlayer = eventPlayer;
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
    
    
    
    protected TagHandler newTagHandler(String tagName) {
        Class tagHandlerClass = getTagHandlerClass(tagName);
        TagHandler tagHandler;
        if (tagHandlerClass == null) {
            tagHandler = new DefaultTagHandler(eventPlayer);
        } else {
            tagHandler = newTagHandler(tagHandlerClass);
        }
        return tagHandler;
    }
    
    protected Class getTagHandlerClass(String elementName) {
        return eventPlayer.getTagHandlerClass(elementName);
    }
    
    protected TagHandler newTagHandler(Class handlerClass) {
        try {
            Constructor<TagHandler> con = handlerClass.getConstructor(new Class[]{EventPlayerImpl.class});
            return con.newInstance(eventPlayer);
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