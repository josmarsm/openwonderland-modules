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
package org.jdesktop.wonderland.modules.marbleous.common.cell;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.marbleous.common.LoopTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.RightTurnTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.Track;

/**
 * The WFS server state class for TrackCellMO.
 * 
 * @author deronj, Bernard Horan
 */
@XmlRootElement(name="track-cell")
@ServerState
public class TrackCellServerState extends CellServerState {
    //@XmlElement(name="track")
    private Track track;
    
    /** Default constructor */
    public TrackCellServerState() {}
    
    /** {@inheritDoc} */
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.marbleous.server.cell.TrackCellMO";
    }

    public static void main(String args[]) throws JAXBException {
        TrackCellServerState serverState = new TrackCellServerState();
        serverState.track = new Track();
        serverState.track.addTrackSegment(new LoopTrackSegmentType().createSegment());
        serverState.track.addTrackSegment(new RightTurnTrackSegmentType().createSegment());

        StringWriter sw = new StringWriter();
        serverState.encode(sw);
        System.out.println(sw.toString());
        TrackCellServerState returned = TrackCellServerState.decode(new StringReader(sw.toString()));
        
    }

    /**
     * Writes the ModuleInfo class to an output stream.
     * <p>
     * @param os The output stream to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer os) throws JAXBException {
        /* Write out to the stream */
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TrackCellServerState.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(this, os);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
        
    }

    public static TrackCellServerState decode(Reader in) throws JAXBException {
        /* Read in from stream */
        JAXBContext jaxbContext = JAXBContext.newInstance(TrackCellServerState.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        TrackCellServerState info = (TrackCellServerState)unmarshaller.unmarshal(in);

        return info;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

}
