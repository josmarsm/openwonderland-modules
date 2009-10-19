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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Describes a cell that has been loaded so that it may be recorded.
 * Represents tha name of the tape, a timestamp and the XML setup information
 * 
 * @author Bernard Horan
 */
@XmlRootElement(name="loadedcell-descriptor")
public class LoadedCellDescriptor {
    /* the name of the tape (recording) for which this change is directed */
    @XmlElement(name="tape-name") private String tapeName = null;
    @XmlElement(name="xml-setup-info") private String setupInfo = null;
    /* the timestamp of the change */
    @XmlElement(name="timestamp") private long timestamp = 0l;
    @XmlElement(name="parentID") private String parentID = null;

    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(LoadedCellDescriptor.class);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }
    
    /** Default constructor */
    public LoadedCellDescriptor() {
    }
    
    /** Constructor, takes all of the class attributes */
    public LoadedCellDescriptor(String tapeName, String setupInfo, CellID parentID, long timestamp) {
        this.tapeName = tapeName;
        this.setupInfo = setupInfo;
        this.timestamp = timestamp;
        if (parentID != null) {
            this.parentID = parentID.toString();
        }
    }

    public String getParentID() {
        return parentID;
    }

    @XmlTransient
    public String getSetupInfo() {
        return setupInfo;
    }
    
    /**
     * Takes a reader for the XML stream and returns an instance of this class
     * <p>
     * @param r The reader of the XML stream
     * @throw ClassCastException If the input file does not map to this class
     * @throw JAXBException Upon error reading the XML stream
     */
    public static LoadedCellDescriptor decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (LoadedCellDescriptor)unmarshaller.unmarshal(r);        
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

    public String getTapeName() {
        return tapeName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    

    
}
