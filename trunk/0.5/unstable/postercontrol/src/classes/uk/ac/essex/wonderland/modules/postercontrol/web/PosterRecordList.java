/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package uk.ac.essex.wonderland.modules.postercontrol.web;

import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container for an array of poster records that can be serialised
 * @author Bernard Horan
 */
@XmlRootElement(name="poster-records")
public class PosterRecordList {
    /* An array of posters */
    @XmlElements({
        @XmlElement(name="poster")
    })
    private PosterRecord[] posters = null;

    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(PosterRecordList.class);
        } catch (javax.xml.bind.JAXBException excp) {
            Logger.getLogger(PosterRecordList.class.getName()).log(Level.WARNING,
                    "Unable to create JAXBContext", excp);
        }
    }

    /**
     * Takes the input stream of the XML and instantiates an instance of
     * the PosterRecordList class
     * <p>
     * @param is The input stream of the XML representation
     * @throw ClassCastException If the input file does not map to PosterRecordList
     * @throw JAXBException Upon error reading the XML stream
     */
    public static PosterRecordList decode(String relativePath, InputStream is) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        PosterRecordList posters = (PosterRecordList)unmarshaller.unmarshal(is);
        return posters;
    }

    public PosterRecordList(Collection<PosterRecord> records) {
        posters = records.toArray(new PosterRecord[] {});
    }

    public PosterRecordList() {
        posters = new PosterRecord[0];
    }

    /**
     * Writes the PosterRecordList instance to an output stream.
     * <p>
     * @param w The output write to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }
}
