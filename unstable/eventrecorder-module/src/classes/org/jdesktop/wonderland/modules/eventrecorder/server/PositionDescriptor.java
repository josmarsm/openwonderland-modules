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
package org.jdesktop.wonderland.modules.eventrecorder.server;

import java.io.Reader;
import java.io.StringWriter;
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
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;

/**
 * Describes the position of the event recorder cell so that it may be recorded.
 * Represents tha name of the tape, and an xml-ised PostionComponentServerState
 * 
 * @author Bernard Horan
 */
@XmlRootElement(name="position-descriptor")
public class PositionDescriptor {
    /* the name of the tape (recording) for which this change is directed */
    @XmlElement(name="tape-name") private String tapeName = null;
    /* the xml-ised position information */
    @XmlElement(name="xml-position-info") private String positionInfo = null;

    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(PositionDescriptor.class);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /** Default constructor */
    public PositionDescriptor() {
    }
    
    /** Constructor, takes all of the class attributes */
    PositionDescriptor(String tapeName, PositionComponentServerState positionState) {
        this.tapeName = tapeName;
        this.positionInfo = getPositionInfo(positionState);        
    }



    @XmlTransient
    public String getPositionInfo() {
        return positionInfo;
    }
    
    /**
     * Takes a reader for the XML stream and returns an instance of this class
     * <p>
     * @param r The reader of the XML stream
     * @throw ClassCastException If the input file does not map to this class
     * @throw JAXBException Upon error reading the XML stream
     */
    public static PositionDescriptor decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (PositionDescriptor)unmarshaller.unmarshal(r);        
    }
    
    /**
     * Writes the XML representation of this class to a writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }

    /**
     * Accessor for the name of the tape
     * @return a string representing the name of the tape
     */
    public String getTapeName() {
        return tapeName;
    }

    

    private String getPositionInfo(PositionComponentServerState positionState) {
        try {
            JAXBContext jaxbPositionContext = JAXBContext.newInstance(PositionComponentServerState.class);
            Marshaller marshaller = jaxbPositionContext.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            StringWriter sw = new StringWriter();
            marshaller.marshal(positionState, sw);
            return sw.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(PositionDescriptor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    

    
}
