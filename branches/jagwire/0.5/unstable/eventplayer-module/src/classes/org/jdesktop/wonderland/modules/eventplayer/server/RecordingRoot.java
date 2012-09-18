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

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.wfs.WorldRoot;

/**
 * Represents a wfs world 'root', the base directory of a recording wfs.
 * Using this, the web services know which wfs to write to or read from.
 * @author Bernard Horan
 */
@XmlRootElement(name="wfs-recording-root")
public class RecordingRoot extends WorldRoot {
    @XmlElement(name="xml-position-info")
    private String positionInfo;

    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(RecordingRoot.class);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }

    /** Default constructor */
    public RecordingRoot() {
        super();
    }

    /** Constructor, takes the root path
     * @param rootPath the path to the root of the recording
     */
    public RecordingRoot(String rootPath) {
        super(rootPath);
    }

    /**
     * Accessor for the positionInfo
     * @return the positionInfo
     */
    @XmlTransient
    public String getPositionInfo() {
        return positionInfo;
    }

    /**
     * Return the object that the position info unmarshalls into
     * @return an unmarshalled positionInfo
     * @throws javax.xml.bind.JAXBException if unmarshalling fails
     */
    public PositionComponentServerState getPosition() throws JAXBException {
        JAXBContext jaxbPositionContext = JAXBContext.newInstance(PositionComponentServerState.class);
        Unmarshaller unmarshaller = jaxbPositionContext.createUnmarshaller();
        StringReader r = new StringReader(positionInfo);
        return (PositionComponentServerState) unmarshaller.unmarshal(r);        
    }

    /**
     * Takes a reader for the XML stream and returns an instance of this class
     * <p>
     * @param r The reader of the XML stream
     * @return a RecordingRoot that's been decoded
     * @throw ClassCastException If the input file does not map to this class
     * @throws JAXBException Upon error reading the XML stream
     */
    public static RecordingRoot decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (RecordingRoot)unmarshaller.unmarshal(r);
    }

    /**
     * Writes the XML representation of this class to a writer.
     * <p>
     * @param w The output writer to write to
     * @throws JAXBException Upon error writing the XML file
     */
    @Override
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }

    /**
     * Set the position info for this root
     * @param positionInfo an xml-ised description of the position of a cell
     */
    public void setPositionInfo(String positionInfo) {
        this.positionInfo = positionInfo;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getSimpleName());
        buffer.append(" ");
        buffer.append(super.toString());
        return buffer.toString();
    }



}
