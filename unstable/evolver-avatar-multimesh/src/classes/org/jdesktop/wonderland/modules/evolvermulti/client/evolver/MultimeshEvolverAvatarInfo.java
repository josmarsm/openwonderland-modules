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
package org.jdesktop.wonderland.modules.evolvermulti.client.evolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshFeetConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshGenderConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshHairConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshHandsConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshHeadConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshJacketConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshLegsConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshModelConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshShapesConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshTorsoConfigElement;

/**
 * A JAXB-annotated class to represent information about an Evolver avatar. This
 * consists of a collection of individual configuration elements.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="multimesh-evolver-avatar")
public class MultimeshEvolverAvatarInfo {

    // The error logger
    private static Logger LOGGER =
            Logger.getLogger(MultimeshEvolverAvatarInfo.class.getName());

    // An array of config elements
    @XmlElementRefs({
        @XmlElementRef()
    })
    public MultimeshConfigElement[] configElements = new MultimeshConfigElement[0];

    // The name of the avatar
    @XmlElement(name="name")
    private String avatarName = null;

    // The JAXB context to (un)marshall (from)to XML
    private static JAXBContext jaxbContext = null;
    static {
        try {
            jaxbContext = JAXBContext.newInstance(
                    MultimeshEvolverAvatarInfo.class, MultimeshConfigElement.class,
                    MultimeshFeetConfigElement.class, MultimeshGenderConfigElement.class,
                    MultimeshHairConfigElement.class, MultimeshHandsConfigElement.class,
                    MultimeshHeadConfigElement.class, MultimeshJacketConfigElement.class,
                    MultimeshLegsConfigElement.class, MultimeshModelConfigElement.class,
                    MultimeshShapesConfigElement.class, MultimeshTorsoConfigElement.class);
        } catch (javax.xml.bind.JAXBException excp) {
            LOGGER.log(Level.WARNING, "Error creating JAXB context", excp);
        }
    }

    /** Default constructor, required by JAXB */
    public MultimeshEvolverAvatarInfo() {
    }

    @XmlTransient
    public String getAvatarName() {
        // Returns the avatar name. If it contains spaces, replace with under-
        // scores to get around a bug in WebDav
        return avatarName.replaceAll(" ", "_");
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    @XmlTransient
    public MultimeshConfigElement[] getConfigElements() {
        return configElements;
    }

    public void setConfigElements(MultimeshConfigElement[] configElements) {
        this.configElements = configElements;
    }
    
    /**
     * Returns a new EvolverAvatarInfo class from the XML read from the input
     * stream.
     *
     * @param is The input stream of XML data
     * @return A new EvolverAvatarInfo class
     * @throw IOException Upon I/O error
     * @throw JAXBException Upon parsing error
     */
    public static MultimeshEvolverAvatarInfo decode(InputStream is)
            throws IOException, JAXBException  {

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MultimeshEvolverAvatarInfo) unmarshaller.unmarshal(is);
    }

    /**
     * Writes this EvolverAvatarInfo class to the output stream as XML.
     *
     * @param is The output stream of XML data
     * @throw IOException Upon I/O error
     * @throw JAXBException Upon parsing error
     */
    public void encode(OutputStream os)
            throws IOException, JAXBException  {

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(this, os);
    }
}
