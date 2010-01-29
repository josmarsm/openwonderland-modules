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
package org.jdesktop.wonderland.modules.eventrecorder.server;

import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.messages.MessageID;

/**
 * Describes a message recorded by the event recording mechanism
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Bernard Horan
 */
@XmlRootElement(name="message-descriptor")
public class MessageDescriptor {
    /* the name of the tape (recording) for which this change is directed */
    @XmlElement(name="tape-name") private String tapeName = null;
    /* the message from darkstar encoded into text */
    @XmlElement(name="encoded-message") private String encodedMessage = null;
    /* the id of the message */
    @XmlElement(name="message-id") private long id = 0l;
    /* the timestamp of the message */
    @XmlElement(name="timestamp") private long timestamp = 0l;

    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(MessageDescriptor.class);
            MessageDescriptor.unmarshaller = jc.createUnmarshaller();
            MessageDescriptor.marshaller = jc.createMarshaller();
            MessageDescriptor.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            Logger.getLogger(MessageDescriptor.class.getName()).log(Level.WARNING,
                    "[WFS] Unable to create JAXBContext", excp);
        }
    }
    
    /** Default constructor */
    public MessageDescriptor() {
    }
    
    /** Constructor, takes all the class fields as parameters
     */
    public MessageDescriptor(String tapeName, long timestamp, MessageID messageID, String encodedMessage) {
        this.tapeName = tapeName;
        this.timestamp = timestamp;
        this.encodedMessage = encodedMessage;
        this.id = messageID.getID();
    }

    @XmlTransient
    public String getTapeName() {
        return tapeName;
    }

    @XmlTransient
    public long getTimestamp() {
        return timestamp;
    }

    @XmlTransient
    public String getEncodedMessage() {
        return encodedMessage;
    }

    @XmlTransient
    public long getMessageID() {
        return id;
    }
    
    /**
     * Takes a reader for the XML stream and returns an instance of this class
     * <p>
     * @param r The reader of the XML stream
     * @throw ClassCastException If the input file does not map to this class
     * @throw JAXBException Upon error reading the XML stream
     */
    public static MessageDescriptor decode(Reader r) throws JAXBException {
        return (MessageDescriptor)MessageDescriptor.unmarshaller.unmarshal(r);
    }
    
    /**
     * Writes the XML representation of this class to a writer.
     * <p>
     * @param w The output writer to write to
     * @throws JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        MessageDescriptor.marshaller.marshal(this, w);
    }
}
